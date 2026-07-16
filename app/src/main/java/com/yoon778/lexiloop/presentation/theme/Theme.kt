package com.yoon778.lexiloop.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yoon778.lexiloop.R
import com.yoon778.lexiloop.data.settings.ThemePreference

// 단일 강조색: 브랜드 틸. dynamic color 미사용으로 밝기·기기와 무관하게 일관 유지.
// 배경은 아주 옅은 뉴트럴, 카드/표면은 흰색 → 애플·토스풍 그룹 리스트 느낌.
private val LightColors = lightColorScheme(
    primary = Color(0xFF1F5C4D),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFB7F0E0),
    onPrimaryContainer = Color(0xFF00201A),
    secondary = Color(0xFF4B635C),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFDCEBE5),
    onSecondaryContainer = Color(0xFF06201A),
    background = Color(0xFFF3F6F4),
    onBackground = Color(0xFF171D1B),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF171D1B),
    surfaceVariant = Color(0xFFEAF0ED),
    onSurfaceVariant = Color(0xFF57635E),
    surfaceContainer = Color(0xFFF7FAF8),
    surfaceContainerHighest = Color(0xFFEDF2EF),
    outline = Color(0xFF8A958F),
    outlineVariant = Color(0xFFDBE3DF),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8BD4C2),
    onPrimary = Color(0xFF00382D),
    primaryContainer = Color(0xFF0C4A3E),
    onPrimaryContainer = Color(0xFFB7F0E0),
    secondary = Color(0xFFB1CCC3),
    onSecondary = Color(0xFF1C352F),
    secondaryContainer = Color(0xFF2A4640),
    onSecondaryContainer = Color(0xFFDCEBE5),
    background = Color(0xFF10140F),
    onBackground = Color(0xFFE1E3E0),
    surface = Color(0xFF191D18),
    onSurface = Color(0xFFE1E3E0),
    surfaceVariant = Color(0xFF3F4946),
    onSurfaceVariant = Color(0xFFBEC9C3),
    surfaceContainer = Color(0xFF1D211C),
    surfaceContainerHighest = Color(0xFF303430),
    outline = Color(0xFF899390),
    outlineVariant = Color(0xFF3F4946),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC),
)

private val Pretendard = FontFamily(
    Font(R.font.pretendard_regular, FontWeight.Normal),
    Font(R.font.pretendard_semibold, FontWeight.SemiBold),
    Font(R.font.pretendard_bold, FontWeight.Bold),
)

// Pretendard 적용 + 제목/숫자 굵기 대비 강화. 모든 스타일 sp 단위라 200% 글자 자동 대응.
private val AppTypography: Typography = Typography().run {
    fun androidx.compose.ui.text.TextStyle.p(weight: FontWeight? = null, spacing: Double? = null) =
        copy(
            fontFamily = Pretendard,
            fontWeight = weight ?: fontWeight,
            letterSpacing = spacing?.sp ?: letterSpacing,
        )
    copy(
        displayLarge = displayLarge.p(FontWeight.Bold, -1.0),
        displayMedium = displayMedium.p(FontWeight.Bold, -0.5),
        displaySmall = displaySmall.p(FontWeight.Bold, -0.5),
        headlineLarge = headlineLarge.p(FontWeight.Bold),
        headlineMedium = headlineMedium.p(FontWeight.Bold),
        headlineSmall = headlineSmall.p(FontWeight.Bold),
        titleLarge = titleLarge.p(FontWeight.SemiBold),
        titleMedium = titleMedium.p(FontWeight.SemiBold),
        titleSmall = titleSmall.p(FontWeight.SemiBold),
        bodyLarge = bodyLarge.p(),
        bodyMedium = bodyMedium.p(),
        bodySmall = bodySmall.p(),
        labelLarge = labelLarge.p(FontWeight.SemiBold),
        labelMedium = labelMedium.p(FontWeight.SemiBold),
        labelSmall = labelSmall.p(FontWeight.SemiBold),
    )
}

// 계층적 라운드: 버튼/칩 14, 카드/입력 20, 시트/다이얼로그 28.
private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

@Composable
fun LexiLoopTheme(
    theme: ThemePreference = ThemePreference.SYSTEM,
    content: @Composable () -> Unit,
) {
    val dark = when (theme) {
        ThemePreference.SYSTEM -> isSystemInDarkTheme()
        ThemePreference.LIGHT -> false
        ThemePreference.DARK -> true
    }
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
