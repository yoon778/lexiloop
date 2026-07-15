package com.yoon778.lexiloop.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yoon778.lexiloop.presentation.components.ChoiceButton
import com.yoon778.lexiloop.presentation.components.OfflineBanner
import com.yoon778.lexiloop.presentation.components.PrimaryButton
import com.yoon778.lexiloop.presentation.components.ScreenScaffold
import com.yoon778.lexiloop.presentation.components.StatChip
import com.yoon778.lexiloop.presentation.contract.HomeEvent
import com.yoon778.lexiloop.presentation.contract.HomeUiState
import com.yoon778.lexiloop.presentation.sample.Samples
import com.yoon778.lexiloop.presentation.theme.LexiLoopTheme

@Composable
fun HomeScreen(
    state: HomeUiState,
    onEvent: (HomeEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    ScreenScaffold(
        title = "오늘도 짧게 이어가요",
        modifier = modifier,
        actions = {
            IconButton(onClick = { onEvent(HomeEvent.OpenSettings) }) {
                Icon(Icons.Filled.Settings, contentDescription = "설정")
            }
        },
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@ScreenScaffold
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            if (state.isOffline) OfflineBanner()

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatChip("연속 학습일", "${state.streakDays}일", Modifier.weight(1f))
                StatChip("누적 학습", "${state.learnedTotal}개", Modifier.weight(1f))
            }

            // 복습
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("복습 ${state.dueReviewCount}개", style = MaterialTheme.typography.titleMedium)
                PrimaryButton(
                    text = if (state.activeSessionType != null) "이어서 하기" else "복습 시작",
                    onClick = { onEvent(HomeEvent.StartReview) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // 신규
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("신규 ${state.dailyNewGoal}개", style = MaterialTheme.typography.titleMedium)
                PrimaryButton(
                    text = "신규 시작",
                    onClick = { onEvent(HomeEvent.OpenNewOverview) },
                    enabled = !state.newStudyLocked,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (state.newStudyLocked && state.lockReason != null) {
                    Text(
                        state.lockReason,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                if (state.availableNewCount < state.dailyNewGoal) {
                    TextButton(onClick = { onEvent(HomeEvent.GenerateMoreRequested) }) {
                        Text("추천 단어 보충하기")
                    }
                }
            }

            ChoiceButton("단어 관리", onClick = { onEvent(HomeEvent.OpenWordManagement) })
        }
    }
}

@Composable
fun NewOverviewScreen(
    words: List<Pair<String, String>>,
    onStart: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ScreenScaffold(
        title = "오늘의 단어 · ${words.size}개",
        modifier = modifier,
        onBack = onBack,
        bottomBar = {
            PrimaryButton(
                text = "학습 시작",
                onClick = onStart,
                enabled = words.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
            )
        },
    ) { padding ->
        if (words.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("오늘 학습할 신규 단어가 없어요", textAlign = TextAlign.Center)
            }
            return@ScreenScaffold
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(words) { (expr, meaning) ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(expr, style = MaterialTheme.typography.bodyLarge)
                    Text(meaning, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Preview
@Composable
private fun HomePreview() = LexiLoopTheme { HomeScreen(Samples.home, {}) }

@Preview
@Composable
private fun HomeLockedPreview() = LexiLoopTheme { HomeScreen(Samples.homeLocked, {}) }

@Preview
@Composable
private fun NewOverviewPreview() = LexiLoopTheme {
    NewOverviewScreen(Samples.newOverview, {}, {})
}
