package com.yoon778.lexiloop.domain.review

import com.yoon778.lexiloop.domain.model.LearningPhase
import com.yoon778.lexiloop.domain.model.ReviewOutcome
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewScheduleTest {
    private val today = LocalDate.of(2026, 7, 15)

    @Test
    fun `initial schedules use one day or seven days`() {
        assertEquals(
            ReviewSchedule.Scheduled(1, today.plusDays(1)),
            initialReviewSchedule(knownPathPassed = false, today),
        )
        assertEquals(
            ReviewSchedule.Scheduled(3, today.plusDays(7)),
            initialReviewSchedule(knownPathPassed = true, today),
        )
    }

    @Test
    fun `review entry alternates spelling and sentence`() {
        assertEquals(LearningPhase.SPELLING, reviewEntryPhase(1))
        assertEquals(LearningPhase.SENTENCE, reviewEntryPhase(2))
        assertEquals(LearningPhase.SPELLING, reviewEntryPhase(5))
    }

    @Test
    fun `review outcomes preserve first attempt failure`() {
        assertEquals(
            ReviewOutcome.PASS,
            determineReviewOutcome(true, remediationCompleted = false, finalSpellingCorrect = false),
        )
        assertEquals(
            ReviewOutcome.RECOVERED,
            determineReviewOutcome(false, remediationCompleted = true, finalSpellingCorrect = true),
        )
        assertEquals(
            ReviewOutcome.FAILED,
            determineReviewOutcome(false, remediationCompleted = true, finalSpellingCorrect = false),
        )
    }

    @Test
    fun `pass advances recovered retreats and failure resets`() {
        assertEquals(
            ReviewSchedule.Scheduled(4, today.plusDays(14)),
            nextReviewSchedule(3, ReviewOutcome.PASS, today),
        )
        assertEquals(
            ReviewSchedule.Scheduled(2, today.plusDays(3)),
            nextReviewSchedule(3, ReviewOutcome.RECOVERED, today),
        )
        assertEquals(
            ReviewSchedule.Scheduled(1, today.plusDays(1)),
            nextReviewSchedule(4, ReviewOutcome.FAILED, today),
        )
        assertEquals(
            ReviewSchedule.Mastered,
            nextReviewSchedule(5, ReviewOutcome.PASS, today),
        )
    }

    @Test
    fun `new learning locks at twenty one due reviews`() {
        assertFalse(isNewLearningLocked(20))
        assertTrue(isNewLearningLocked(21))
    }
}
