package com.yoon778.lexiloop.domain.learning

import java.util.Locale

private val whitespace = Regex("\\s+")

fun normalizeEnglish(input: String): String =
    input.trim().replace(whitespace, "").lowercase(Locale.ROOT)

fun isEnglishAnswerCorrect(expected: String, actual: String): Boolean =
    normalizeEnglish(expected) == normalizeEnglish(actual)

data class AnswerHint(
    val firstCharacter: Char?,
    val characterCount: Int,
    val targetMeaningKo: String,
)

fun answerHint(expression: String, targetMeaningKo: String): AnswerHint {
    val compact = expression.filterNot(Char::isWhitespace)
    return AnswerHint(
        firstCharacter = compact.firstOrNull(),
        characterCount = compact.length,
        targetMeaningKo = targetMeaningKo,
    )
}

fun shouldShowAnswerHint(failureCount: Int): Boolean = failureCount >= 2
