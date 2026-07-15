package com.yoon778.lexiloop.domain.model

data class LearningItemDraft(
    val expression: String,
    val baseForm: String?,
    val itemType: ItemType,
    val partOfSpeech: PartOfSpeech,
    val targetMeaningKo: String,
    val auxiliaryMeaningsKo: List<String>,
    val phonetic: String?,
    val exampleSentence: String,
    val exampleTranslationKo: String,
    val exampleTargetForm: String,
    val topic: String,
    val difficulty: Difficulty,
    val meaningSourceName: String?,
    val meaningSourceUrl: String?,
    val meaningLicenseName: String?,
    val meaningLicenseUrl: String?,
    val exampleSourceName: String?,
    val exampleSourceUrl: String?,
    val exampleLicenseName: String?,
    val exampleLicenseUrl: String?,
)
