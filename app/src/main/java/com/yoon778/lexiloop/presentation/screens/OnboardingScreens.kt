package com.yoon778.lexiloop.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yoon778.lexiloop.data.settings.RecommendationProfile
import com.yoon778.lexiloop.domain.model.Difficulty
import com.yoon778.lexiloop.domain.model.SelfAssessment
import com.yoon778.lexiloop.presentation.components.ChoiceButton
import com.yoon778.lexiloop.presentation.components.LoadStateBox
import com.yoon778.lexiloop.presentation.components.PrimaryButton
import com.yoon778.lexiloop.presentation.components.ScreenScaffold
import com.yoon778.lexiloop.presentation.contract.LoadState
import com.yoon778.lexiloop.presentation.contract.OnboardingEvent
import com.yoon778.lexiloop.presentation.contract.OnboardingUiState
import com.yoon778.lexiloop.presentation.sample.Samples
import com.yoon778.lexiloop.presentation.theme.LexiLoopTheme

private fun difficultyLabel(d: Difficulty) = when (d) {
    Difficulty.BEGINNER -> "초급"
    Difficulty.INTERMEDIATE -> "중급"
    Difficulty.ADVANCED -> "고급"
}

@Composable
fun OnboardingPurposeScreen(
    state: OnboardingUiState,
    onEvent: (OnboardingEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    ScreenScaffold(title = "LexiLoop · 1/3", modifier = modifier) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text("어떤 영어가 필요한가요?", style = MaterialTheme.typography.headlineSmall)

            OutlinedTextField(
                value = state.purposeText,
                onValueChange = { onEvent(OnboardingEvent.PurposeChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("학습 목적을 자연어로 입력") },
                minLines = 3,
                isError = state.fieldErrors.containsKey("purpose"),
                supportingText = state.fieldErrors["purpose"]?.let { { Text(it) } },
            )

            Text("난이도", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Difficulty.entries.forEach { d ->
                    FilterChip(
                        selected = state.difficulty == d,
                        onClick = { onEvent(OnboardingEvent.DifficultySelected(d)) },
                        label = { Text(difficultyLabel(d)) },
                    )
                }
            }

            Text("하루 신규 단어", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(10, 20, 30).forEach { n ->
                    FilterChip(
                        selected = state.dailyNewCount == n,
                        onClick = { onEvent(OnboardingEvent.DailyNewCountSelected(n)) },
                        label = { Text("$n") },
                    )
                }
            }

            if (state.analysis is LoadState.Error) {
                Text(
                    state.analysis.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
                ChoiceButton("기본 단어장으로 시작", onClick = { onEvent(OnboardingEvent.UseStarterDeck) })
            }

            PrimaryButton(
                text = "목적 분석",
                onClick = { onEvent(OnboardingEvent.AnalyzeRequested) },
                enabled = state.isSubmitEnabled,
                loading = state.isAnalyzing,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
fun OnboardingAnalysisScreen(
    analysis: LoadState<RecommendationProfile>?,
    onAccept: () -> Unit,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onUseStarter: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ScreenScaffold(title = "분석 결과", modifier = modifier, onBack = onBack) { padding ->
        LoadStateBox(
            state = analysis ?: LoadState.Loading,
            modifier = Modifier.padding(padding),
            onRetry = onRetry,
            onAction = onUseStarter,
        ) { profile ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                profile.topics.forEach { topic ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { contentDescription = "${topic.name} ${topic.weightPercent}퍼센트" },
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(topic.name, style = MaterialTheme.typography.bodyLarge)
                        Text("${topic.weightPercent}%", style = MaterialTheme.typography.bodyLarge)
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("난이도", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(difficultyLabel(profile.difficulty))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("제외 분야", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(profile.excludedTopics.takeIf { it.isNotEmpty() }?.joinToString() ?: "없음")
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    ChoiceButton("다시 입력", onClick = onBack, modifier = Modifier.weight(1f))
                    PrimaryButton(
                        "이대로 생성",
                        onClick = onAccept,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
fun DiagnosisScreen(
    word: String,
    index: Int,
    total: Int,
    onAnswer: (SelfAssessment) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ScreenScaffold(title = "수준 진단 · $index/$total", modifier = modifier, onBack = onBack) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                word,
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier.padding(top = 48.dp),
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ChoiceButton("안다", onClick = { onAnswer(SelfAssessment.KNOW) })
                ChoiceButton("헷갈린다", onClick = { onAnswer(SelfAssessment.UNSURE) })
                ChoiceButton("처음 본다", onClick = { onAnswer(SelfAssessment.NEW) })
            }
        }
    }
}

@Preview
@Composable
private fun PurposePreview() = LexiLoopTheme {
    OnboardingPurposeScreen(Samples.onboarding, {})
}

@Preview
@Composable
private fun AnalysisPreview() = LexiLoopTheme {
    OnboardingAnalysisScreen(LoadState.Content(Samples.profile), {}, {}, {}, {})
}

@Preview
@Composable
private fun DiagnosisPreview() = LexiLoopTheme {
    DiagnosisScreen("maintain", 7, 20, {}, {})
}
