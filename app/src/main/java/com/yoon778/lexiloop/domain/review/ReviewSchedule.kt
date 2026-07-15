package com.yoon778.lexiloop.domain.review

import com.yoon778.lexiloop.domain.model.LearningPhase
import com.yoon778.lexiloop.domain.model.ReviewOutcome
import java.time.LocalDate

private val intervalDays = mapOf(
    1 to 1L,
    2 to 3L,
    3 to 7L,
    4 to 14L,
    5 to 30L,
)

sealed interface ReviewSchedule {
    data class Scheduled(
        val intervalIndex: Int,
        val dueDate: LocalDate,
    ) : ReviewSchedule

    data object Mastered : ReviewSchedule
}

fun initialReviewSchedule(knownPathPassed: Boolean, today: LocalDate): ReviewSchedule.Scheduled {
    val index = if (knownPathPassed) 3 else 1
    return ReviewSchedule.Scheduled(index, today.plusDays(daysFor(index)))
}

fun reviewEntryPhase(intervalIndex: Int): LearningPhase {
    requireValidIndex(intervalIndex)
    return if (intervalIndex % 2 == 1) LearningPhase.SPELLING else LearningPhase.SENTENCE
}

fun determineReviewOutcome(
    firstAttemptCorrect: Boolean,
    remediationCompleted: Boolean,
    finalSpellingCorrect: Boolean,
): ReviewOutcome = when {
    firstAttemptCorrect -> ReviewOutcome.PASS
    remediationCompleted && finalSpellingCorrect -> ReviewOutcome.RECOVERED
    else -> ReviewOutcome.FAILED
}

fun nextReviewSchedule(
    currentIndex: Int,
    outcome: ReviewOutcome,
    today: LocalDate,
): ReviewSchedule {
    requireValidIndex(currentIndex)
    if (outcome == ReviewOutcome.PASS && currentIndex == 5) {
        return ReviewSchedule.Mastered
    }

    val nextIndex = when (outcome) {
        ReviewOutcome.PASS -> currentIndex + 1
        ReviewOutcome.RECOVERED -> maxOf(1, currentIndex - 1)
        ReviewOutcome.FAILED -> 1
    }
    return ReviewSchedule.Scheduled(nextIndex, today.plusDays(daysFor(nextIndex)))
}

fun isNewLearningLocked(dueReviewCount: Int): Boolean {
    require(dueReviewCount >= 0) { "Due review count cannot be negative" }
    return dueReviewCount >= 21
}

private fun daysFor(index: Int): Long = requireNotNull(intervalDays[index])

private fun requireValidIndex(index: Int) {
    require(index in 1..5) { "Review interval index must be in 1..5" }
}
