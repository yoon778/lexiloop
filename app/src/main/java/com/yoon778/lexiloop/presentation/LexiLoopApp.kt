package com.yoon778.lexiloop.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.produceState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.yoon778.lexiloop.LexiLoopApplication
import com.yoon778.lexiloop.data.settings.UserSettings
import com.yoon778.lexiloop.platform.tts.EnglishTextToSpeech
import com.yoon778.lexiloop.presentation.contract.AppRoute
import com.yoon778.lexiloop.presentation.contract.HomeEvent
import com.yoon778.lexiloop.presentation.contract.LoadState
import com.yoon778.lexiloop.presentation.contract.OnboardingEvent
import com.yoon778.lexiloop.presentation.contract.UiEffect
import com.yoon778.lexiloop.presentation.screens.DataManagementScreen
import com.yoon778.lexiloop.presentation.screens.DiagnosisScreen
import com.yoon778.lexiloop.presentation.screens.HomeScreen
import com.yoon778.lexiloop.presentation.screens.NewOverviewScreen
import com.yoon778.lexiloop.presentation.screens.OnboardingAnalysisScreen
import com.yoon778.lexiloop.presentation.screens.OnboardingPurposeScreen
import com.yoon778.lexiloop.presentation.screens.SettingsScreen
import com.yoon778.lexiloop.presentation.screens.SessionResultScreen
import com.yoon778.lexiloop.presentation.screens.StudyScreen
import com.yoon778.lexiloop.presentation.screens.WordManagementScreen
import com.yoon778.lexiloop.presentation.theme.LexiLoopTheme
import com.yoon778.lexiloop.presentation.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

/** 앱 루트: 테마 적용 + Navigation 3 호스트. */
@Composable
fun LexiLoopApp() {
    val context = LocalContext.current
    val app = context.applicationContext as? LexiLoopApplication
    val settings: UserSettings = app?.settingsRepository?.settings
        ?.collectAsStateWithLifecycle(initialValue = UserSettings())?.value
        ?: UserSettings()

    LexiLoopTheme(theme = settings.theme) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            if (app == null) {
                // Preview 등 Application 컨텍스트가 없을 때.
                PlaceholderScreen("LexiLoop")
            } else {
                AppNavHost(app, settings)
            }
        }
    }
}

@Composable
private fun AppNavHost(app: LexiLoopApplication, settings: UserSettings) {
    val backStack: SnapshotStateList<AppRoute> = remember {
        mutableStateListOf<AppRoute>(AppRoute.OnboardingPurpose)
    }
    // 온보딩 완료 상태가 로드되면 최초 진입점을 홈으로 교체.
    LaunchedEffect(settings.onboardingCompleted) {
        if (settings.onboardingCompleted && backStack.size == 1 &&
            backStack.first() == AppRoute.OnboardingPurpose
        ) {
            backStack.clear()
            backStack.add(AppRoute.Home)
        }
    }

    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    fun toast(text: String) = scope.launch { snackbar.showSnackbar(text) }.let { }

    val tts = remember { EnglishTextToSpeech(app) }
    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose { tts.close() }
    }

    val nav = remember {
        object {
            fun go(route: AppRoute, clear: Boolean = false) {
                if (clear) backStack.clear()
                backStack.add(route)
            }
            fun back() {
                if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
            }
            fun handle(effect: UiEffect) {
                when (effect) {
                    is UiEffect.Navigate -> go(effect.route, effect.clearBackStack)
                    UiEffect.NavigateBack -> back()
                    is UiEffect.Message -> toast(effect.text)
                    is UiEffect.Speak -> if (!tts.speak(effect.text)) toast("발음을 재생할 수 없어요")
                    UiEffect.LaunchJsonExport, UiEffect.LaunchJsonImport ->
                        toast("데이터 내보내기·가져오기는 준비 중이에요")
                    UiEffect.RequestNotificationPermission -> Unit
                    else -> Unit
                }
            }
        }
    }

    val onboardingVm = remember { app.viewModels.onboarding() }
    val onboardingState by onboardingVm.state.collectAsStateWithLifecycle()
    val homeVm = remember { app.viewModels.home() }
    val homeState by homeVm.state.collectAsStateWithLifecycle()
    LaunchedEffect(onboardingVm) { onboardingVm.effects.collect(nav::handle) }
    LaunchedEffect(homeVm) { homeVm.effects.collect(nav::handle) }

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        NavDisplay(
            backStack = backStack,
            modifier = Modifier.padding(padding),
            onBack = { nav.back() },
            entryProvider = entryProvider {
                entry<AppRoute.OnboardingPurpose> {
                    OnboardingPurposeScreen(
                        state = onboardingState,
                        onEvent = onboardingVm::onEvent,
                    )
                }
                entry<AppRoute.OnboardingAnalysis> {
                    OnboardingAnalysisScreen(
                        analysis = onboardingState.analysis,
                        onAccept = { onboardingVm.onEvent(OnboardingEvent.AnalysisAccepted) },
                        onBack = { nav.back() },
                        onRetry = { onboardingVm.onEvent(OnboardingEvent.RetryRequested) },
                        onUseStarter = { onboardingVm.onEvent(OnboardingEvent.UseStarterDeck) },
                        isGenerating = onboardingState.isGenerating,
                    )
                }
                entry<AppRoute.Diagnosis> {
                    DiagnosisScreen(
                        word = onboardingState.diagnosisWord,
                        index = onboardingState.diagnosisIndex,
                        total = onboardingState.diagnosisTotal,
                        onAnswer = { onboardingVm.onEvent(OnboardingEvent.DiagnosisAnswered(it)) },
                        onBack = { nav.back() },
                    )
                }
                entry<AppRoute.Home> {
                    LaunchedEffect(Unit) { homeVm.onEvent(HomeEvent.Refresh) }
                    HomeScreen(
                        state = homeState,
                        onEvent = homeVm::onEvent,
                    )
                }
                entry<AppRoute.NewOverview> {
                    val words = (homeState.newItems as? LoadState.Content)?.value.orEmpty()
                    NewOverviewScreen(
                        words = words,
                        onStart = { homeVm.onEvent(HomeEvent.StartNew) },
                        onBack = { nav.back() },
                    )
                }
                entry<AppRoute.Study> { route ->
                    val vm = remember(route.sessionId) { app.viewModels.study(route.sessionId) }
                    val state by vm.state.collectAsStateWithLifecycle()
                    LaunchedEffect(vm) { vm.effects.collect(nav::handle) }
                    if (state.isLoading) {
                        PlaceholderScreen("학습 세션을 불러오는 중")
                    } else {
                        StudyScreen(state, vm::onEvent, nav::back)
                    }
                }
                entry<AppRoute.SessionResult> { route ->
                    val result by produceState<com.yoon778.lexiloop.presentation.contract.SessionResultUiState?>(
                        initialValue = null,
                        key1 = route.sessionId,
                    ) {
                        value = runCatching { app.viewModels.sessionResult(route.sessionId) }.getOrNull()
                    }
                    result?.let { state ->
                        SessionResultScreen(state, onHome = { nav.go(AppRoute.Home, clear = true) })
                    } ?: PlaceholderScreen("학습 결과를 불러오는 중")
                }
                entry<AppRoute.WordManagement> {
                    val vm = remember { app.viewModels.words() }
                    val state by vm.state.collectAsStateWithLifecycle()
                    LaunchedEffect(vm) { vm.effects.collect(nav::handle) }
                    WordManagementScreen(
                        state = state,
                        onEvent = vm::onEvent,
                        onBack = { nav.back() },
                    )
                }
                entry<AppRoute.Settings> {
                    SettingsRoute(app, onEffect = nav::handle, onBack = { nav.back() })
                }
                entry<AppRoute.DataManagement> {
                    DataManagementRoute(app, onEffect = nav::handle, onBack = { nav.back() })
                }
            },
        )
    }
}

@Composable
private fun SettingsRoute(
    app: LexiLoopApplication,
    onEffect: (UiEffect) -> Unit,
    onBack: () -> Unit,
) {
    val vm = remember { SettingsViewModel(app.settingsRepository, app.notificationScheduler) }
    val state by vm.state.collectAsStateWithLifecycle()
    LaunchedEffect(vm) { vm.effects.collect(onEffect) }
    SettingsScreen(state = state, onEvent = vm::onEvent, onBack = onBack)
}

@Composable
private fun DataManagementRoute(
    app: LexiLoopApplication,
    onEffect: (UiEffect) -> Unit,
    onBack: () -> Unit,
) {
    val vm = remember { SettingsViewModel(app.settingsRepository, app.notificationScheduler) }
    LaunchedEffect(vm) { vm.effects.collect(onEffect) }
    DataManagementScreen(onEvent = vm::onEvent, onBack = onBack)
}

@Composable
private fun PlaceholderScreen(text: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium)
    }
}

@Preview
@Composable
private fun AppPreview() {
    LexiLoopTheme { PlaceholderScreen("LexiLoop") }
}
