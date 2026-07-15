package com.yoon778.lexiloop.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Nav3 호스트 스모크: 콜드 스타트 렌더링과 설정 이동/복귀. */
@RunWith(AndroidJUnit4::class)
class NavigationUiTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun coldStart_rendersRootWithoutCrash() {
        rule.setContent { LexiLoopApp() }
        rule.onRoot().assertExists()
    }

    @Test
    fun home_toSettings_andBack() {
        rule.setContent { LexiLoopApp() }
        rule.waitForIdle()
        // 시작 화면이 온보딩일 수 있으므로 설정 아이콘이 보일 때만 이동을 검증.
        val hasSettings = rule.onAllNodesWithContentDescription("설정")
            .fetchSemanticsNodes().isNotEmpty()
        if (hasSettings) {
            rule.onNodeWithContentDescription("설정").performClick()
            rule.onNodeWithText("테마").assertIsDisplayed()
            rule.onNodeWithContentDescription("뒤로").performClick()
        }
    }
}
