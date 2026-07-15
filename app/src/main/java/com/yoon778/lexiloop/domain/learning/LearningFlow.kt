package com.yoon778.lexiloop.domain.learning

import com.yoon778.lexiloop.domain.model.LearningPhase
import com.yoon778.lexiloop.domain.model.SelfAssessment

data class LearningStep(
    val phase: LearningPhase,
    val knownPath: Boolean = false,
    val retryPhase: LearningPhase? = null,
    val phaseFailureCount: Int = 0,
)

sealed interface LearningEvent {
    data class Assessed(val assessment: SelfAssessment) : LearningEvent
    data class Answered(val correct: Boolean) : LearningEvent
    data object CorrectionAcknowledged : LearningEvent
}

fun nextLearningStep(state: LearningStep, event: LearningEvent): LearningStep = when (event) {
    is LearningEvent.Assessed -> onAssessment(state, event.assessment)
    is LearningEvent.Answered -> onAnswer(state, event.correct)
    LearningEvent.CorrectionAcknowledged -> onCorrectionAcknowledged(state)
}

private fun onAssessment(state: LearningStep, assessment: SelfAssessment): LearningStep {
    require(state.phase == LearningPhase.CARD) { "Self assessment is only valid on CARD" }
    return when (assessment) {
        SelfAssessment.KNOW -> LearningStep(LearningPhase.SPELLING, knownPath = true)
        SelfAssessment.UNSURE,
        SelfAssessment.NEW,
        -> LearningStep(LearningPhase.EN_TO_KO)
    }
}

private fun onAnswer(state: LearningStep, correct: Boolean): LearningStep {
    require(state.phase in answerPhases) { "Answer is not valid on ${state.phase}" }
    if (!correct) {
        val retryPhase = if (state.knownPath) {
            LearningPhase.EN_TO_KO
        } else {
            easierPhase(state.phase)
        }
        return LearningStep(
            phase = LearningPhase.CORRECTION,
            knownPath = false,
            retryPhase = retryPhase,
            phaseFailureCount = state.phaseFailureCount + 1,
        )
    }

    val nextPhase = when (state.phase) {
        LearningPhase.EN_TO_KO -> LearningPhase.KO_TO_EN
        LearningPhase.KO_TO_EN -> LearningPhase.SPELLING
        LearningPhase.SPELLING -> LearningPhase.SENTENCE
        LearningPhase.SENTENCE -> LearningPhase.DONE
        else -> error("Unsupported answer phase")
    }
    return LearningStep(
        phase = nextPhase,
        knownPath = state.knownPath,
    )
}

private fun onCorrectionAcknowledged(state: LearningStep): LearningStep {
    require(state.phase == LearningPhase.CORRECTION) {
        "Correction acknowledgement is only valid on CORRECTION"
    }
    return LearningStep(
        phase = requireNotNull(state.retryPhase),
        knownPath = false,
        phaseFailureCount = state.phaseFailureCount,
    )
}

private fun easierPhase(phase: LearningPhase): LearningPhase = when (phase) {
    LearningPhase.EN_TO_KO -> LearningPhase.EN_TO_KO
    LearningPhase.KO_TO_EN -> LearningPhase.EN_TO_KO
    LearningPhase.SPELLING -> LearningPhase.KO_TO_EN
    LearningPhase.SENTENCE -> LearningPhase.SPELLING
    else -> error("No easier phase for $phase")
}

private val answerPhases = setOf(
    LearningPhase.EN_TO_KO,
    LearningPhase.KO_TO_EN,
    LearningPhase.SPELLING,
    LearningPhase.SENTENCE,
)
