package com.flowscale.app

import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.flowscale.app.ui.RatingScreen
import com.flowscale.app.ui.formatRating
import com.flowscale.app.ui.theme.FlowscaleTheme
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class RatingScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var context: Context
    private lateinit var viewModel: RatingViewModel

    private val stepLabel: String get() = formatRating(RatingViewModel.STEP)

    @Before
    fun setUp() {
        val app = InstrumentationRegistry.getInstrumentation()
            .targetContext.applicationContext as FlowscaleApplication
        context = app
        runBlocking { app.database.clearAllTables() }
        app.getSharedPreferences("flowscale_prefs", Context.MODE_PRIVATE).edit().clear().commit()
        viewModel = RatingViewModel(app)
    }

    private fun launchScreen() {
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            FlowscaleTheme {
                RatingScreen(viewModel)
            }
        }
        composeTestRule.mainClock.autoAdvance = true
        composeTestRule.waitForIdle()
    }

    @Test
    fun initialValueIsZero() {
        launchScreen()
        composeTestRule.onNodeWithContentDescription(
            context.getString(R.string.current_intensity_description, formatRating(0.0)),
        ).assertExists()
    }

    @Test
    fun incrementChangesDisplayedValue() {
        launchScreen()
        composeTestRule.onNodeWithText(
            context.getString(R.string.increase_button, stepLabel),
        ).performClick()
        composeTestRule.onNodeWithText(formatRating(RatingViewModel.STEP)).assertExists()
    }

    @Test
    fun decrementAfterIncrementReturnsToZero() {
        launchScreen()
        composeTestRule.onNodeWithText(
            context.getString(R.string.increase_button, stepLabel),
        ).performClick()
        composeTestRule.onNodeWithText(
            context.getString(R.string.decrease_button, stepLabel),
        ).performClick()
        composeTestRule.onNodeWithContentDescription(
            context.getString(R.string.current_intensity_description, formatRating(0.0)),
        ).assertExists()
    }

    @Test
    fun volumeKeysToggleChangesLabel() {
        launchScreen()
        composeTestRule.onNodeWithText(
            context.getString(R.string.volume_keys_volume),
        ).performClick()
        composeTestRule.onNodeWithText(
            context.getString(R.string.volume_keys_intensity),
        ).assertExists()
    }

    @Test
    fun keepScreenOnToggleChangesLabel() {
        launchScreen()
        composeTestRule.onNodeWithText(
            context.getString(R.string.screen_auto),
        ).performClick()
        composeTestRule.onNodeWithText(
            context.getString(R.string.screen_always_on),
        ).assertExists()
    }
}
