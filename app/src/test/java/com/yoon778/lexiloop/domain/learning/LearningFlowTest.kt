package com.yoon778.lexiloop.domain.learning

import com.yoon778.lexiloop.domain.model.LearningPhase
import com.yoon778.lexiloop.domain.model.SelfAssessment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LearningFlowTest {
    @Test
    fun `known path skips choices and completes after spelling and sentence`() {
        val spelling = nextLearningStep(
            LearningStep(LearningPhase.CARD),
            LearningEvent.Assessed(SelfAssessment.KNOW),
        )
        val sentence = nextLearningStep(spelling, LearningEvent.Answered(correct = true))
        val done = nextLearningStep(sentence, LearningEvent.Answered(correct = true))

        assertEquals(LearningPhase.SPELLING, spelling.phase)
        assertTrue(spelling.knownPath)
        assertEquals(LearningPhase.SENTENCE, sentence.phase)
        assertEquals(LearningPhase.DONE, done.phase)
        assertTrue(done.knownPath)
    }

    @Test
    fun `known path failure shows correction then restarts general flow`() {
        val correction = nextLearningStep(
            LearningStep(LearningPhase.SPELLING, knownPath = true),
            LearningEvent.Answered(correct = false),
        )
        val retry = nextLearningStep(correction, LearningEvent.CorrectionAcknowledged)

        assertEquals(LearningPhase.CORRECTION, correction.phase)
        assertEquals(LearningPhase.EN_TO_KO, correction.retryPhase)
        assertEquals(LearningPhase.EN_TO_KO, retry.phase)
        assertFalse(retry.knownPath)
    }

    @Test
    fun `general wrong answer retries from one easier phase`() {
        val cases = mapOf(
            LearningPhase.EN_TO_KO to LearningPhase.EN_TO_KO,
            LearningPhase.KO_TO_EN to LearningPhase.EN_TO_KO,
            LearningPhase.SPELLING to LearningPhase.KO_TO_EN,
            LearningPhase.SENTENCE to LearningPhase.SPELLING,
        )

        cases.forEach { (phase, expectedRetry) ->
            val correction = nextLearningStep(
                LearningStep(phase),
                LearningEvent.Answered(correct = false),
            )
            assertEquals(expectedRetry, correction.retryPhase)
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `self assessment is rejected outside card`() {
        nextLearningStep(
            LearningStep(LearningPhase.SPELLING),
            LearningEvent.Assessed(SelfAssessment.KNOW),
        )
    }
}
