package com.yoon778.lexiloop.domain.recommendation

data class TopicWeight(
    val topicId: String,
    val name: String,
    val weightPercent: Int,
)

data class TopicAllocation(
    val topicId: String,
    val name: String,
    val count: Int,
)

fun allocateTopicCounts(
    topics: List<TopicWeight>,
    requestedCount: Int,
): List<TopicAllocation> {
    require(topics.size in 1..5) { "Topic count must be in 1..5" }
    require(topics.sumOf(TopicWeight::weightPercent) == 100) { "Topic weights must sum to 100" }
    require(topics.all { it.weightPercent in 1..100 }) { "Topic weights must be in 1..100" }
    require(topics.map { it.topicId }.distinct().size == topics.size) { "Topic IDs must be unique" }
    require(requestedCount >= topics.size) { "Each topic must receive at least one item" }

    val remaining = requestedCount - topics.size
    val weighted = topics.mapIndexed { index, topic ->
        val exact = remaining * topic.weightPercent / 100.0
        AllocationWork(index, topic, exact.toInt(), exact - exact.toInt())
    }.toMutableList()

    var unassigned = remaining - weighted.sumOf(AllocationWork::baseCount)
    weighted
        .sortedWith(compareByDescending<AllocationWork> { it.remainder }.thenBy { it.index })
        .forEach { work ->
            if (unassigned > 0) {
                weighted[work.index] = work.copy(baseCount = work.baseCount + 1)
                unassigned--
            }
        }

    return weighted.map { work ->
        TopicAllocation(
            topicId = work.topic.topicId,
            name = work.topic.name,
            count = work.baseCount + 1,
        )
    }
}

private data class AllocationWork(
    val index: Int,
    val topic: TopicWeight,
    val baseCount: Int,
    val remainder: Double,
)
