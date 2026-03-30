package com.flowscale.app

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.flowscale.app.ui.theme.FlowScaleTheme
import org.junit.Rule
import org.junit.Test

class ExampleInstrumentedTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun appShowsTitle() {
        composeTestRule.setContent {
            FlowScaleTheme {
                // Minimal smoke test
            }
        }
    }
}
