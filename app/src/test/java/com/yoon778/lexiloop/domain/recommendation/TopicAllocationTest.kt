package com.yoon778.lexiloop.domain.recommendation

import org.junit.Assert.assertEquals
import org.junit.Test

class TopicAllocationTest {
    @Test
    fun `equal weights split requested items exactly`() {
        val result = allocateTopicCounts(
            listOf(
                TopicWeight("daily", "일상", 50),
                TopicWeight("dev", "개발", 50),
            ),
            requestedCount = 20,
        )

        assertEquals(listOf(10, 10), result.map(TopicAllocation::count))
    }

    @Test
    fun `small topics still receive one item`() {
        val result = allocateTopicCounts(
            listOf(
                TopicWeight("small", "작은 분야", 1),
                TopicWeight("main", "주요 분야", 99),
            ),
            requestedCount = 50,
        )

        assertEquals(listOf(1, 49), result.map(TopicAllocation::count))
        assertEquals(50, result.sumOf(TopicAllocation::count))
    }

    @Test
    fun `largest remainder uses original order for ties`() {
        val result = allocateTopicCounts(
            listOf(
                TopicWeight("a", "A", 34),
                TopicWeight("b", "B", 33),
                TopicWeight("c", "C", 33),
            ),
            requestedCount = 10,
        )

        assertEquals(listOf(4, 3, 3), result.map(TopicAllocation::count))
    }
}
