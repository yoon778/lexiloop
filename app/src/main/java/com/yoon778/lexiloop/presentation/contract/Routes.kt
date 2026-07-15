package com.yoon778.lexiloop.presentation.contract

import kotlinx.serialization.Serializable

sealed interface AppRoute {
    @Serializable data object OnboardingPurpose : AppRoute
    @Serializable data object OnboardingAnalysis : AppRoute
    @Serializable data object Diagnosis : AppRoute
    @Serializable data object Home : AppRoute
    @Serializable data object NewOverview : AppRoute
    @Serializable data class Study(val sessionId: String) : AppRoute
    @Serializable data class SessionResult(val sessionId: String) : AppRoute
    @Serializable data object WordManagement : AppRoute
    @Serializable data object Settings : AppRoute
    @Serializable data object DataManagement : AppRoute
}
