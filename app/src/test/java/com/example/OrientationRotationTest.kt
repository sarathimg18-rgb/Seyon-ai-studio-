package com.example

import android.content.pm.ActivityInfo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [36])
class OrientationRotationTest {

  @get:Rule
  val composeRule = createAndroidComposeRule<MainActivity>()

  @Test
  fun activity_supportsFullSensorOrientationAndIsNotLockedToPortrait() {
    val activity = composeRule.activity
    assertEquals(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR, activity.requestedOrientation)
  }

  @Test
  fun portrait_and_landscape_screensAreUsableAndRetainPrompt() {
    // 1. In portrait (default): check preview card and prompt input
    composeRule.onNodeWithTag("image_preview_card").assertIsDisplayed()
    composeRule.onNodeWithTag("prompt_input_field").performScrollTo().assertIsDisplayed()
    composeRule.onNodeWithTag("generate_button").assertExists()

    // 2. Type prompt
    val testPrompt = "Cyberpunk portrait with neon glow"
    composeRule.onNodeWithTag("prompt_input_field").performTextInput(testPrompt)
    composeRule.onNodeWithTag("prompt_input_field").assertTextContains(testPrompt)

    // 3. Rotate to Landscape
    composeRule.activityRule.scenario.onActivity { activity ->
      activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }
    composeRule.waitForIdle()

    // 4. In landscape: verify preview card, prompt, and generate button remain accessible and prompt is preserved
    composeRule.onNodeWithTag("image_preview_card").assertExists()
    composeRule.onNodeWithTag("prompt_input_field").assertExists()
    composeRule.onNodeWithTag("prompt_input_field").assertTextContains(testPrompt)
    composeRule.onNodeWithTag("generate_button").assertExists()

    // 5. Rotate back to Portrait
    composeRule.activityRule.scenario.onActivity { activity ->
      activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }
    composeRule.waitForIdle()

    // 6. Verify components still visible and prompt retained
    composeRule.onNodeWithTag("image_preview_card").assertIsDisplayed()
    composeRule.onNodeWithTag("prompt_input_field").performScrollTo().assertIsDisplayed()
    composeRule.onNodeWithTag("prompt_input_field").assertTextContains(testPrompt)
    composeRule.onNodeWithTag("generate_button").assertExists()
  }
}
