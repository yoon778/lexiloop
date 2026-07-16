package com.yoon778.lexiloop.data.repository

import androidx.room.withTransaction
import com.yoon778.lexiloop.data.local.LexiLoopDatabase
import com.yoon778.lexiloop.data.local.entity.AuxiliaryMeaningEntity
import com.yoon778.lexiloop.data.local.entity.ErrorNoteCategory
import com.yoon778.lexiloop.data.local.entity.ErrorNoteEntity
import com.yoon778.lexiloop.data.local.entity.LearningItemEntity
import com.yoon778.lexiloop.data.local.entity.LearningProgressEntity
import com.yoon778.lexiloop.data.local.entity.SessionItemEntity
import com.yoon778.lexiloop.data.local.entity.StudySessionEntity
import com.yoon778.lexiloop.domain.learning.LearningStep
import com.yoon778.lexiloop.domain.learning.normalizeEnglish
import com.yoon778.lexiloop.domain.model.LearningItemDraft
import com.yoon778.lexiloop.domain.model.LearningPhase
import com.yoon778.lexiloop.domain.model.LearningStatus
import com.yoon778.lexiloop.domain.model.ReviewOutcome
import com.yoon778.lexiloop.domain.model.SessionItemState
import com.yoon778.lexiloop.domain.model.SessionStatus
import com.yoon778.lexiloop.domain.model.SessionType
import com.yoon778.lexiloop.domain.review.ReviewSchedule
import com.yoon778.lexiloop.domain.review.initialReviewSchedule
import com.yoon778.lexiloop.domain.review.isNewLearningLocked
import com.yoon778.lexiloop.domain.review.nextReviewSchedule
import com.yoon778.lexiloop.domain.review.reviewEntryPhase
import java.time.LocalDate
import java.util.UUID

class RoomLearningRepository(
    private val database: LexiLoopDatabase,
    private val newId: () -> String = { UUID.randomUUID().toString() },
) {
    private val dao = database.dao()

    suspend fun insertBatch(
        drafts: List<LearningItemDraft>,
        generationBatchId: String?,
        nowMillis: Long,
    ): Int = database.withTransaction {
        require(drafts.isNotEmpty()) { "Batch cannot be empty" }
        drafts.forEach(::validateDraft)

        val stored = drafts.map { draft ->
            draft.toStoredItem(
                id = newId(),
                generationBatchId = generationBatchId,
                nowMillis = nowMillis,
            )
        }
        val contentKeys = stored.map { it.item.contentKey }
        require(contentKeys.distinct().size == contentKeys.size) { "Batch contains duplicate cards" }
        require(dao.existingContentKeys(contentKeys).isEmpty()) { "Batch overlaps stored cards" }

        val firstQueueOrder = dao.maxQueueOrder() + 1
        dao.insertLearningItems(stored.map(StoredItem::item))
        dao.insertAuxiliaryMeanings(stored.flatMap(StoredItem::meanings))
        dao.insertProgresses(
            stored.mapIndexed { index, item ->
                LearningProgressEntity(
                    itemId = item.item.id,
                    status = LearningStatus.QUEUED,
                    queueOrder = firstQueueOrder + index,
                    intervalIndex = null,
                    dueEpochDay = null,
                    learnedEpochDay = null,
                    masteredEpochDay = null,
                    excludedAtMillis = null,
                    updatedAtMillis = nowMillis,
                )
            },
        )
        stored.size
    }

    suspend fun startNewSession(
        today: LocalDate,
        goalCount: Int,
        nowMillis: Long,
    ): SessionStartResult = database.withTransaction {
        require(goalCount > 0)
        dao.sessionForDay(today.toEpochDay(), SessionType.NEW)?.let {
            return@withTransaction SessionStartResult.Existing(it)
        }
        val dueCount = dao.dueReviewCount(today.toEpochDay())
        if (isNewLearningLocked(dueCount)) {
            return@withTransaction SessionStartResult.Locked(dueCount)
        }

        val supplementaryTarget = goalCount * SUPPLEMENTARY_PERCENT / 100
        val coreTarget = goalCount - supplementaryTarget
        var coreItems = dao.queuedCoreItems(coreTarget)
        var supplementaryItems = dao.queuedSupplementaryItems(supplementaryTarget)
        val missingCount = goalCount - coreItems.size - supplementaryItems.size
        if (missingCount > 0) {
            if (coreItems.size < coreTarget) {
                supplementaryItems = dao.queuedSupplementaryItems(supplementaryTarget + missingCount)
            } else {
                coreItems = dao.queuedCoreItems(coreTarget + missingCount)
            }
        }
        val items = coreItems + supplementaryItems
        if (items.isEmpty()) return@withTransaction SessionStartResult.Empty

        createSession(
            type = SessionType.NEW,
            today = today,
            items = items,
            nowMillis = nowMillis,
        ) { LearningStep(LearningPhase.CARD) }
    }

    suspend fun startReviewSession(
        today: LocalDate,
        nowMillis: Long,
    ): SessionStartResult = database.withTransaction {
        dao.sessionForDay(today.toEpochDay(), SessionType.REVIEW)?.let {
            return@withTransaction SessionStartResult.Existing(it)
        }
        val items = dao.dueItems(today.toEpochDay())
        if (items.isEmpty()) return@withTransaction SessionStartResult.Empty

        createSession(
            type = SessionType.REVIEW,
            today = today,
            items = items,
            nowMillis = nowMillis,
        ) { item ->
            val intervalIndex = requireNotNull(dao.progress(item.id)?.intervalIndex)
            LearningStep(reviewEntryPhase(intervalIndex))
        }
    }

    suspend fun saveLearningStep(
        sessionId: String,
        itemId: String,
        step: LearningStep,
        lastSubmittedAnswer: String?,
        nowMillis: Long,
    ) = database.withTransaction {
        val current = requireNotNull(dao.sessionItem(sessionId, itemId))
        dao.updateSessionItem(
            current.copy(
                state = SessionItemState.ACTIVE,
                phase = step.phase,
                retryPhase = step.retryPhase,
                knownPath = step.knownPath,
                phaseFailureCount = step.phaseFailureCount,
                hadInitialReviewError = current.hadInitialReviewError ||
                    step.phase == LearningPhase.CORRECTION,
                lastSubmittedAnswer = lastSubmittedAnswer,
                updatedAtMillis = nowMillis,
            ),
        )
    }

    suspend fun completeNewItem(
        sessionId: String,
        itemId: String,
        knownPathPassed: Boolean,
        today: LocalDate,
        nowMillis: Long,
    ) = database.withTransaction {
        val schedule = initialReviewSchedule(knownPathPassed, today)
        dao.updateProgressState(
            itemId = itemId,
            status = LearningStatus.REVIEWING,
            queueOrder = null,
            intervalIndex = schedule.intervalIndex,
            dueEpochDay = schedule.dueDate.toEpochDay(),
            learnedEpochDay = today.toEpochDay(),
            masteredEpochDay = null,
            updatedAtMillis = nowMillis,
        )
        completeSessionItem(sessionId, itemId, nowMillis)
    }

    suspend fun markFullyKnown(
        sessionId: String,
        itemId: String,
        today: LocalDate,
        nowMillis: Long,
    ) = database.withTransaction {
        dao.updateProgressState(
            itemId = itemId,
            status = LearningStatus.REVIEWING,
            queueOrder = null,
            intervalIndex = 5,
            dueEpochDay = today.plusDays(30).toEpochDay(),
            learnedEpochDay = today.toEpochDay(),
            masteredEpochDay = null,
            updatedAtMillis = nowMillis,
        )
        completeSessionItem(sessionId, itemId, nowMillis)
    }

    suspend fun markItemFullyKnown(
        itemId: String,
        today: LocalDate,
        nowMillis: Long,
    ) = database.withTransaction {
        val progress = requireNotNull(dao.progress(itemId))
        dao.updateProgressState(
            itemId = itemId,
            status = LearningStatus.REVIEWING,
            queueOrder = null,
            intervalIndex = 5,
            dueEpochDay = today.plusDays(30).toEpochDay(),
            learnedEpochDay = progress.learnedEpochDay ?: today.toEpochDay(),
            masteredEpochDay = null,
            updatedAtMillis = nowMillis,
        )
    }

    suspend fun completeReviewItem(
        sessionId: String,
        itemId: String,
        outcome: ReviewOutcome,
        today: LocalDate,
        nowMillis: Long,
    ) = database.withTransaction {
        val progress = requireNotNull(dao.progress(itemId))
        val currentIndex = requireNotNull(progress.intervalIndex)
        when (val schedule = nextReviewSchedule(currentIndex, outcome, today)) {
            is ReviewSchedule.Scheduled -> dao.updateProgressState(
                itemId = itemId,
                status = LearningStatus.REVIEWING,
                queueOrder = null,
                intervalIndex = schedule.intervalIndex,
                dueEpochDay = schedule.dueDate.toEpochDay(),
                learnedEpochDay = progress.learnedEpochDay,
                masteredEpochDay = null,
                updatedAtMillis = nowMillis,
            )

            ReviewSchedule.Mastered -> dao.updateProgressState(
                itemId = itemId,
                status = LearningStatus.MASTERED,
                queueOrder = null,
                intervalIndex = null,
                dueEpochDay = null,
                learnedEpochDay = progress.learnedEpochDay,
                masteredEpochDay = today.toEpochDay(),
                updatedAtMillis = nowMillis,
            )
        }
        completeSessionItem(sessionId, itemId, nowMillis)
    }

    suspend fun deferNewItem(sessionId: String, itemId: String, nowMillis: Long) =
        database.withTransaction {
            val queueOrder = dao.maxQueueOrder() + 1
            dao.updateProgressState(
                itemId = itemId,
                status = LearningStatus.QUEUED,
                queueOrder = queueOrder,
                intervalIndex = null,
                dueEpochDay = null,
                learnedEpochDay = null,
                masteredEpochDay = null,
                updatedAtMillis = nowMillis,
            )
            dao.setSessionItemState(sessionId, itemId, SessionItemState.DEFERRED, nowMillis)
            completeSessionIfFinished(sessionId, nowMillis)
        }

    suspend fun deferReviewItem(sessionId: String, itemId: String, nowMillis: Long) =
        database.withTransaction {
            dao.setSessionItemState(sessionId, itemId, SessionItemState.DEFERRED, nowMillis)
            completeSessionIfFinished(sessionId, nowMillis)
        }

    suspend fun excludeItem(itemId: String, nowMillis: Long) =
        dao.setExcluded(itemId, nowMillis, nowMillis)

    suspend fun excludeSessionItem(sessionId: String, itemId: String, nowMillis: Long) =
        database.withTransaction {
            val session = requireNotNull(dao.studySession(sessionId))
            val progress = requireNotNull(dao.progress(itemId))
            if (session.type == SessionType.NEW) {
                dao.updateProgress(
                    progress.copy(
                        status = LearningStatus.QUEUED,
                        queueOrder = dao.maxQueueOrder() + 1,
                        excludedAtMillis = nowMillis,
                        updatedAtMillis = nowMillis,
                    ),
                )
            } else {
                dao.setExcluded(itemId, nowMillis, nowMillis)
            }
            dao.setSessionItemState(sessionId, itemId, SessionItemState.DEFERRED, nowMillis)
            completeSessionIfFinished(sessionId, nowMillis)
        }

    suspend fun restoreExcludedItem(itemId: String, nowMillis: Long) =
        database.withTransaction {
            val progress = requireNotNull(dao.progress(itemId))
            dao.updateProgress(
                progress.copy(
                    queueOrder = if (progress.status == LearningStatus.QUEUED) {
                        dao.maxQueueOrder() + 1
                    } else {
                        progress.queueOrder
                    },
                    excludedAtMillis = null,
                    updatedAtMillis = nowMillis,
                ),
            )
        }

    suspend fun addErrorNote(itemId: String, note: String, nowMillis: Long) {
        val text = note.trim()
        require(text.isNotEmpty())
        dao.insertErrorNote(
            ErrorNoteEntity(
                id = newId(),
                itemId = itemId,
                category = ErrorNoteCategory.MEANING,
                note = text,
                createdAtMillis = nowMillis,
                resolvedAtMillis = null,
            ),
        )
    }

    suspend fun expireOldSessions(today: LocalDate, nowMillis: Long) = database.withTransaction {
        dao.expiredSessionCandidates(today.toEpochDay()).forEach { session ->
            if (session.type == SessionType.NEW) {
                var nextQueueOrder = dao.maxQueueOrder() + 1
                dao.unfinishedNewItemIds(session.id).forEach { itemId ->
                    dao.updateProgressState(
                        itemId = itemId,
                        status = LearningStatus.QUEUED,
                        queueOrder = nextQueueOrder++,
                        intervalIndex = null,
                        dueEpochDay = null,
                        learnedEpochDay = null,
                        masteredEpochDay = null,
                        updatedAtMillis = nowMillis,
                    )
                }
            }
            dao.setSessionStatus(session.id, SessionStatus.EXPIRED, null)
        }
    }

    private suspend fun createSession(
        type: SessionType,
        today: LocalDate,
        items: List<LearningItemEntity>,
        nowMillis: Long,
        initialStep: suspend (LearningItemEntity) -> LearningStep,
    ): SessionStartResult.Started {
        val session = StudySessionEntity(
            id = newId(),
            epochDay = today.toEpochDay(),
            type = type,
            status = SessionStatus.ACTIVE,
            goalCount = items.size,
            startedAtMillis = nowMillis,
            completedAtMillis = null,
        )
        dao.insertStudySession(session)
        val sessionItems = items.mapIndexed { index, item ->
            val progress = requireNotNull(dao.progress(item.id))
            val step = initialStep(item)
            SessionItemEntity(
                sessionId = session.id,
                itemId = item.id,
                queueOrder = index.toLong(),
                state = SessionItemState.PENDING,
                phase = step.phase,
                retryPhase = null,
                knownPath = false,
                phaseFailureCount = 0,
                hadInitialReviewError = false,
                reviewIntervalAtStart = if (type == SessionType.REVIEW) progress.intervalIndex else null,
                lastSubmittedAnswer = null,
                updatedAtMillis = nowMillis,
            )
        }
        dao.insertSessionItems(sessionItems)
        if (type == SessionType.NEW) {
            items.forEach { item ->
                dao.updateProgressState(
                    itemId = item.id,
                    status = LearningStatus.LEARNING,
                    queueOrder = null,
                    intervalIndex = null,
                    dueEpochDay = null,
                    learnedEpochDay = null,
                    masteredEpochDay = null,
                    updatedAtMillis = nowMillis,
                )
            }
        }
        return SessionStartResult.Started(session, sessionItems)
    }

    private suspend fun completeSessionItem(sessionId: String, itemId: String, nowMillis: Long) {
        val item = requireNotNull(dao.sessionItem(sessionId, itemId))
        dao.updateSessionItem(
            item.copy(
                state = SessionItemState.COMPLETED,
                phase = LearningPhase.DONE,
                retryPhase = null,
                lastSubmittedAnswer = null,
                updatedAtMillis = nowMillis,
            ),
        )
        completeSessionIfFinished(sessionId, nowMillis)
    }

    private suspend fun completeSessionIfFinished(sessionId: String, nowMillis: Long) {
        if (dao.unfinishedSessionItemCount(sessionId) == 0) {
            dao.setSessionStatus(sessionId, SessionStatus.COMPLETED, nowMillis)
        }
    }

    private companion object {
        const val SUPPLEMENTARY_PERCENT = 20
    }
}

sealed interface SessionStartResult {
    data class Started(
        val session: StudySessionEntity,
        val items: List<SessionItemEntity>,
    ) : SessionStartResult

    data class Existing(val session: StudySessionEntity) : SessionStartResult
    data class Locked(val dueReviewCount: Int) : SessionStartResult
    data object Empty : SessionStartResult
}

private data class StoredItem(
    val item: LearningItemEntity,
    val meanings: List<AuxiliaryMeaningEntity>,
)

private fun LearningItemDraft.toStoredItem(
    id: String,
    generationBatchId: String?,
    nowMillis: Long,
): StoredItem {
    val normalizedExpression = normalizeEnglish(expression)
    val normalizedMeaning = targetMeaningKo.trim().replace(Regex("\\s+"), " ")
    val contentKey = "$normalizedExpression|${partOfSpeech.name}|$normalizedMeaning"
    return StoredItem(
        item = LearningItemEntity(
            id = id,
            contentKey = contentKey,
            expression = expression.trim(),
            normalizedExpression = normalizedExpression,
            baseForm = baseForm?.trim(),
            itemType = itemType,
            partOfSpeech = partOfSpeech,
            targetMeaningKo = targetMeaningKo.trim(),
            phonetic = phonetic?.trim(),
            exampleSentence = exampleSentence.trim(),
            exampleTranslationKo = exampleTranslationKo.trim(),
            exampleTargetForm = exampleTargetForm.trim(),
            topic = topic.trim(),
            difficulty = difficulty,
            meaningSourceName = meaningSourceName,
            meaningSourceUrl = meaningSourceUrl,
            meaningLicenseName = meaningLicenseName,
            meaningLicenseUrl = meaningLicenseUrl,
            exampleSourceName = exampleSourceName,
            exampleSourceUrl = exampleSourceUrl,
            exampleLicenseName = exampleLicenseName,
            exampleLicenseUrl = exampleLicenseUrl,
            generationBatchId = generationBatchId,
            createdAtMillis = nowMillis,
            updatedAtMillis = nowMillis,
        ),
        meanings = auxiliaryMeaningsKo.mapIndexed { index, meaning ->
            AuxiliaryMeaningEntity(id, index, meaning.trim())
        },
    )
}

private fun validateDraft(draft: LearningItemDraft) {
    require(draft.expression.isNotBlank())
    require(draft.targetMeaningKo.isNotBlank())
    require(draft.auxiliaryMeaningsKo.size <= 3)
    require(draft.exampleSentence.isNotBlank())
    require(draft.exampleTranslationKo.isNotBlank())
    require(draft.exampleTargetForm.isNotBlank())
    require(draft.topic.isNotBlank())
}
