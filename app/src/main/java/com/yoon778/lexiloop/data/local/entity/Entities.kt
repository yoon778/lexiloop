package com.yoon778.lexiloop.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.yoon778.lexiloop.domain.model.Difficulty
import com.yoon778.lexiloop.domain.model.ItemType
import com.yoon778.lexiloop.domain.model.LearningPhase
import com.yoon778.lexiloop.domain.model.LearningStatus
import com.yoon778.lexiloop.domain.model.PartOfSpeech
import com.yoon778.lexiloop.domain.model.SessionItemState
import com.yoon778.lexiloop.domain.model.SessionStatus
import com.yoon778.lexiloop.domain.model.SessionType

@Entity(
    tableName = "learning_items",
    indices = [
        Index(value = ["contentKey"], unique = true),
        Index(value = ["normalizedExpression"]),
        Index(value = ["topic", "difficulty"]),
    ],
)
data class LearningItemEntity(
    @androidx.room.PrimaryKey val id: String,
    val contentKey: String,
    val expression: String,
    val normalizedExpression: String,
    val baseForm: String?,
    val itemType: ItemType,
    val partOfSpeech: PartOfSpeech,
    val targetMeaningKo: String,
    val phonetic: String?,
    val exampleSentence: String,
    val exampleTranslationKo: String,
    val exampleTargetForm: String,
    val topic: String,
    val difficulty: Difficulty,
    val meaningSourceName: String?,
    val meaningSourceUrl: String?,
    val meaningLicenseName: String?,
    val meaningLicenseUrl: String?,
    val exampleSourceName: String?,
    val exampleSourceUrl: String?,
    val exampleLicenseName: String?,
    val exampleLicenseUrl: String?,
    val generationBatchId: String?,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
)

@Entity(
    tableName = "auxiliary_meanings",
    primaryKeys = ["itemId", "sortOrder"],
    foreignKeys = [
        ForeignKey(
            entity = LearningItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class AuxiliaryMeaningEntity(
    val itemId: String,
    val sortOrder: Int,
    val meaningKo: String,
)

@Entity(
    tableName = "learning_progress",
    foreignKeys = [
        ForeignKey(
            entity = LearningItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["status", "excludedAtMillis", "queueOrder"]),
        Index(value = ["status", "excludedAtMillis", "dueEpochDay", "itemId"]),
    ],
)
data class LearningProgressEntity(
    @androidx.room.PrimaryKey val itemId: String,
    val status: LearningStatus,
    val queueOrder: Long?,
    val intervalIndex: Int?,
    val dueEpochDay: Long?,
    val learnedEpochDay: Long?,
    val masteredEpochDay: Long?,
    val excludedAtMillis: Long?,
    val updatedAtMillis: Long,
)

@Entity(
    tableName = "study_sessions",
    indices = [Index(value = ["epochDay", "type"], unique = true)],
)
data class StudySessionEntity(
    @androidx.room.PrimaryKey val id: String,
    val epochDay: Long,
    val type: SessionType,
    val status: SessionStatus,
    val goalCount: Int,
    val startedAtMillis: Long,
    val completedAtMillis: Long?,
)

@Entity(
    tableName = "session_items",
    primaryKeys = ["sessionId", "itemId"],
    foreignKeys = [
        ForeignKey(
            entity = StudySessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = LearningItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
        ),
    ],
    indices = [
        Index(value = ["itemId"]),
        Index(value = ["sessionId", "state", "queueOrder"]),
    ],
)
data class SessionItemEntity(
    val sessionId: String,
    val itemId: String,
    val queueOrder: Long,
    val state: SessionItemState,
    val phase: LearningPhase,
    val retryPhase: LearningPhase?,
    val knownPath: Boolean,
    val phaseFailureCount: Int,
    val hadInitialReviewError: Boolean,
    val reviewIntervalAtStart: Int?,
    val lastSubmittedAnswer: String?,
    val updatedAtMillis: Long,
)

@Entity(
    tableName = "error_notes",
    foreignKeys = [
        ForeignKey(
            entity = LearningItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["itemId", "createdAtMillis"])],
)
data class ErrorNoteEntity(
    @androidx.room.PrimaryKey val id: String,
    val itemId: String,
    val category: ErrorNoteCategory,
    val note: String,
    val createdAtMillis: Long,
    val resolvedAtMillis: Long?,
)

enum class ErrorNoteCategory {
    MEANING,
    EXAMPLE,
}
