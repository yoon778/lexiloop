package com.yoon778.lexiloop.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yoon778.lexiloop.presentation.contract.LoadState
import com.yoon778.lexiloop.presentation.contract.StudyFeedback

private val MinTouch = 48.dp

/** 표현이 두 단어 이상이면 복합(보조) 표현으로 간주. 표시용 휴리스틱. */
fun isCompoundExpression(expression: String): Boolean =
    expression.trim().any { it.isWhitespace() }

/** 상단바(뒤로가기 48dp)와 표준 배경을 가진 화면 골격. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenScaffold(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    actions: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(title, style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                ),
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack, modifier = Modifier.size(MinTouch)) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                        }
                    }
                },
                actions = { actions() },
            )
        },
        bottomBar = bottomBar,
        content = content,
    )
}

/** 흰 표면 + hairline 테두리 + 라운드 카드. 애플·토스풍 그룹 컨테이너. */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Box(Modifier.padding(20.dp)) { content() }
    }
}

/** 비동기 화면의 로딩·빈·오류·콘텐츠 상태를 일관되게 표시. */
@Composable
fun <T> LoadStateBox(
    state: LoadState<T>,
    modifier: Modifier = Modifier,
    onRetry: () -> Unit = {},
    onAction: () -> Unit = {},
    content: @Composable (T) -> Unit,
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (state) {
            is LoadState.Loading -> CircularProgressIndicator(
                modifier = Modifier.semantics { contentDescription = "불러오는 중" },
            )
            is LoadState.Content -> content(state.value)
            is LoadState.Empty -> CenteredMessage(state.message, state.actionLabel, onAction)
            is LoadState.Error -> CenteredMessage(
                state.message,
                if (state.canRetry) "다시 시도" else null,
                onRetry,
            )
        }
    }
}

@Composable
private fun CenteredMessage(message: String, actionLabel: String?, onAction: () -> Unit) {
    Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(message, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
        if (actionLabel != null) {
            OutlinedButton(onClick = onAction, modifier = Modifier.heightIn(min = MinTouch)) {
                Text(actionLabel)
            }
        }
    }
}

/** 오프라인 안내 배너. 색상과 문구를 함께 사용. */
@Composable
fun OfflineBanner(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "오프라인 상태예요. 학습은 계속할 수 있어요",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

/** 복합(보조) 표현 배지. 색상 + '보조 표현' 텍스트로 핵심 단어와 구분. */
@Composable
fun AuxiliaryBadge(modifier: Modifier = Modifier) {
    Text(
        text = "보조 표현",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSecondaryContainer,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 10.dp, vertical = 4.dp)
            .semantics { contentDescription = "보조 표현" },
    )
}

/** 주 행동 버튼. 눌림 시 미세 축소(손맛), 처리 중에는 자기 자신만 비활성화. */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.97f else 1f, label = "press")
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        interactionSource = interaction,
        shape = MaterialTheme.shapes.small,
        contentPadding = PaddingValues(vertical = 14.dp, horizontal = 24.dp),
        modifier = modifier
            .heightIn(min = MinTouch)
            .graphicsLayer { scaleX = scale; scaleY = scale },
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(20.dp)
                    .semantics { contentDescription = "처리 중" },
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        } else {
            Text(text, style = MaterialTheme.typography.titleMedium)
        }
    }
}

/** 객관식 등 선택지 버튼. 전체 너비, 최소 48dp, hairline 라운드. */
@Composable
fun ChoiceButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.98f else 1f, label = "press")
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interaction,
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.surface),
        contentPadding = PaddingValues(vertical = 14.dp, horizontal = 20.dp),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = MinTouch)
            .graphicsLayer { scaleX = scale; scaleY = scale },
    ) {
        Text(text, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
    }
}

/** 학습 진행률. 현재·전체 수를 stateDescription으로 제공. */
@Composable
fun StudyProgressBar(completed: Int, total: Int, modifier: Modifier = Modifier) {
    val fraction = if (total > 0) completed.toFloat() / total else 0f
    LinearProgressIndicator(
        progress = { fraction },
        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 6.dp)
            .semantics { stateDescription = "전체 $total 중 $completed 완료" },
    )
}

/** 정답·오답 피드백. 색상·아이콘·텍스트 3중 구분 + 등장 팝 모션 + haptic. */
@Composable
fun FeedbackBanner(feedback: StudyFeedback, modifier: Modifier = Modifier) {
    val haptic = LocalHapticFeedback.current
    LaunchedEffect(feedback) {
        haptic.performHapticFeedback(
            if (feedback.correct) HapticFeedbackType.Confirm else HapticFeedbackType.Reject,
        )
    }
    val appear = remember(feedback) { Animatable(0.92f) }
    LaunchedEffect(feedback) {
        appear.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
    }
    val container = if (feedback.correct) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.errorContainer
    }
    val onContainer = if (feedback.correct) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onErrorContainer
    }
    val icon = if (feedback.correct) Icons.Filled.Check else Icons.Filled.Close
    val label = if (feedback.correct) "정답" else "오답"
    Row(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = appear.value
                scaleY = appear.value
                alpha = (appear.value - 0.9f).coerceIn(0f, 0.1f) * 10f
            }
            .clip(MaterialTheme.shapes.medium)
            .background(container)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(icon, contentDescription = null, tint = onContainer)
        Text(
            "$label · ${feedback.message}",
            color = onContainer,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

/** 큰 숫자를 강조하는 통계 카드. 토스풍 시그니처. */
@Composable
fun StatChip(label: String, value: String, modifier: Modifier = Modifier) {
    AppCard(modifier = modifier.clearAndSetSemantics { contentDescription = "$label $value" }) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(value, style = MaterialTheme.typography.displaySmall)
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun TextIconRow(icon: ImageVector, text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(icon, contentDescription = null)
        Text(text)
    }
}

@Composable
fun DialogButtons(
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        TextButton(onClick = onDismiss, modifier = Modifier.heightIn(min = MinTouch)) { Text("취소") }
        TextButton(onClick = onConfirm, modifier = Modifier.heightIn(min = MinTouch)) { Text(confirmLabel) }
    }
}
