package com.yoon778.lexiloop.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.yoon778.lexiloop.domain.model.LearningPhase
import com.yoon778.lexiloop.presentation.contract.LoadState
import com.yoon778.lexiloop.presentation.contract.StudyFeedback
import com.yoon778.lexiloop.presentation.contract.WordManagementUiState
import com.yoon778.lexiloop.presentation.sample.Samples
import com.yoon778.lexiloop.presentation.screens.HomeScreen
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
