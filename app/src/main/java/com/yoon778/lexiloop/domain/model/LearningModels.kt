package com.yoon778.lexiloop.domain.model

enum class LearningStatus {
    QUEUED,
    LEARNING,
    REVIEWING,
    MASTERED,
}

enum class LearningPhase {
    CARD,
    EN_TO_KO,
    KO_TO_EN,
    SPELLING,
    SENTENCE,
    CORRECTION,
    DONE,
}

enum class SelfAssessment {
    KNOW,
    UNSURE,
    NEW,
}

enum class ReviewOutcome {
    PASS,
    RECOVERED,
    FAILED,
}

enum class SessionType {
    NEW,
    REVIEW,
}

enum class SessionStatus {
    ACTIVE,
    COMPLETED,
    EXPIRED,
}

enum class SessionItemState {
    PENDING,
    ACTIVE,
    COMPLETED,
    DEFERRED,
}

enum class Difficulty {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED,
}

enum class ItemType {
    WORD,
    IDIOM,
    PHRASAL_VERB,
    TECH_TERM,
    EXPRESSION,
}

enum class PartOfSpeech {
    NOUN,
    VERB,
    ADJECTIVE,
    ADVERB,
    PREPOSITION,
    CONJUNCTION,
    PRONOUN,
    DETERMINER,
    INTERJECTION,
    PHRASE,
    OTHER,
}
