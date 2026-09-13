package com.example

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.ui.screens.AspectRatioSelectorSection
import com.example.ui.screens.IdentityPreservationCard
import com.example.ui.screens.ImagePreviewCard
import com.example.ui.screens.PromptInputSection
import com.example.ui.screens.StyleSelectorSection
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.PreviewDisplayMode
import com.example.ui.viewmodel.SeyonUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SeyonInteractiveTouchTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Test
  fun previewTabs_areClickableAndChangeSelectedTab() {
    var currentTab by mutableStateOf(PreviewDisplayMode.GENERATED)

    composeTestRule.setContent {
      MyApplicationTheme {
        ImagePreviewCard(
          uiState = SeyonUiState(previewDisplayMode = currentTab),
          onPickImage = {},
          onClearImage = {},
          onTabSelected = { currentTab = it }
        )
      }
    }

    // Check Preview tab is displayed and clickable
    composeTestRule.onNodeWithTag("preview_tab").assertIsDisplayed().performClick()
    assertEquals(PreviewDisplayMode.GENERATED, currentTab)

    // Click Reference tab
    composeTestRule.onNodeWithTag("preview_tab_reference").assertIsDisplayed().performClick()
    assertEquals(PreviewDisplayMode.REFERENCE, currentTab)

    // Click Side-by-Side tab
    composeTestRule.onNodeWithTag("preview_tab_split").assertIsDisplayed().performClick()
    assertEquals(PreviewDisplayMode.SPLIT_COMPARE, currentTab)
  }

  @Test
  fun promptTextField_isFocusableAndEditable() {
    var promptText by mutableStateOf("")

    composeTestRule.setContent {
      MyApplicationTheme {
        PromptInputSection(
          prompt = promptText,
          hasReferenceImage = false,
          isGenerating = false,
          errorMessage = null,
          suggestions = listOf("Cyberpunk: Neon Tokyo street"),
          onPromptChange = { promptText = it },
          onSuggestionClick = { promptText = it }
        )
      }
    }

    val inputField = composeTestRule.onNodeWithTag("prompt_input_field")
    inputField.assertIsDisplayed()
    inputField.assertIsEnabled()
    inputField.performTextInput("Futuristic astronaut in deep space")

    assertEquals("Futuristic astronaut in deep space", promptText)
  }

  @Test
  fun identityToggle_isClickable() {
    var preserveIdentity by mutableStateOf(true)

    composeTestRule.setContent {
      MyApplicationTheme {
        IdentityPreservationCard(
          preserveIdentity = preserveIdentity,
          onToggle = { preserveIdentity = it }
        )
      }
    }

    composeTestRule.onNodeWithTag("preserve_identity_switch").assertIsDisplayed().performClick()
    assertEquals(false, preserveIdentity)

    composeTestRule.onNodeWithTag("preserve_identity_switch").performClick()
    assertEquals(true, preserveIdentity)
  }

  @Test
  fun styleChips_areClickable() {
    var selectedStyle by mutableStateOf("Realistic")

    composeTestRule.setContent {
      MyApplicationTheme {
        StyleSelectorSection(
          selectedStyle = selectedStyle,
          availableStyles = listOf("Realistic", "Cinematic", "Anime"),
          onStyleSelected = { selectedStyle = it }
        )
      }
    }

    composeTestRule.onNodeWithTag("style_chip_Anime").assertIsDisplayed().performClick()
    assertEquals("Anime", selectedStyle)

    composeTestRule.onNodeWithTag("style_chip_Cinematic").assertIsDisplayed().performClick()
    assertEquals("Cinematic", selectedStyle)
  }

  @Test
  fun aspectRatioChips_areClickable() {
    var selectedRatio by mutableStateOf("1:1")

    composeTestRule.setContent {
      MyApplicationTheme {
        AspectRatioSelectorSection(
          selectedRatio = selectedRatio,
          availableRatios = listOf("1:1", "16:9", "9:16"),
          onRatioSelected = { selectedRatio = it }
        )
      }
    }

    composeTestRule.onNodeWithTag("aspect_ratio_16:9").assertIsDisplayed().performClick()
    assertEquals("16:9", selectedRatio)

    composeTestRule.onNodeWithTag("aspect_ratio_9:16").assertIsDisplayed().performClick()
    assertEquals("9:16", selectedRatio)
  }
}
