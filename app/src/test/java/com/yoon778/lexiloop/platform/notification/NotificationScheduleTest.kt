package com.yoon778.lexiloop.platform.notification

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class NotificationScheduleTest {
    private val zone = ZoneId.of("Asia/Seoul")

    @Test
    fun `future time uses same day`() {
        val now = ZonedDateTime.of(2026, 7, 15, 19, 59, 30, 0, zone)
        assertEquals(
            ZonedDateTime.of(2026, 7, 15, 20, 0, 0, 0, zone),
            nextNotificationTime(now, 20, 0),
        )
    }

    @Test
    fun `passed time uses next day`() {
        val now = ZonedDateTime.of(2026, 7, 15, 20, 0, 0, 0, zone)
        assertEquals(
            ZonedDateTime.of(2026, 7, 16, 20, 0, 0, 0, zone),
            nextNotificationTime(now, 20, 0),
        )
    }
}
