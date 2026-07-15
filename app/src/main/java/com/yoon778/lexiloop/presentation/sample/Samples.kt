package com.yoon778.lexiloop.presentation.sample

import com.yoon778.lexiloop.data.settings.RecommendationProfile
import com.yoon778.lexiloop.data.settings.RecommendationTopic
import com.yoon778.lexiloop.domain.model.Difficulty
import com.yoon778.lexiloop.domain.model.LearningPhase
import com.yoon778.lexiloop.domain.model.LearningStatus
import com.yoon778.lexiloop.domain.model.SessionType
import com.yoon778.lexiloop.presentation.contract.HomeUiState
import com.yoon778.lexiloop.presentation.contract.LoadState
import com.yoon778.lexiloop.presentation.contract.OnboardingUiState
import com.yoon778.lexiloop.presentation.contract.SessionResultUiState
import com.yoon778.lexiloop.presentation.contract.SettingsUiState
import com.yoon778.lexiloop.presentation.contract.StudyFeedback
import com.yoon778.lexiloop.presentation.contract.StudyUiState
import com.yoon778.lexiloop.presentation.contract.WordListItemUiState
import com.yoon778.lexiloop.presentation.contract.WordManagementUiState

/** Preview·테스트 전용 샘플 상태. 실제 데이터가 아님. */
object Samples {
    val profile = RecommendationProfile(
        topics = listOf(
            RecommendationTopic("1", "일상 영어", 50),
            RecommendationTopic("2", "개발·업무 영어", 50),
        ),
        difficulty = Difficulty.INTERMEDIATE,
        excludedTopics = emptyList(),
    )

    val onboarding = OnboardingUiState(
        purposeText = "일상과 개발자 관점 단어를 공부하고 싶어",
        difficulty = Difficulty.INTERMEDIATE,
        dailyNewCount = 20,
    )

    val home = HomeUiState(
        dueReviewCount = 12,
        dailyNewGoal = 20,
        availableNewCount = 20,
        streakDays = 4,
        learnedTotal = 128,
        newStudyLocked = false,
        activeSessionType = null,
        isOffline = false,
        isLoading = false,
    )

    val homeLocked = home.copy(
        dueReviewCount = 24,
        newStudyLocked = true,
        lockReason = "밀린 복습 24개를 먼저 끝내주세요",
    )

    val newOverview: List<Pair<String, String>> = listOf(
        "deploy" to "배포하다",
        "grocery" to "식료품",
        "maintain" to "유지하다",
        "refactor" to "리팩터링하다",
        "commute" to "통근하다",
    )

    fun study(phase: LearningPhase, feedback: StudyFeedback? = null) = StudyUiState(
        sessionId = "s1",
        sessionType = SessionType.NEW,
        completedCount = 8,
        totalCount = 20,
        itemId = "i1",
        expression = "deploy",
        phonetic = "/dɪˈplɔɪ/",
        targetMeaningKo = "배포하다",
        auxiliaryMeanings = listOf("전개하다", "배치하다"),
        exampleSentence = "We deploy the app every Friday.",
        exampleTranslationKo = "우리는 매주 금요일에 앱을 배포한다.",
        phase = phase,
        phaseContent = when (phase) {
            LearningPhase.EN_TO_KO -> listOf("배포하다", "식료품", "유지하다", "통근하다")
            LearningPhase.KO_TO_EN -> listOf("deploy", "grocery", "maintain", "commute")
            LearningPhase.SENTENCE -> listOf("We ___ the app every Friday.")
            else -> emptyList()
        },
        hint = if (phase == LearningPhase.SPELLING) "d · 6글자 · 배포하다" else null,
        feedback = feedback,
        canSubmit = true,
    )

    val sessionResult = SessionResultUiState(
        sessionId = "s1",
        sessionType = SessionType.NEW,
        completedCount = 20,
    )

    val words = WordManagementUiState(
        query = "",
        statusFilter = null,
        words = LoadState.Content(
            listOf(
                WordListItemUiState("1", "deploy", "배포하다", LearningStatus.REVIEWING, false),
                WordListItemUiState("2", "grocery", "식료품", LearningStatus.LEARNING, false),
                WordListItemUiState("3", "legacy", "레거시", LearningStatus.MASTERED, false),
                WordListItemUiState("4", "obsolete", "구식의", LearningStatus.QUEUED, true),
            ),
        ),
    )

    val settings = SettingsUiState(
        dailyNewCount = 20,
        notificationEnabled = true,
        notificationHour = 20,
        notificationMinute = 0,
    )
}
