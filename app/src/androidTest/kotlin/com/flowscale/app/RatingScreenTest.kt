package com.flowscale.app

import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.flowscale.app.ui.RatingScreen
import com.flowscale.app.ui.theme.FlowScaleTheme
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class RatingScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var viewModel: RatingViewModel

    @Before
    fun setUp() {
        val app = InstrumentationRegistry.getInstrumentation()
            .targetContext.applicationContext as FlowScaleApplication
        runBlocking { app.database.clearAllTables() }
        app.getSharedPreferences("flowscale_prefs", Context.MODE_PRIVATE).edit().clear().commit()
        viewModel = RatingViewModel(app)
    }

    private fun launchScreen() {
        composeTestRule.setContent {
            FlowScaleTheme {
                RatingScreen(viewModel)
            }
        }
    }

    @Test
    fun initialValueIsZero() {
        launchScreen()
        composeTestRule.onNodeWithText("0").assertExists()
    }

    @Test
    fun incrementChangesDisplayedValue() {
        launchScreen()
        composeTestRule.onNodeWithText("+ 0.25").performClick()
        composeTestRule.onNodeWithText("0.25").assertExists()
    }

    @Test
    fun decrementAfterIncrementReturnsToZero() {
        launchScreen()
        composeTestRule.onNodeWithText("+ 0.25").performClick()
        composeTestRule.onNodeWithText("− 0.25").performClick()
        composeTestRule.onNodeWithText("0").assertExists()
    }

    @Test
    fun volumeKeysToggleChangesLabel() {
        launchScreen()
        composeTestRule.onNodeWithText("Lautstärketasten → Lautstärke").performClick()
        composeTestRule.onNodeWithText("Lautstärketasten → Intensität").assertExists()
    }

    @Test
    fun keepScreenOnToggleChangesLabel() {
        launchScreen()
        composeTestRule.onNodeWithText("Bildschirm: auto").performClick()
        composeTestRule.onNodeWithText("Bildschirm: immer an").assertExists()
    }
}
