package com.yoon778.lexiloop.presentation.contract

import com.yoon778.lexiloop.data.settings.RecommendationProfile
import com.yoon778.lexiloop.data.settings.ThemePreference
import com.yoon778.lexiloop.domain.model.Difficulty
import com.yoon778.lexiloop.domain.model.LearningPhase
import com.yoon778.lexiloop.domain.model.LearningStatus
import com.yoon778.lexiloop.domain.model.SelfAssessment
import com.yoon778.lexiloop.domain.model.SessionType

sealed interface LoadState<out T> {
    data object Loading : LoadState<Nothing>
    data class Content<T>(val value: T) : LoadState<T>
    data class Empty(val message: String, val actionLabel: String? = null) : LoadState<Nothing>
    data class Error(val message: String, val canRetry: Boolean) : LoadState<Nothing>
}

data class OnboardingUiState(
    val purposeText: String = "",
    val difficulty: Difficulty = Difficulty.INTERMEDIATE,
    val dailyNewCount: Int = 20,
    val analysis: LoadState<RecommendationProfile>? = null,
    val isAnalyzing: Boolean = false,
    val isGenerating: Boolean = false,
    val fieldErrors: Map<String, String> = emptyMap(),
    val diagnosisWord: String = "maintain",
    val diagnosisIndex: Int = 1,
    val diagnosisTotal: Int = 20,
) {
    val isSubmitEnabled: Boolean get() = purposeText.isNotBlank() && !isAnalyzing
}

sealed interface OnboardingEvent {
    data class PurposeChanged(val value: String) : OnboardingEvent
    data class DifficultySelected(val value: Difficulty) : OnboardingEvent
    data class DailyNewCountSelected(val value: Int) : OnboardingEvent
    data object AnalyzeRequested : OnboardingEvent
    data object AnalysisAccepted : OnboardingEvent
    data object RetryRequested : OnboardingEvent
    data object UseStarterDeck : OnboardingEvent
    data class DiagnosisAnswered(val assessment: SelfAssessment) : OnboardingEvent
}

data class HomeUiState(
    val dueReviewCount: Int = 0,
    val dailyNewGoal: Int = 20,
    val availableNewCount: Int = 0,
    val streakDays: Int = 0,
    val learnedTotal: Int = 0,
    val newStudyLocked: Boolean = false,
    val lockReason: String? = null,
    val activeSessionType: SessionType? = null,
    val activeSessionId: String? = null,
    val newItems: LoadState<List<Pair<String, String>>> = LoadState.Loading,
    val isOffline: Boolean = false,
    val isLoading: Boolean = true,
)

sealed interface HomeEvent {
    data object Refresh : HomeEvent
    data object StartReview : HomeEvent
    data object OpenNewOverview : HomeEvent
    data object StartNew : HomeEvent
    data object ContinueSession : HomeEvent
    data object OpenWordManagement : HomeEvent
    data object OpenSettings : HomeEvent
    data object GenerateMoreRequested : HomeEvent
}

data class StudyUiState(
    val sessionId: String,
    val sessionType: SessionType,
    val completedCount: Int,
    val totalCount: Int,
    val itemId: String,
    val expression: String,
    val phonetic: String? = null,
    val targetMeaningKo: String,
    val auxiliaryMeanings: List<String> = emptyList(),
    val exampleSentence: String,
    val exampleTranslationKo: String,
    val phase: LearningPhase,
    val phaseContent: List<String> = emptyList(),
    val answerText: String = "",
    val hint: String? = null,
    val feedback: StudyFeedback? = null,
    val canSubmit: Boolean = false,
    val isSubmitting: Boolean = false,
    val canGoBack: Boolean = true,
    val isLoading: Boolean = false,
)

data class StudyFeedback(val correct: Boolean, val message: String)

sealed interface StudyEvent {
    data class SelfAssessmentSelected(val value: SelfAssessment) : StudyEvent
    data class ChoiceSelected(val index: Int) : StudyEvent
    data class AnswerChanged(val value: String) : StudyEvent
    data object Submit : StudyEvent
    data object HintRequested : StudyEvent
    data object Next : StudyEvent
    data object RepeatPronunciation : StudyEvent
    data object MarkFullyKnown : StudyEvent
    data object Defer : StudyEvent
    data object Exclude : StudyEvent
    data class AddErrorNote(val note: String) : StudyEvent
}

data class SessionResultUiState(
    val sessionId: String,
    val sessionType: SessionType,
    val completedCount: Int,
)

data class WordListItemUiState(
    val id: String,
    val expression: String,
    val targetMeaningKo: String,
    val status: LearningStatus,
    val excluded: Boolean,
)

data class WordManagementUiState(
    val query: String = "",
    val statusFilter: LearningStatus? = null,
    val words: LoadState<List<WordListItemUiState>> = LoadState.Loading,
)

sealed interface WordManagementEvent {
    data class QueryChanged(val value: String) : WordManagementEvent
    data class FilterSelected(val value: LearningStatus?) : WordManagementEvent
    data class RestoreExcluded(val itemId: String) : WordManagementEvent
    data class MarkFullyKnown(val itemId: String) : WordManagementEvent
    data class AddErrorNote(val itemId: String, val note: String) : WordManagementEvent
    data object Retry : WordManagementEvent
}

data class SettingsUiState(
    val dailyNewCount: Int = 20,
    val notificationEnabled: Boolean = false,
    val notificationHour: Int = 20,
    val notificationMinute: Int = 0,
    val theme: ThemePreference = ThemePreference.SYSTEM,
    val isSaving: Boolean = false,
    val message: String? = null,
)

sealed interface SettingsEvent {
    data class DailyNewCountChanged(val value: Int) : SettingsEvent
    data class NotificationChanged(val enabled: Boolean, val hour: Int, val minute: Int) : SettingsEvent
    data class ThemeChanged(val value: ThemePreference) : SettingsEvent
    data object OpenDataManagement : SettingsEvent
    data object OpenLicenses : SettingsEvent
    data object ExportData : SettingsEvent
    data object ImportData : SettingsEvent
    data object ResetLearning : SettingsEvent
    data object DeleteAllData : SettingsEvent
}

sealed interface UiEffect {
    data class Navigate(val route: AppRoute, val clearBackStack: Boolean = false) : UiEffect
    data object NavigateBack : UiEffect
    data object ShowKeyboard : UiEffect
    data object HideKeyboard : UiEffect
    data class AutoAdvance(val delayMillis: Long = 700) : UiEffect
    data class Speak(val text: String) : UiEffect
    data class Message(val text: String) : UiEffect
    data object RequestNotificationPermission : UiEffect
    data object LaunchJsonExport : UiEffect
    data object LaunchJsonImport : UiEffect
}
