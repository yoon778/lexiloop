package com.yoon778.lexiloop.domain.progress

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StudyDayTest {
    @Test
    fun `review completion or new goal qualifies the day`() {
        assertTrue(qualifiesAsStudyDay(12, 12, 20, 3))
        assertTrue(qualifiesAsStudyDay(12, 2, 20, 20))
        assertFalse(qualifiesAsStudyDay(0, 0, 20, 19))
    }

    @Test
    fun `streak remains visible before todays completion`() {
        assertEquals(3, calculateStreak(setOf(10L, 11L, 12L), todayEpochDay = 13L))
        assertEquals(4, calculateStreak(setOf(10L, 11L, 12L, 13L), todayEpochDay = 13L))
        assertEquals(0, calculateStreak(setOf(10L, 11L), todayEpochDay = 13L))
    }
}
