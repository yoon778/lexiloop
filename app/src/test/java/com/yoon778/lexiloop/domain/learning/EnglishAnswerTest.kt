package com.yoon778.lexiloop.domain.learning

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EnglishAnswerTest {
    @Test
    fun `normalization ignores case and all whitespace`() {
        assertEquals("phrasalverb", normalizeEnglish("  Phrasal \t Verb\n"))
    }

    @Test
    fun `only case and whitespace differences are accepted`() {
        assertTrue(isEnglishAnswerCorrect("Deploy", " de ploy "))
        assertFalse(isEnglishAnswerCorrect("deploy", "deployed"))
        assertFalse(isEnglishAnswerCorrect("don't", "dont"))
    }

    @Test
    fun `hint describes compact expression`() {
        assertEquals(
            AnswerHint('p', 8, "처리하다"),
            answerHint("phase out", "처리하다"),
        )
        assertFalse(shouldShowAnswerHint(1))
        assertTrue(shouldShowAnswerHint(2))
    }
}
