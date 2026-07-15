package com.yoon778.lexiloop.domain.progress

fun qualifiesAsStudyDay(
    scheduledReviewCount: Int,
    completedReviewCount: Int,
    newGoalCount: Int,
    checkedNewCount: Int,
): Boolean {
    require(scheduledReviewCount >= 0 && completedReviewCount >= 0)
    require(newGoalCount >= 0 && checkedNewCount >= 0)

    val reviewsCompleted = scheduledReviewCount > 0 && completedReviewCount >= scheduledReviewCount
    val newGoalCompleted = newGoalCount > 0 && checkedNewCount >= newGoalCount
    return reviewsCompleted || newGoalCompleted
}

fun calculateStreak(completedEpochDays: Set<Long>, todayEpochDay: Long): Int {
    val start = when {
        todayEpochDay in completedEpochDays -> todayEpochDay
        todayEpochDay - 1 in completedEpochDays -> todayEpochDay - 1
        else -> return 0
    }

    var day = start
    var streak = 0
    while (day in completedEpochDays) {
        streak++
        day--
    }
    return streak
}
