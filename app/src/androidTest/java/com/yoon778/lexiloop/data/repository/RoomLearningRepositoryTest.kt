package com.yoon778.lexiloop.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.yoon778.lexiloop.data.local.LexiLoopDatabase
import com.yoon778.lexiloop.domain.model.Difficulty
import com.yoon778.lexiloop.domain.model.ItemType
import com.yoon778.lexiloop.domain.model.LearningItemDraft
import com.yoon778.lexiloop.domain.model.LearningStatus
import com.yoon778.lexiloop.domain.model.PartOfSpeech
import com.yoon778.lexiloop.domain.model.ReviewOutcome
import com.yoon778.lexiloop.domain.model.SessionStatus
import java.time.LocalDate
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomLearningRepositoryTest {
    private lateinit var database: LexiLoopDatabase
    private lateinit var repository: RoomLearningRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, LexiLoopDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        var nextId = 0
        repository = RoomLearningRepository(database) { "id-${nextId++}" }
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun batchAndNewSessionTransitionsAreAtomic() = runTest {
        repository.insertBatch(
            drafts = listOf(draft("deploy"), draft("grocery")),
            generationBatchId = "batch",
            nowMillis = 100,
        )

        val started = repository.startNewSession(
            today = LocalDate.of(2026, 7, 15),
            goalCount = 2,
            nowMillis = 200,
        ) as SessionStartResult.Started

        val firstId = started.items[0].itemId
        val secondId = started.items[1].itemId
        assertEquals(LearningStatus.LEARNING, database.dao().progress(firstId)?.status)

        repository.completeNewItem(
            sessionId = started.session.id,
            itemId = firstId,
            knownPathPassed = false,
            today = LocalDate.of(2026, 7, 15),
            nowMillis = 300,
        )
        repository.deferNewItem(started.session.id, secondId, nowMillis = 400)

        val firstProgress = database.dao().progress(firstId)
        assertEquals(LearningStatus.REVIEWING, firstProgress?.status)
        assertEquals(1, firstProgress?.intervalIndex)
        assertEquals(LocalDate.of(2026, 7, 16).toEpochDay(), firstProgress?.dueEpochDay)
        assertEquals(LearningStatus.QUEUED, database.dao().progress(secondId)?.status)
        assertEquals(SessionStatus.COMPLETED, database.dao().studySession(started.session.id)?.status)
    }

    @Test
    fun reviewPassAdvancesSchedule() = runTest {
        repository.insertBatch(listOf(draft("maintain")), "batch", nowMillis = 100)
        val newSession = repository.startNewSession(
            LocalDate.of(2026, 7, 15),
            goalCount = 1,
            nowMillis = 200,
        ) as SessionStartResult.Started
        val itemId = newSession.items.single().itemId
        repository.completeNewItem(
            newSession.session.id,
            itemId,
            knownPathPassed = false,
            LocalDate.of(2026, 7, 15),
            nowMillis = 300,
        )

        val reviewSession = repository.startReviewSession(
            LocalDate.of(2026, 7, 16),
            nowMillis = 400,
        ) as SessionStartResult.Started
        repository.completeReviewItem(
            reviewSession.session.id,
            itemId,
            ReviewOutcome.PASS,
            LocalDate.of(2026, 7, 16),
            nowMillis = 500,
        )

        val progress = database.dao().progress(itemId)
        assertEquals(2, progress?.intervalIndex)
        assertEquals(LocalDate.of(2026, 7, 19).toEpochDay(), progress?.dueEpochDay)
    }

    @Test
    fun duplicateBatchLeavesDatabaseEmpty() = runTest {
        val duplicate = draft("deploy")
        val failure = runCatching {
            repository.insertBatch(listOf(duplicate, duplicate), "batch", nowMillis = 100)
        }

        assertTrue(failure.isFailure)
        assertTrue(database.dao().queuedItems(10).isEmpty())
    }

    @Test
    fun exclusionDuringNewSessionDefersAndRestoresQueue() = runTest {
        repository.insertBatch(listOf(draft("exclude")), "batch", nowMillis = 100)
        val session = repository.startNewSession(
            LocalDate.of(2026, 7, 15),
            goalCount = 1,
            nowMillis = 200,
        ) as SessionStartResult.Started
        val itemId = session.items.single().itemId

        repository.excludeSessionItem(session.session.id, itemId, nowMillis = 300)
        val excluded = database.dao().progress(itemId)
        assertEquals(LearningStatus.QUEUED, excluded?.status)
        assertEquals(300L, excluded?.excludedAtMillis)
        assertEquals(SessionStatus.COMPLETED, database.dao().studySession(session.session.id)?.status)

        repository.restoreExcludedItem(itemId, nowMillis = 400)
        val restored = database.dao().progress(itemId)
        assertEquals(null, restored?.excludedAtMillis)
        assertTrue(restored?.queueOrder != null)
    }

    private fun draft(expression: String) = LearningItemDraft(
        expression = expression,
        baseForm = null,
        itemType = ItemType.WORD,
        partOfSpeech = PartOfSpeech.VERB,
        targetMeaningKo = "$expression 뜻",
        auxiliaryMeaningsKo = emptyList(),
        phonetic = null,
        exampleSentence = "We $expression it.",
        exampleTranslationKo = "예문 번역",
        exampleTargetForm = expression,
        topic = "일상",
        difficulty = Difficulty.INTERMEDIATE,
        meaningSourceName = null,
        meaningSourceUrl = null,
        meaningLicenseName = null,
        meaningLicenseUrl = null,
        exampleSourceName = "manual",
        exampleSourceUrl = null,
        exampleLicenseName = null,
        exampleLicenseUrl = null,
    )
}
