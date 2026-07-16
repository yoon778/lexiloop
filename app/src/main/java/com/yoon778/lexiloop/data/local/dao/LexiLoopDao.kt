package com.yoon778.lexiloop.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.yoon778.lexiloop.data.local.entity.AuxiliaryMeaningEntity
import com.yoon778.lexiloop.data.local.entity.ErrorNoteEntity
import com.yoon778.lexiloop.data.local.entity.LearningItemEntity
import com.yoon778.lexiloop.data.local.entity.LearningProgressEntity
import com.yoon778.lexiloop.data.local.entity.SessionItemEntity
import com.yoon778.lexiloop.data.local.entity.StudySessionEntity
import com.yoon778.lexiloop.domain.model.LearningStatus
import com.yoon778.lexiloop.domain.model.SessionItemState
import com.yoon778.lexiloop.domain.model.SessionStatus
import com.yoon778.lexiloop.domain.model.SessionType
import kotlinx.coroutines.flow.Flow

@Dao
interface LexiLoopDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertLearningItems(items: List<LearningItemEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAuxiliaryMeanings(meanings: List<AuxiliaryMeaningEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertProgresses(progresses: List<LearningProgressEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertStudySession(session: StudySessionEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSessionItems(items: List<SessionItemEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertErrorNote(note: ErrorNoteEntity)

    @Update
    suspend fun updateProgress(progress: LearningProgressEntity)

    @Update
    suspend fun updateSessionItem(item: SessionItemEntity)

    @Update
    suspend fun updateStudySession(session: StudySessionEntity)

    @Query("SELECT * FROM learning_items WHERE id = :itemId")
    suspend fun learningItem(itemId: String): LearningItemEntity?

    @Query("SELECT * FROM learning_progress WHERE itemId = :itemId")
    suspend fun progress(itemId: String): LearningProgressEntity?

    @Query("SELECT * FROM study_sessions WHERE id = :sessionId")
    suspend fun studySession(sessionId: String): StudySessionEntity?

    @Query("SELECT * FROM session_items WHERE sessionId = :sessionId AND itemId = :itemId")
    suspend fun sessionItem(sessionId: String, itemId: String): SessionItemEntity?

    @Query(
        "SELECT * FROM session_items WHERE sessionId = :sessionId " +
            "AND state NOT IN ('COMPLETED', 'DEFERRED') ORDER BY queueOrder LIMIT 1",
    )
    suspend fun currentSessionItem(sessionId: String): SessionItemEntity?

    @Query("SELECT COUNT(*) FROM session_items WHERE sessionId = :sessionId AND state = 'COMPLETED'")
    suspend fun completedSessionItemCount(sessionId: String): Int

    @Query("SELECT * FROM auxiliary_meanings WHERE itemId = :itemId ORDER BY sortOrder")
    suspend fun auxiliaryMeanings(itemId: String): List<AuxiliaryMeaningEntity>

    @Query(
        """
        SELECT * FROM study_sessions
        WHERE epochDay = :epochDay AND type = :type AND status = 'ACTIVE'
        LIMIT 1
        """,
    )
    suspend fun activeSession(epochDay: Long, type: SessionType): StudySessionEntity?

    @Query("SELECT * FROM study_sessions WHERE epochDay = :epochDay AND type = :type LIMIT 1")
    suspend fun sessionForDay(epochDay: Long, type: SessionType): StudySessionEntity?

    @Query(
        """
        SELECT learning_items.* FROM learning_items
        JOIN learning_progress ON learning_progress.itemId = learning_items.id
        WHERE learning_progress.status = 'QUEUED'
          AND learning_progress.excludedAtMillis IS NULL
        ORDER BY CASE WHEN instr(trim(learning_items.expression), ' ') = 0 THEN 0 ELSE 1 END,
                 learning_progress.queueOrder, learning_items.id
        LIMIT :limit
        """,
    )
    suspend fun queuedItems(limit: Int): List<LearningItemEntity>

    @Query(
        """
        SELECT learning_items.* FROM learning_items
        JOIN learning_progress ON learning_progress.itemId = learning_items.id
        WHERE learning_progress.status = 'QUEUED'
          AND learning_progress.excludedAtMillis IS NULL
          AND instr(trim(learning_items.expression), ' ') = 0
        ORDER BY learning_progress.queueOrder, learning_items.id
        LIMIT :limit
        """,
    )
    suspend fun queuedCoreItems(limit: Int): List<LearningItemEntity>

    @Query(
        """
        SELECT learning_items.* FROM learning_items
        JOIN learning_progress ON learning_progress.itemId = learning_items.id
        WHERE learning_progress.status = 'QUEUED'
          AND learning_progress.excludedAtMillis IS NULL
          AND instr(trim(learning_items.expression), ' ') > 0
        ORDER BY learning_progress.queueOrder, learning_items.id
        LIMIT :limit
        """,
    )
    suspend fun queuedSupplementaryItems(limit: Int): List<LearningItemEntity>

    @Query(
        """
        SELECT learning_items.* FROM learning_items
        JOIN learning_progress ON learning_progress.itemId = learning_items.id
        WHERE learning_progress.status = 'REVIEWING'
          AND learning_progress.excludedAtMillis IS NULL
          AND learning_progress.dueEpochDay <= :todayEpochDay
        ORDER BY learning_progress.dueEpochDay, learning_items.id
        """,
    )
    suspend fun dueItems(todayEpochDay: Long): List<LearningItemEntity>

    @Query(
        """
        SELECT COUNT(*) FROM learning_progress
        WHERE status = 'REVIEWING'
          AND excludedAtMillis IS NULL
          AND dueEpochDay <= :todayEpochDay
        """,
    )
    suspend fun dueReviewCount(todayEpochDay: Long): Int

    @Query(
        """
        SELECT COUNT(*) FROM learning_progress
        WHERE status IN ('REVIEWING', 'MASTERED')
        """,
    )
    fun learnedTotal(): Flow<Int>

    @Query(
        """
        SELECT COUNT(*) FROM learning_progress
        WHERE status = 'QUEUED' AND excludedAtMillis IS NULL
        """,
    )
    fun queuedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM learning_items WHERE generationBatchId IS NOT NULL")
    suspend fun generatedItemCount(): Int

    @Query("SELECT epochDay FROM study_sessions WHERE status = 'COMPLETED' ORDER BY epochDay DESC")
    suspend fun completedSessionEpochDays(): List<Long>

    @Query(
        """
        SELECT learning_items.id AS id, learning_items.expression AS expression,
               learning_items.targetMeaningKo AS targetMeaningKo,
               learning_progress.status AS status,
               learning_progress.excludedAtMillis AS excludedAtMillis
        FROM learning_items
        JOIN learning_progress ON learning_progress.itemId = learning_items.id
        WHERE (:status IS NULL OR learning_progress.status = :status)
          AND (:query = '' OR learning_items.normalizedExpression LIKE '%' || :query || '%'
               OR learning_items.targetMeaningKo LIKE '%' || :query || '%')
        ORDER BY learning_items.normalizedExpression
        LIMIT 500
        """,
    )
    suspend fun managedWords(query: String, status: LearningStatus?): List<ManagedWordRow>

    @Query("SELECT * FROM learning_items WHERE id != :itemId ORDER BY id LIMIT :limit")
    suspend fun distractorItems(itemId: String, limit: Int): List<LearningItemEntity>

    @Query(
        "SELECT expression, partOfSpeech, targetMeaningKo FROM learning_items " +
            "ORDER BY createdAtMillis DESC LIMIT :limit",
    )
    suspend fun blockedCards(limit: Int): List<BlockedCardRow>

    @Query(
        """
        UPDATE learning_progress
        SET status = :status, queueOrder = :queueOrder, intervalIndex = :intervalIndex,
            dueEpochDay = :dueEpochDay, learnedEpochDay = :learnedEpochDay,
            masteredEpochDay = :masteredEpochDay, updatedAtMillis = :updatedAtMillis
        WHERE itemId = :itemId
        """,
    )
    suspend fun updateProgressState(
        itemId: String,
        status: LearningStatus,
        queueOrder: Long?,
        intervalIndex: Int?,
        dueEpochDay: Long?,
        learnedEpochDay: Long?,
        masteredEpochDay: Long?,
        updatedAtMillis: Long,
    )

    @Query("UPDATE learning_progress SET excludedAtMillis = :excludedAtMillis, updatedAtMillis = :updatedAtMillis WHERE itemId = :itemId")
    suspend fun setExcluded(itemId: String, excludedAtMillis: Long?, updatedAtMillis: Long)

    @Query("SELECT COALESCE(MAX(queueOrder), 0) FROM learning_progress")
    suspend fun maxQueueOrder(): Long

    @Query("SELECT COUNT(*) FROM session_items WHERE sessionId = :sessionId AND state NOT IN ('COMPLETED', 'DEFERRED')")
    suspend fun unfinishedSessionItemCount(sessionId: String): Int

    @Query("SELECT * FROM session_items WHERE sessionId = :sessionId ORDER BY queueOrder")
    fun observeSessionItems(sessionId: String): Flow<List<SessionItemEntity>>

    @Query("SELECT * FROM study_sessions WHERE status = 'ACTIVE' AND epochDay < :todayEpochDay")
    suspend fun expiredSessionCandidates(todayEpochDay: Long): List<StudySessionEntity>

    @Query(
        """
        SELECT session_items.itemId FROM session_items
        JOIN study_sessions ON study_sessions.id = session_items.sessionId
        WHERE study_sessions.id = :sessionId
          AND study_sessions.type = 'NEW'
          AND session_items.state NOT IN ('COMPLETED', 'DEFERRED')
        """,
    )
    suspend fun unfinishedNewItemIds(sessionId: String): List<String>

    @Query("UPDATE study_sessions SET status = :status, completedAtMillis = :completedAtMillis WHERE id = :sessionId")
    suspend fun setSessionStatus(sessionId: String, status: SessionStatus, completedAtMillis: Long?)

    @Query("UPDATE session_items SET state = :state, updatedAtMillis = :updatedAtMillis WHERE sessionId = :sessionId AND itemId = :itemId")
    suspend fun setSessionItemState(
        sessionId: String,
        itemId: String,
        state: SessionItemState,
        updatedAtMillis: Long,
    )

    @Query("SELECT contentKey FROM learning_items WHERE contentKey IN (:contentKeys)")
    suspend fun existingContentKeys(contentKeys: List<String>): List<String>

    @Query("DELETE FROM learning_items WHERE id IN (SELECT itemId FROM learning_progress WHERE status = 'QUEUED' AND excludedAtMillis IS NULL)")
    suspend fun deleteAvailableQueuedItems()
}

data class ManagedWordRow(
    val id: String,
    val expression: String,
    val targetMeaningKo: String,
    val status: LearningStatus,
    val excludedAtMillis: Long?,
)

data class BlockedCardRow(
    val expression: String,
    val partOfSpeech: com.yoon778.lexiloop.domain.model.PartOfSpeech,
    val targetMeaningKo: String,
)
