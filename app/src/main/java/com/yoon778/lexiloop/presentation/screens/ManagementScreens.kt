package com.yoon778.lexiloop.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yoon778.lexiloop.data.settings.ThemePreference
import com.yoon778.lexiloop.domain.model.LearningStatus
import com.yoon778.lexiloop.presentation.components.ChoiceButton
import com.yoon778.lexiloop.presentation.components.LoadStateBox
import com.yoon778.lexiloop.presentation.components.PrimaryButton
import com.yoon778.lexiloop.presentation.components.ScreenScaffold
import com.yoon778.lexiloop.presentation.contract.SettingsEvent
import com.yoon778.lexiloop.presentation.contract.SettingsUiState
import com.yoon778.lexiloop.presentation.contract.WordListItemUiState
import com.yoon778.lexiloop.presentation.contract.WordManagementEvent
import com.yoon778.lexiloop.presentation.contract.WordManagementUiState
import com.yoon778.lexiloop.presentation.sample.Samples
import com.yoon778.lexiloop.presentation.theme.LexiLoopTheme

private fun statusLabel(s: LearningStatus?) = when (s) {
    null -> "전체"
    LearningStatus.QUEUED -> "대기"
    LearningStatus.LEARNING -> "학습"
    LearningStatus.REVIEWING -> "복습"
    LearningStatus.MASTERED -> "장기 기억"
}

@Composable
fun WordManagementScreen(
    state: WordManagementUiState,
    onEvent: (WordManagementEvent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ScreenScaffold(title = "단어 관리", modifier = modifier, onBack = onBack) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = state.query,
                onValueChange = { onEvent(WordManagementEvent.QueryChanged(it)) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                label = { Text("검색") },
                singleLine = true,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScrollRow()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val filters = listOf<LearningStatus?>(null) + LearningStatus.entries
                filters.forEach { f ->
                    FilterChip(
                        selected = state.statusFilter == f,
                        onClick = { onEvent(WordManagementEvent.FilterSelected(f)) },
                        label = { Text(statusLabel(f)) },
                    )
                }
            }
            LoadStateBox(
                state = state.words,
                modifier = Modifier.fillMaxSize(),
                onRetry = { onEvent(WordManagementEvent.Retry) },
            ) { list ->
                if (list.isEmpty()) {
                    Text("일치하는 단어가 없어요", modifier = Modifier.padding(24.dp), textAlign = TextAlign.Center)
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(24.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        items(list, key = { it.id }) { item -> WordRow(item, onEvent) }
                    }
                }
            }
        }
    }
}

@Composable
private fun WordRow(item: WordListItemUiState, onEvent: (WordManagementEvent) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(item.expression, style = MaterialTheme.typography.bodyLarge)
            Text(
                "${item.targetMeaningKo} · ${statusLabel(item.status)}${if (item.excluded) " · 제외됨" else ""}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (item.excluded) {
            TextButton(onClick = { onEvent(WordManagementEvent.RestoreExcluded(item.id)) }) { Text("복원") }
        } else if (item.status != LearningStatus.MASTERED) {
            TextButton(onClick = { onEvent(WordManagementEvent.MarkFullyKnown(item.id)) }) { Text("완전히 앎") }
        }
    }
}

// 필터 칩 가로 스크롤을 위한 소형 헬퍼.
@Composable
private fun Modifier.horizontalScrollRow(): Modifier =
    this.then(Modifier.horizontalScroll(rememberScrollState()))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onEvent: (SettingsEvent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var timeOpen by remember { mutableStateOf(false) }
    ScreenScaffold(title = "설정", modifier = modifier, onBack = onBack) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text("하루 신규 단어", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(10, 20, 30).forEach { n ->
                    FilterChip(
                        selected = state.dailyNewCount == n,
                        onClick = { onEvent(SettingsEvent.DailyNewCountChanged(n)) },
                        label = { Text("$n") },
                    )
                }
            }

            HorizontalDivider()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            ) {
                Text("학습 알림", style = MaterialTheme.typography.titleMedium)
                Switch(
                    checked = state.notificationEnabled,
                    onCheckedChange = {
                        onEvent(SettingsEvent.NotificationChanged(it, state.notificationHour, state.notificationMinute))
                    },
                )
            }
            if (state.notificationEnabled) {
                TextButton(onClick = { timeOpen = true }) {
                    Text("알림 시각 · %02d:%02d".format(state.notificationHour, state.notificationMinute))
                }
            }

            HorizontalDivider()
            Text("테마", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemePreference.entries.forEach { t ->
                    FilterChip(
                        selected = state.theme == t,
                        onClick = { onEvent(SettingsEvent.ThemeChanged(t)) },
                        label = { Text(when (t) { ThemePreference.SYSTEM -> "시스템"; ThemePreference.LIGHT -> "밝게"; ThemePreference.DARK -> "어둡게" }) },
                    )
                }
            }

            HorizontalDivider()
            ChoiceButton("데이터 관리", onClick = { onEvent(SettingsEvent.OpenDataManagement) })
            ChoiceButton("오픈 데이터 라이선스", onClick = { onEvent(SettingsEvent.OpenLicenses) })

            state.message?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
    if (timeOpen) {
        val picker = rememberTimePickerState(
            initialHour = state.notificationHour,
            initialMinute = state.notificationMinute,
            is24Hour = true,
        )
        AlertDialog(
            onDismissRequest = { timeOpen = false },
            title = { Text("알림 시각") },
            text = { TimePicker(state = picker) },
            confirmButton = {
                TextButton(onClick = {
                    timeOpen = false
                    onEvent(SettingsEvent.NotificationChanged(true, picker.hour, picker.minute))
                }) { Text("확인") }
            },
            dismissButton = { TextButton(onClick = { timeOpen = false }) { Text("취소") } },
        )
    }
}

@Composable
fun DataManagementScreen(
    onEvent: (SettingsEvent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var confirm by remember { mutableStateOf<DestructiveAction?>(null) }
    ScreenScaffold(title = "데이터 관리", modifier = modifier, onBack = onBack) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(padding).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ChoiceButton("JSON 내보내기", onClick = { onEvent(SettingsEvent.ExportData) })
            ChoiceButton("JSON 가져오기", onClick = { onEvent(SettingsEvent.ImportData) })
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            ChoiceButton("학습 기록 초기화", onClick = { confirm = DestructiveAction.ResetLearning })
            ChoiceButton("전체 데이터 삭제", onClick = { confirm = DestructiveAction.DeleteAll })
        }
    }
    confirm?.let { action ->
        AlertDialog(
            onDismissRequest = { confirm = null },
            title = { Text(action.confirmLabel) },
            text = { Text(action.explanation) },
            confirmButton = {
                TextButton(onClick = {
                    confirm = null
                    onEvent(if (action == DestructiveAction.ResetLearning) SettingsEvent.ResetLearning else SettingsEvent.DeleteAllData)
                }) { Text(action.confirmLabel) }
            },
            dismissButton = { TextButton(onClick = { confirm = null }) { Text("취소") } },
        )
    }
}

private enum class DestructiveAction(val confirmLabel: String, val explanation: String) {
    ResetLearning("학습 기록 초기화", "모든 학습·복습 진도가 사라집니다. 추천 단어장은 유지됩니다."),
    DeleteAll("전체 데이터 삭제", "단어장, 진도, 설정을 포함한 모든 데이터가 영구히 삭제됩니다."),
}

@Preview
@Composable
private fun WordManagementPreview() = LexiLoopTheme {
    WordManagementScreen(Samples.words, {}, {})
}

@Preview
@Composable
private fun SettingsPreview() = LexiLoopTheme {
    SettingsScreen(Samples.settings, {}, {})
}

@Preview
@Composable
private fun DataManagementPreview() = LexiLoopTheme {
    DataManagementScreen({}, {})
}
