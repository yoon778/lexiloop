package com.yoon778.lexiloop.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yoon778.lexiloop.domain.model.LearningPhase
import com.yoon778.lexiloop.presentation.components.ChoiceButton
import com.yoon778.lexiloop.presentation.components.FeedbackBanner
import com.yoon778.lexiloop.presentation.components.PrimaryButton
import com.yoon778.lexiloop.presentation.components.ScreenScaffold
import com.yoon778.lexiloop.presentation.components.StudyProgressBar
import com.yoon778.lexiloop.presentation.contract.SessionResultUiState
import com.yoon778.lexiloop.presentation.contract.StudyEvent
import com.yoon778.lexiloop.presentation.contract.StudyFeedback
import com.yoon778.lexiloop.presentation.contract.StudyUiState
import com.yoon778.lexiloop.domain.model.SelfAssessment
import com.yoon778.lexiloop.domain.model.SessionType
import com.yoon778.lexiloop.presentation.sample.Samples
import com.yoon778.lexiloop.presentation.theme.LexiLoopTheme

@Composable
fun StudyScreen(
    state: StudyUiState,
    onEvent: (StudyEvent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val title = if (state.sessionType == SessionType.NEW) "신규 학습" else "복습"
    ScreenScaffold(
        title = "$title · ${state.completedCount}/${state.totalCount}",
        modifier = modifier,
        onBack = if (state.canGoBack) onBack else null,
        actions = { StudyOverflow(onEvent) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            StudyProgressBar(state.completedCount, state.totalCount)

            // 표현 + 발음
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(state.expression, style = MaterialTheme.typography.displaySmall)
                state.phonetic?.let {
                    Text(it, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                TextButton(onClick = { onEvent(StudyEvent.RepeatPronunciation) }) {
                    Text("다시 듣기")
                }
            }

            state.feedback?.let { FeedbackBanner(it) }

            PhaseContent(state, onEvent)
        }
    }
}

@Composable
private fun PhaseContent(state: StudyUiState, onEvent: (StudyEvent) -> Unit) {
    when (state.phase) {
        LearningPhase.CARD -> {
            MeaningBlock(state)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                ChoiceButton("안다", onClick = { onEvent(StudyEvent.SelfAssessmentSelected(SelfAssessment.KNOW)) })
                ChoiceButton("헷갈린다", onClick = { onEvent(StudyEvent.SelfAssessmentSelected(SelfAssessment.UNSURE)) })
                ChoiceButton("처음 본다", onClick = { onEvent(StudyEvent.SelfAssessmentSelected(SelfAssessment.NEW)) })
            }
        }
        LearningPhase.EN_TO_KO, LearningPhase.KO_TO_EN -> {
            val prompt = if (state.phase == LearningPhase.KO_TO_EN) state.targetMeaningKo else state.expression
            Text(prompt, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                state.phaseContent.forEachIndexed { i, choice ->
                    ChoiceButton(choice, onClick = { onEvent(StudyEvent.ChoiceSelected(i)) })
                }
            }
        }
        LearningPhase.SPELLING, LearningPhase.SENTENCE -> {
            if (state.phase == LearningPhase.SENTENCE) {
                Text(state.phaseContent.firstOrNull() ?: state.exampleSentence, style = MaterialTheme.typography.titleMedium)
                Text(state.exampleTranslationKo, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Text(state.targetMeaningKo, style = MaterialTheme.typography.titleLarge)
            }
            AnswerInput(state, onEvent)
            state.hint?.let {
                Text("힌트: $it", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            TextButton(onClick = { onEvent(StudyEvent.HintRequested) }) { Text("힌트 보기") }
        }
        LearningPhase.CORRECTION -> {
            MeaningBlock(state)
            PrimaryButton("다음", onClick = { onEvent(StudyEvent.Next) }, modifier = Modifier.fillMaxWidth())
        }
        LearningPhase.DONE -> Unit
    }
}

@Composable
private fun AnswerInput(state: StudyUiState, onEvent: (StudyEvent) -> Unit) {
    OutlinedTextField(
        value = state.answerText,
        onValueChange = { onEvent(StudyEvent.AnswerChanged(it)) },
        modifier = Modifier.fillMaxWidth(),
        label = { Text("영어로 입력") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { if (state.canSubmit) onEvent(StudyEvent.Submit) }),
    )
    PrimaryButton(
        "확인",
        onClick = { onEvent(StudyEvent.Submit) },
        enabled = state.canSubmit,
        loading = state.isSubmitting,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun MeaningBlock(state: StudyUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
        Text("주요 뜻 · ${state.targetMeaningKo}", style = MaterialTheme.typography.titleMedium)
        if (state.auxiliaryMeanings.isNotEmpty()) {
            Text(
                "보조 뜻 · ${state.auxiliaryMeanings.joinToString()}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(state.exampleSentence, style = MaterialTheme.typography.bodyLarge)
        Text(state.exampleTranslationKo, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun StudyOverflow(onEvent: (StudyEvent) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var noteOpen by remember { mutableStateOf(false) }
    IconButton(onClick = { expanded = true }) {
        Icon(Icons.Filled.MoreVert, contentDescription = "단어 관리 메뉴")
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        DropdownMenuItem(text = { Text("이미 완전히 앎") }, onClick = { expanded = false; onEvent(StudyEvent.MarkFullyKnown) })
        DropdownMenuItem(text = { Text("나중에 다시") }, onClick = { expanded = false; onEvent(StudyEvent.Defer) })
        DropdownMenuItem(text = { Text("단어장 제외") }, onClick = { expanded = false; onEvent(StudyEvent.Exclude) })
        DropdownMenuItem(text = { Text("오류 메모") }, onClick = { expanded = false; noteOpen = true })
    }
    if (noteOpen) {
        var note by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { noteOpen = false },
            title = { Text("오류 메모") },
            text = {
                OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("뜻·예문 오류를 적어주세요") })
            },
            confirmButton = {
                TextButton(onClick = { noteOpen = false; onEvent(StudyEvent.AddErrorNote(note)) }) { Text("저장") }
            },
            dismissButton = { TextButton(onClick = { noteOpen = false }) { Text("취소") } },
        )
    }
}

@Composable
fun SessionResultScreen(
    state: SessionResultUiState,
    onHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ScreenScaffold(title = "오늘 학습 완료", modifier = modifier) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            val label = if (state.sessionType == SessionType.NEW) "신규 확인" else "복습 완료"
            Text("$label ${state.completedCount}개", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(top = 48.dp))
            PrimaryButton("홈으로", onClick = onHome, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Preview
@Composable
private fun StudyCardPreview() = LexiLoopTheme {
    StudyScreen(Samples.study(LearningPhase.CARD), {}, {})
}

@Preview
@Composable
private fun StudyChoicePreview() = LexiLoopTheme {
    StudyScreen(Samples.study(LearningPhase.EN_TO_KO), {}, {})
}

@Preview
@Composable
private fun StudySpellingPreview() = LexiLoopTheme {
    StudyScreen(Samples.study(LearningPhase.SPELLING), {}, {})
}

@Preview
@Composable
private fun StudyCorrectionPreview() = LexiLoopTheme {
    StudyScreen(
        Samples.study(LearningPhase.CORRECTION, StudyFeedback(false, "정답은 deploy")),
        {},
        {},
    )
}

@Preview
@Composable
private fun ResultPreview() = LexiLoopTheme {
    SessionResultScreen(Samples.sessionResult, {})
}
