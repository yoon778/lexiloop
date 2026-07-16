package com.yoon778.lexiloop.presentation.viewmodel

import com.yoon778.lexiloop.data.gemini.GeminiCallResult
import com.yoon778.lexiloop.data.gemini.GeminiRepository
import com.yoon778.lexiloop.data.local.LexiLoopDatabase
import com.yoon778.lexiloop.data.local.entity.LearningItemEntity
import com.yoon778.lexiloop.data.local.entity.SessionItemEntity
import com.yoon778.lexiloop.data.repository.RoomLearningRepository
import com.yoon778.lexiloop.data.repository.SessionStartResult
import com.yoon778.lexiloop.data.settings.SettingsRepository
import com.yoon778.lexiloop.domain.learning.LearningEvent
import com.yoon778.lexiloop.domain.learning.LearningStep
import com.yoon778.lexiloop.domain.learning.answerHint
import com.yoon778.lexiloop.domain.learning.isEnglishAnswerCorrect
import com.yoon778.lexiloop.domain.learning.nextLearningStep
import com.yoon778.lexiloop.domain.model.LearningPhase
import com.yoon778.lexiloop.domain.model.ReviewOutcome
import com.yoon778.lexiloop.domain.model.SessionType
import com.yoon778.lexiloop.domain.progress.calculateStreak
import com.yoon778.lexiloop.presentation.contract.AppRoute
import com.yoon778.lexiloop.presentation.contract.HomeUiState
import com.yoon778.lexiloop.presentation.contract.LoadState
import com.yoon778.lexiloop.presentation.contract.SessionResultUiState
import com.yoon778.lexiloop.presentation.contract.StudyEvent
import com.yoon778.lexiloop.presentation.contract.StudyFeedback
import com.yoon778.lexiloop.presentation.contract.StudyUiState
import com.yoon778.lexiloop.presentation.contract.UiEffect
import com.yoon778.lexiloop.presentation.contract.WordListItemUiState
import com.yoon778.lexiloop.presentation.contract.WordManagementEvent
import java.io.IOException
import java.time.LocalDate
import kotlin.random.Random
import kotlinx.coroutines.flow.first

class LexiLoopViewModelProvider(
    private val database: LexiLoopDatabase,
    private val learningRepository: RoomLearningRepository,
    private val settingsRepository: SettingsRepository,
    private val geminiRepository: GeminiRepository,
    private val today: () -> LocalDate = LocalDate::now,
    private val nowMillis: () -> Long = System::currentTimeMillis,
) {
    private val dao = database.dao()

    fun onboarding() = OnboardingViewModel(
        analyze = { request ->
            when (val result = geminiRepository.analyzePurpose(request)) {
                is GeminiCallResult.Success -> result.value
                is GeminiCallResult.Failure -> throw IOException(result.error.code.name)
            }
        },
        saveProfile = settingsRepository::setRecommendationProfile,
        completeOnboarding = settingsRepository::setOnboarding,
    )

    fun home() = HomeViewModel(
        load = ::loadHome,
        startReview = { startSession(SessionType.REVIEW) },
        startNew = { startSession(SessionType.NEW) },
    )

    fun study(sessionId: String) = StudyViewModel(
        sessionId = sessionId,
        loadInitial = { loadStudy(sessionId) },
        reduce = ::reduceStudy,
    )

    fun words() = WordManagementViewModel(
        load = { query, status ->
            LoadState.Content(
                dao.managedWords(query.trim().lowercase(), status).map { row ->
                    WordListItemUiState(
                        id = row.id,
                        expression = row.expression,
                        targetMeaningKo = row.targetMeaningKo,
                        status = row.status,
                        excluded = row.excludedAtMillis != null,
                    )
                },
            )
        },
        mutate = { event ->
            when (event) {
                is WordManagementEvent.RestoreExcluded ->
                    learningRepository.restoreExcludedItem(event.itemId, nowMillis())
                is WordManagementEvent.MarkFullyKnown ->
                    learningRepository.markItemFullyKnown(event.itemId, today(), nowMillis())
                is WordManagementEvent.AddErrorNote ->
                    learningRepository.addErrorNote(event.itemId, event.note, nowMillis())
                else -> Unit
            }
        },
    )

    suspend fun sessionResult(sessionId: String): SessionResultUiState {
        val session = requireNotNull(dao.studySession(sessionId))
        return SessionResultUiState(
            sessionId = session.id,
            sessionType = session.type,
            completedCount = dao.completedSessionItemCount(sessionId),
        )
    }

    private suspend fun loadHome(): HomeUiState {
        val date = today()
        val settings = settingsRepository.settings.first()
        val dueCount = dao.dueReviewCount(date.toEpochDay())
        val queued = dao.queuedItems(settings.dailyNewCount)
        val active = dao.activeSession(date.toEpochDay(), SessionType.REVIEW)
            ?: dao.activeSession(date.toEpochDay(), SessionType.NEW)
        return HomeUiState(
            dueReviewCount = dueCount,
            dailyNewGoal = settings.dailyNewCount,
            availableNewCount = dao.queuedCount().first(),
            streakDays = calculateStreak(dao.completedSessionEpochDays().toSet(), date.toEpochDay()),
            learnedTotal = dao.learnedTotal().first(),
            newStudyLocked = dueCount >= 21,
            lockReason = if (dueCount >= 21) "복습이 20개 이하가 되면 신규 학습을 시작할 수 있어요" else null,
            activeSessionType = active?.type,
            activeSessionId = active?.id,
            newItems = if (queued.isEmpty()) {
                LoadState.Empty("오늘 학습할 신규 단어가 없어요")
            } else {
                LoadState.Content(queued.map { it.expression to it.targetMeaningKo })
            },
            isLoading = false,
        )
    }

    private suspend fun startSession(type: SessionType): String? {
        val result = if (type == SessionType.REVIEW) {
            learningRepository.startReviewSession(today(), nowMillis())
        } else {
            val goal = settingsRepository.settings.first().dailyNewCount
            learningRepository.startNewSession(today(), goal, nowMillis())
        }
        return when (result) {
            is SessionStartResult.Started -> result.session.id
            is SessionStartResult.Existing -> result.session.id
            is SessionStartResult.Empty, is SessionStartResult.Locked -> null
        }
    }

    private suspend fun loadStudy(sessionId: String): StudyUiState {
        val session = requireNotNull(dao.studySession(sessionId))
        val sessionItem = dao.currentSessionItem(sessionId)
            ?: return completedState(session.id, session.type, session.goalCount)
        val item = requireNotNull(dao.learningItem(sessionItem.itemId))
        val choices = choices(item, sessionItem.phase)
        return StudyUiState(
            sessionId = session.id,
            sessionType = session.type,
            completedCount = dao.completedSessionItemCount(sessionId),
            totalCount = session.goalCount,
            itemId = item.id,
            expression = item.expression,
            phonetic = item.phonetic,
            targetMeaningKo = item.targetMeaningKo,
            auxiliaryMeanings = dao.auxiliaryMeanings(item.id).map { it.meaningKo },
            exampleSentence = item.exampleSentence,
            exampleTranslationKo = item.exampleTranslationKo,
            phase = sessionItem.phase,
            phaseContent = choices,
            feedback = if (sessionItem.phase == LearningPhase.CORRECTION) {
                StudyFeedback(false, "정답은 ${expectedAnswer(item, sessionItem.phase, sessionItem.retryPhase)}")
            } else null,
            canSubmit = false,
            isLoading = false,
        )
    }

    private suspend fun reduceStudy(state: StudyUiState, event: StudyEvent): StudyTransition {
        val session = requireNotNull(dao.studySession(state.sessionId))
        val stored = requireNotNull(dao.currentSessionItem(state.sessionId))
        val item = requireNotNull(dao.learningItem(stored.itemId))
        return when (event) {
            is StudyEvent.SelfAssessmentSelected -> persistStep(
                state,
                stored,
                nextLearningStep(stored.step(), LearningEvent.Assessed(event.value)),
                null,
            )
            is StudyEvent.ChoiceSelected -> {
                val answer = state.phaseContent.getOrNull(event.index).orEmpty()
                answerTransition(state, stored, item, answer, answer == expectedAnswer(item, stored.phase, null))
            }
            StudyEvent.Submit -> answerTransition(
                state,
                stored,
                item,
                state.answerText,
                isEnglishAnswerCorrect(expectedAnswer(item, stored.phase, null), state.answerText),
            )
            StudyEvent.HintRequested -> {
                val hint = answerHint(expectedAnswer(item, stored.phase, null), item.targetMeaningKo)
                StudyTransition(state.copy(hint = "${hint.firstCharacter ?: '-'} · ${hint.characterCount}글자 · ${hint.targetMeaningKo}"))
            }
            StudyEvent.Next -> persistStep(
                state,
                stored,
                nextLearningStep(stored.step(), LearningEvent.CorrectionAcknowledged),
                null,
            )
            StudyEvent.MarkFullyKnown -> {
                learningRepository.markFullyKnown(state.sessionId, item.id, today(), nowMillis())
                nextItemOrResult(state.sessionId)
            }
            StudyEvent.Defer -> {
                if (session.type == SessionType.NEW) {
                    learningRepository.deferNewItem(state.sessionId, item.id, nowMillis())
                } else {
                    learningRepository.deferReviewItem(state.sessionId, item.id, nowMillis())
                }
                nextItemOrResult(state.sessionId)
            }
            StudyEvent.Exclude -> {
                learningRepository.excludeSessionItem(state.sessionId, item.id, nowMillis())
                nextItemOrResult(state.sessionId)
            }
            is StudyEvent.AddErrorNote -> {
                learningRepository.addErrorNote(item.id, event.note, nowMillis())
                StudyTransition(state, listOf(UiEffect.Message("오류 메모를 저장했어요")))
            }
            is StudyEvent.AnswerChanged, StudyEvent.RepeatPronunciation -> StudyTransition(state)
        }
    }

    private suspend fun answerTransition(
        state: StudyUiState,
        stored: SessionItemEntity,
        item: LearningItemEntity,
        answer: String,
        correct: Boolean,
    ): StudyTransition {
        val step = nextLearningStep(stored.step(), LearningEvent.Answered(correct))
        if (step.phase != LearningPhase.DONE) return persistStep(state, stored, step, answer)

        val session = requireNotNull(dao.studySession(state.sessionId))
        if (session.type == SessionType.NEW) {
            learningRepository.completeNewItem(
                state.sessionId,
                item.id,
                knownPathPassed = step.knownPath,
                today = today(),
                nowMillis = nowMillis(),
            )
        } else {
            learningRepository.completeReviewItem(
                state.sessionId,
                item.id,
                outcome = if (stored.hadInitialReviewError) ReviewOutcome.RECOVERED else ReviewOutcome.PASS,
                today = today(),
                nowMillis = nowMillis(),
            )
        }
        return nextItemOrResult(state.sessionId)
    }

    private suspend fun persistStep(
        state: StudyUiState,
        stored: SessionItemEntity,
        step: LearningStep,
        answer: String?,
    ): StudyTransition {
        learningRepository.saveLearningStep(state.sessionId, stored.itemId, step, answer, nowMillis())
        return StudyTransition(loadStudy(state.sessionId))
    }

    private suspend fun nextItemOrResult(sessionId: String): StudyTransition {
        val next = loadStudy(sessionId)
        return if (next.phase == LearningPhase.DONE) {
            StudyTransition(next, listOf(UiEffect.Navigate(AppRoute.SessionResult(sessionId))))
        } else {
            StudyTransition(next)
        }
    }

    private suspend fun choices(item: LearningItemEntity, phase: LearningPhase): List<String> {
        if (phase !in setOf(LearningPhase.EN_TO_KO, LearningPhase.KO_TO_EN)) {
            return if (phase == LearningPhase.SENTENCE) {
                listOf(item.exampleSentence.replace(item.exampleTargetForm, "_____", ignoreCase = true))
            } else emptyList()
        }
        val distractors = dao.distractorItems(item.id, 12)
        val values = if (phase == LearningPhase.EN_TO_KO) {
            listOf(item.targetMeaningKo) + distractors.map { it.targetMeaningKo }
        } else {
            listOf(item.expression) + distractors.map { it.expression }
        }
        return values.distinct().take(4).shuffled(Random(item.id.hashCode() + phase.ordinal))
    }

    private fun expectedAnswer(
        item: LearningItemEntity,
        phase: LearningPhase,
        retryPhase: LearningPhase?,
    ): String = when (if (phase == LearningPhase.CORRECTION) retryPhase else phase) {
        LearningPhase.EN_TO_KO -> item.targetMeaningKo
        LearningPhase.KO_TO_EN, LearningPhase.SPELLING -> item.expression
        LearningPhase.SENTENCE -> item.exampleTargetForm
        else -> item.expression
    }

    private fun SessionItemEntity.step() = LearningStep(
        phase = phase,
        knownPath = knownPath,
        retryPhase = retryPhase,
        phaseFailureCount = phaseFailureCount,
    )

    private fun completedState(sessionId: String, type: SessionType, total: Int) = StudyUiState(
        sessionId = sessionId,
        sessionType = type,
        completedCount = total,
        totalCount = total,
        itemId = "",
        expression = "",
        targetMeaningKo = "",
        exampleSentence = "",
        exampleTranslationKo = "",
        phase = LearningPhase.DONE,
        canGoBack = false,
    )
}
