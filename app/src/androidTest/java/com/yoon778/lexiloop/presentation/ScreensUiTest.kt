package com.yoon778.lexiloop.presentation

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.yoon778.lexiloop.domain.model.LearningPhase
import com.yoon778.lexiloop.presentation.contract.LoadState
import com.yoon778.lexiloop.presentation.contract.StudyFeedback
import com.yoon778.lexiloop.presentation.contract.WordManagementUiState
import com.yoon778.lexiloop.presentation.sample.Samples
import com.yoon778.lexiloop.presentation.screens.HomeScreen
import com.yoon778.lexiloop.presentation.screens.NewOverviewScreen
import com.yoon778.lexiloop.presentation.screens.SettingsScreen
import com.yoon778.lexiloop.presentation.screens.StudyScreen
import com.yoon778.lexiloop.presentation.screens.WordManagementScreen
import com.yoon778.lexiloop.presentation.theme.LexiLoopTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScreensUiTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun homeLocked_showsReason_andDisablesNewButton() {
        rule.setContent { LexiLoopTheme { HomeScreen(Samples.homeLocked, {}) } }
        rule.onNodeWithText(Samples.homeLocked.lockReason!!).assertIsDisplayed()
        rule.onNodeWithText("신규 시작").assertIsNotEnabled()
    }

    @Test
    fun studyCorrection_showsIncorrectAsText_notColorOnly() {
        rule.setContent {
            LexiLoopTheme {
                StudyScreen(
                    Samples.study(LearningPhase.CORRECTION, StudyFeedback(false, "정답은 deploy")),
                    {},
                    {},
                )
            }
        }
        // 색상 외에 '오답' 텍스트로도 구분됨을 검증.
        rule.onNodeWithText("오답 · 정답은 deploy").assertIsDisplayed()
    }

    @Test
    fun backIcon_hasContentDescription() {
        rule.setContent { LexiLoopTheme { SettingsScreen(Samples.settings, {}, {}) } }
        rule.onNodeWithContentDescription("뒤로").assertIsDisplayed()
    }

    @Test
    fun homeSettingsIcon_hasContentDescription() {
        rule.setContent { LexiLoopTheme { HomeScreen(Samples.home, {}) } }
        rule.onNodeWithContentDescription("설정").assertIsDisplayed()
    }

    @Test
    fun spellingPhase_doesNotRevealExpression() {
        rule.setContent { LexiLoopTheme { StudyScreen(Samples.study(LearningPhase.SPELLING), {}, {}) } }
        // 정답인 영어 표현·발음·듣기가 화면에 없어야 함.
        rule.onAllNodesWithText("deploy").assertCountEquals(0)
        rule.onAllNodesWithText("다시 듣기").assertCountEquals(0)
        // 뜻 프롬프트는 표시.
        rule.onNodeWithText("배포하다").assertIsDisplayed()
    }

    @Test
    fun koToEnChoicePhase_doesNotRevealExpressionOutsideChoices() {
        rule.setContent { LexiLoopTheme { StudyScreen(Samples.study(LearningPhase.KO_TO_EN), {}, {}) } }
        // 선택지 안의 1개만 존재해야 함(상단 헤더 노출 없음).
        rule.onAllNodesWithText("deploy").assertCountEquals(1)
        rule.onAllNodesWithText("다시 듣기").assertCountEquals(0)
    }

    @Test
    fun cardPhase_showsExpressionAndListen() {
        rule.setContent { LexiLoopTheme { StudyScreen(Samples.study(LearningPhase.CARD), {}, {}) } }
        rule.onNodeWithText("deploy").assertIsDisplayed()
        rule.onNodeWithText("다시 듣기").assertIsDisplayed()
    }

    @Test
    fun newOverview_compoundExpression_showsAuxiliaryBadge() {
        rule.setContent {
            LexiLoopTheme {
                NewOverviewScreen(
                    words = listOf("give up" to "포기하다", "deploy" to "배포하다"),
                    onStart = {},
                    onBack = {},
                )
            }
        }
        rule.onNodeWithText("보조 표현").assertIsDisplayed()
    }

    @Test
    fun wordManagementEmpty_showsMessage() {
        rule.setContent {
            LexiLoopTheme {
                WordManagementScreen(
                    WordManagementUiState(words = LoadState.Empty("단어가 아직 없어요")),
                    {},
                    {},
                )
            }
        }
        rule.onNodeWithText("단어가 아직 없어요").assertIsDisplayed()
    }
}
