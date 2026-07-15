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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.yoon778.lexiloop.presentation.contract.HomeUiState
import com.yoon778.lexiloop.presentation.contract.OnboardingEvent
import com.yoon778.lexiloop.presentation.contract.OnboardingUiState
import com.yoon778.lexiloop.presentation.contract.UiEffect
import com.yoon778.lexiloop.presentation.screens.DataManagementScreen
import com.yoon778.lexiloop.presentation.screens.DiagnosisScreen
import com.yoon778.lexiloop.presentation.screens.HomeScreen
import com.yoon778.lexiloop.presentation.screens.NewOverviewScreen
import com.yoon778.lexiloop.presentation.screens.OnboardingAnalysisScreen
import com.yoon778.lexiloop.presentation.screens.OnboardingPurposeScreen
import com.yoon778.lexiloop.presentation.screens.SettingsScreen
import com.yoon778.lexiloop.presentation.screens.WordManagementScreen
import com.yoon778.lexiloop.presentation.theme.LexiLoopTheme
import com.yoon778.lexiloop.presentation.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

/**
 * 앱 루트: 테마 적용 + Navigation 3 호스트.
 *
 * 표시 계층만 소유. 데이터 구동 화면(Home/Study/WordManagement/Onboarding 분석)의 ViewModel
 * 팩토리는 Codex 소유이며 아직 부재하여, 해당 화면은 기본·빈 상태로 표시하고 이동만 배선한다.
 * 요청: coordination/TO_CODEX.md REQ-002. Settings/DataManagement는 실제 SettingsViewModel로 배선.
 */
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
                    UiEffect.LaunchJsonExport, UiEffect.LaunchJsonImport ->
                        toast("데이터 내보내기·가져오기는 준비 중이에요")
                    UiEffect.RequestNotificationPermission -> Unit
                    else -> Unit
                }
            }
        }
    }

    // TTS: 발음 재생. Study VM 배선 전이라도 발음 효과는 동작하도록 준비.
    val tts = remember { EnglishTextToSpeech(app) }
    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose { tts.close() }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        NavDisplay(
            backStack = backStack,
            modifier = Modifier.padding(padding),
            onBack = { nav.back() },
            entryProvider = entryProvider {
                entry<AppRoute.OnboardingPurpose> {
                    OnboardingPurposeRoute(onEvent = { ev ->
                        when (ev) {
                            OnboardingEvent.AnalyzeRequested, OnboardingEvent.RetryRequested ->
                                toast("목적 분석은 Codex Gemini 연동 후 동작해요")
                            OnboardingEvent.UseStarterDeck -> nav.go(AppRoute.Diagnosis)
                            else -> Unit
                        }
                    })
                }
                entry<AppRoute.OnboardingAnalysis> {
                    OnboardingAnalysisScreen(
                        analysis = null,
                        onAccept = { nav.go(AppRoute.Diagnosis) },
                        onBack = { nav.back() },
                        onRetry = { toast("목적 분석은 Codex Gemini 연동 후 동작해요") },
                        onUseStarter = { nav.go(AppRoute.Diagnosis) },
                    )
                }
                entry<AppRoute.Diagnosis> {
                    DiagnosisScreen(
                        word = "maintain",
                        index = 1,
                        total = 20,
                        onAnswer = { nav.go(AppRoute.Home, clear = true) },
                        onBack = { nav.back() },
                    )
                }
                entry<AppRoute.Home> {
                    HomeScreen(
                        state = HomeUiState(isLoading = false, dailyNewGoal = settings.dailyNewCount),
                        onEvent = { ev ->
                            when (ev) {
                                HomeEvent.OpenSettings -> nav.go(AppRoute.Settings)
                                HomeEvent.OpenWordManagement -> nav.go(AppRoute.WordManagement)
                                HomeEvent.OpenNewOverview -> nav.go(AppRoute.NewOverview)
                                HomeEvent.StartReview -> toast("복습 세션은 Codex 연동 후 동작해요")
                                HomeEvent.GenerateMoreRequested -> toast("추천 생성 확인이 필요해요")
                                else -> Unit
                            }
                        },
                    )
                }
                entry<AppRoute.NewOverview> {
                    NewOverviewScreen(
                        words = emptyList(),
                        onStart = { toast("신규 세션은 Codex 연동 후 동작해요") },
                        onBack = { nav.back() },
                    )
                }
                entry<AppRoute.Study> { route ->
                    // Study VM(initialState/reduce)은 Codex 소유. 배선 전 안내만 표시.
                    PlaceholderScreen("학습 세션 준비 중\n(session ${route.sessionId})")
                }
                entry<AppRoute.SessionResult> {
                    PlaceholderScreen("학습 결과 준비 중")
                }
                entry<AppRoute.WordManagement> {
                    WordManagementScreen(
                        state = com.yoon778.lexiloop.presentation.contract.WordManagementUiState(
                            words = com.yoon778.lexiloop.presentation.contract.LoadState.Empty("단어가 아직 없어요"),
                        ),
                        onEvent = {},
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

/** 온보딩 입력은 순수 UI 상태라 로컬에서 관리 (Gemini 분석만 Codex 대기). */
@Composable
private fun OnboardingPurposeRoute(onEvent: (OnboardingEvent) -> Unit) {
    var state by remember { mutableStateOf(OnboardingUiState()) }

    OnboardingPurposeScreen(
        state = state,
        onEvent = { ev ->
            when (ev) {
                is OnboardingEvent.PurposeChanged -> state = state.copy(purposeText = ev.value.take(1_000))
                is OnboardingEvent.DifficultySelected -> state = state.copy(difficulty = ev.value)
                is OnboardingEvent.DailyNewCountSelected -> state = state.copy(dailyNewCount = ev.value)
                else -> onEvent(ev)
            }
        },
    )
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
