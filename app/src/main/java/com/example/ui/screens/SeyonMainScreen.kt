package com.example.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import java.util.Locale
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import android.app.Activity
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanLight
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkBorderLight
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GlowGradientEnd
import com.example.ui.theme.GlowGradientStart
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.VioletLight
import com.example.ui.theme.VioletSecondary
import com.example.ui.viewmodel.PreviewDisplayMode
import com.example.ui.viewmodel.SeyonUiState
import com.example.ui.viewmodel.SeyonViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeyonMainScreen(
  viewModel: SeyonViewModel,
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()
  val context = LocalContext.current
  val snackbarHostState = remember { SnackbarHostState() }

  // Photo gallery picker
  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia(),
    onResult = { uri ->
      if (uri != null) {
        viewModel.onReferenceImageSelected(uri)
      }
    }
  )

  // Show snackbar messages
  LaunchedEffect(uiState.statusMessage) {
    uiState.statusMessage?.let {
      snackbarHostState.showSnackbar(it)
      viewModel.clearStatusMessage()
    }
  }

  val configuration = LocalConfiguration.current
  val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
  val activity = context.findActivity()

  val toggleOrientation: () -> Unit = {
    activity?.let { act ->
      val targetOrientation = if (isLandscape) {
        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
      } else {
        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
      }
      act.requestedOrientation = targetOrientation
      Toast.makeText(
        context,
        if (isLandscape) "Switching to Portrait" else "Switching to Landscape",
        Toast.LENGTH_SHORT
      ).show()
    }
  }

  val rotateImage: () -> Unit = {
    viewModel.onRotatePreviewImage()
    Toast.makeText(context, "Rotated image 90°", Toast.LENGTH_SHORT).show()
  }

  // If History screen is opened, display full History Gallery screen
  if (uiState.isHistoryScreenVisible) {
    HistoryScreen(
      historyList = uiState.historyEntities,
      onBack = { viewModel.onCloseHistoryScreen() },
      onOpenItem = { viewModel.onOpenHistoryItem(it) },
      onRegenerateItem = { viewModel.onRegenerateFromHistory(it) },
      onDeleteItem = { viewModel.onDeleteHistoryItem(it) },
      onSaveItem = { item ->
        viewModel.onSaveHistoryItemToGallery(item) { _, message ->
          Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
      },
      onShareItem = { item ->
        val shareIntent = viewModel.createShareIntentForHistory(item)
        if (shareIntent != null) {
          context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Seyon AI Image"))
        } else {
          Toast.makeText(context, "Cannot share this image", Toast.LENGTH_SHORT).show()
        }
      },
      onClearAll = { viewModel.onClearAllHistory() },
      onToggleOrientation = toggleOrientation
    )
    return
  }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    containerColor = DarkBackground,
    snackbarHost = { SnackbarHost(snackbarHostState) },
    topBar = {
      SeyonTopAppBar(
        historyCount = uiState.historyEntities.size,
        isLandscape = isLandscape,
        onToggleOrientation = toggleOrientation,
        onHistoryClicked = { viewModel.onOpenHistoryScreen() },
        onSettingsClicked = { viewModel.onOpenSettings() }
      )
    }
  ) { innerPadding ->
    if (isLandscape) {
      // Landscape Layout: Dual-pane responsive view with preview on left & controls on right
      Row(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
          .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        // Left Column: Visual Stage (Preview, Result Actions, Error Card)
        Column(
          modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .verticalScroll(rememberScrollState()),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          // 1. Showcase Preview Section (Always visible, fully interactive)
          ImagePreviewCard(
            uiState = uiState,
            onPickImage = {
              photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
              )
            },
            onClearImage = { viewModel.onClearReferenceImage() },
            onTabSelected = { mode -> viewModel.setPreviewMode(mode) },
            onToggleOrientation = toggleOrientation,
            onRotateImage = rotateImage
          )

          // Action Buttons (Save, Share, Regenerate) when image is generated
          AnimatedVisibility(
            visible = uiState.generatedBitmap != null && !uiState.isGenerating,
            enter = fadeIn(),
            exit = fadeOut()
          ) {
            GeneratedImageActions(
              onSave = {
                viewModel.onSaveImage { success, message ->
                  Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
              },
              onShare = {
                val shareIntent = viewModel.createShareIntent()
                if (shareIntent != null) {
                  context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Seyon AI Image"))
                } else {
                  Toast.makeText(context, "No generated image to share", Toast.LENGTH_SHORT).show()
                }
              },
              onRegenerate = { viewModel.onRegenerate() },
              onRotate = rotateImage
            )
          }

          // 2. Error & Retry Card when generation encounters an issue
          AnimatedVisibility(
            visible = uiState.errorMessage != null && !uiState.isGenerating,
            enter = fadeIn(),
            exit = fadeOut()
          ) {
            GenerationErrorCard(
              errorMessage = uiState.errorMessage ?: "An unexpected error occurred during generation",
              onRetry = { viewModel.onRetry() },
              onOpenSettings = { viewModel.onOpenSettings() }
            )
          }

          Spacer(modifier = Modifier.height(16.dp))
        }

        // Right Column: Controls Stage (Prompt, Reference, Styles, Ratios, Advanced, Generate Button)
        Column(
          modifier = Modifier
            .weight(1.15f)
            .fillMaxHeight()
            .verticalScroll(rememberScrollState()),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          // 3. PRIMARY & MOST PROMINENT CONTROL: Prompt Input Section
          PromptInputSection(
            prompt = uiState.prompt,
            hasReferenceImage = uiState.referenceBitmap != null,
            isGenerating = uiState.isGenerating,
            errorMessage = uiState.errorMessage,
            suggestions = viewModel.promptSuggestions,
            onPromptChange = { viewModel.onPromptChanged(it) },
            onSuggestionClick = { viewModel.onApplyPromptSuggestion(it) }
          )

          // 4. CLEARLY OPTIONAL: Reference Image Section (Character Consistency)
          OptionalReferenceImageSection(
            referenceBitmap = uiState.referenceBitmap,
            preserveIdentity = uiState.preserveIdentity,
            onPickImage = {
              photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
              )
            },
            onClearImage = { viewModel.onClearReferenceImage() },
            onTogglePreserveIdentity = { viewModel.onTogglePreserveIdentity(it) }
          )

          // 5. Style Preset Selector
          StyleSelectorSection(
            selectedStyle = uiState.selectedStylePreset,
            availableStyles = viewModel.availableStyles,
            onStyleSelected = { viewModel.onSelectStylePreset(it) }
          )

          // 6. Aspect Ratio Selector
          AspectRatioSelectorSection(
            selectedRatio = uiState.selectedAspectRatio,
            availableRatios = viewModel.availableAspectRatios,
            onRatioSelected = { viewModel.onSelectAspectRatio(it) }
          )

          // 7. Advanced Generation Parameters (Seed, Guidance, Steps, Images)
          AdvancedSettingsSection(
            isExpanded = uiState.isAdvancedSettingsExpanded,
            onToggleExpanded = { viewModel.onToggleAdvancedSettings() },
            seedInput = uiState.seedInput,
            onSeedChanged = { viewModel.onSeedChanged(it) },
            onRandomizeSeed = { viewModel.onRandomizeSeed() },
            guidanceScale = uiState.guidanceScale,
            onGuidanceScaleChanged = { viewModel.onGuidanceScaleChanged(it) },
            numInferenceSteps = uiState.numInferenceSteps,
            onInferenceStepsChanged = { viewModel.onInferenceStepsChanged(it) },
            numImages = uiState.numImages,
            onNumImagesChanged = { viewModel.onNumImagesChanged(it) }
          )

          // 8. Large Prominent Generate Button & Progress State
          GenerationControlsSection(
            uiState = uiState,
            onGenerate = { viewModel.onGenerate() },
            onCancel = { viewModel.onCancelGeneration() }
          )

          Spacer(modifier = Modifier.height(24.dp))
        }
      }
    } else {
      // Portrait Layout: Single scrolling column
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {

        // 1. Showcase Preview Section (Always visible, fully interactive)
        ImagePreviewCard(
          uiState = uiState,
          onPickImage = {
            photoPickerLauncher.launch(
              PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
          },
          onClearImage = { viewModel.onClearReferenceImage() },
          onTabSelected = { mode -> viewModel.setPreviewMode(mode) },
          onToggleOrientation = toggleOrientation,
          onRotateImage = rotateImage
        )

        // Action Buttons (Save, Share, Regenerate) when image is generated
        AnimatedVisibility(
          visible = uiState.generatedBitmap != null && !uiState.isGenerating,
          enter = fadeIn(),
          exit = fadeOut()
        ) {
          GeneratedImageActions(
            onSave = {
              viewModel.onSaveImage { success, message ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
              }
            },
            onShare = {
              val shareIntent = viewModel.createShareIntent()
              if (shareIntent != null) {
                context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Seyon AI Image"))
              } else {
                Toast.makeText(context, "No generated image to share", Toast.LENGTH_SHORT).show()
              }
            },
            onRegenerate = { viewModel.onRegenerate() },
            onRotate = rotateImage
          )
        }

        // 2. Error & Retry Card when generation encounters an issue
        AnimatedVisibility(
          visible = uiState.errorMessage != null && !uiState.isGenerating,
          enter = fadeIn(),
          exit = fadeOut()
        ) {
          GenerationErrorCard(
            errorMessage = uiState.errorMessage ?: "An unexpected error occurred during generation",
            onRetry = { viewModel.onRetry() },
            onOpenSettings = { viewModel.onOpenSettings() }
          )
        }

        // 3. PRIMARY & MOST PROMINENT CONTROL: Prompt Input Section
        PromptInputSection(
          prompt = uiState.prompt,
          hasReferenceImage = uiState.referenceBitmap != null,
          isGenerating = uiState.isGenerating,
          errorMessage = uiState.errorMessage,
          suggestions = viewModel.promptSuggestions,
          onPromptChange = { viewModel.onPromptChanged(it) },
          onSuggestionClick = { viewModel.onApplyPromptSuggestion(it) }
        )

        // 4. CLEARLY OPTIONAL: Reference Image Section (Character Consistency)
        OptionalReferenceImageSection(
          referenceBitmap = uiState.referenceBitmap,
          preserveIdentity = uiState.preserveIdentity,
          onPickImage = {
            photoPickerLauncher.launch(
              PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
          },
          onClearImage = { viewModel.onClearReferenceImage() },
          onTogglePreserveIdentity = { viewModel.onTogglePreserveIdentity(it) }
        )

        // 5. Style Preset Selector
        StyleSelectorSection(
          selectedStyle = uiState.selectedStylePreset,
          availableStyles = viewModel.availableStyles,
          onStyleSelected = { viewModel.onSelectStylePreset(it) }
        )

        // 6. Aspect Ratio Selector
        AspectRatioSelectorSection(
          selectedRatio = uiState.selectedAspectRatio,
          availableRatios = viewModel.availableAspectRatios,
          onRatioSelected = { viewModel.onSelectAspectRatio(it) }
        )

        // 7. Advanced Generation Parameters (Seed, Guidance, Steps, Images)
        AdvancedSettingsSection(
          isExpanded = uiState.isAdvancedSettingsExpanded,
          onToggleExpanded = { viewModel.onToggleAdvancedSettings() },
          seedInput = uiState.seedInput,
          onSeedChanged = { viewModel.onSeedChanged(it) },
          onRandomizeSeed = { viewModel.onRandomizeSeed() },
          guidanceScale = uiState.guidanceScale,
          onGuidanceScaleChanged = { viewModel.onGuidanceScaleChanged(it) },
          numInferenceSteps = uiState.numInferenceSteps,
          onInferenceStepsChanged = { viewModel.onInferenceStepsChanged(it) },
          numImages = uiState.numImages,
          onNumImagesChanged = { viewModel.onNumImagesChanged(it) }
        )

        // 8. Large Prominent Generate Button & Progress State
        GenerationControlsSection(
          uiState = uiState,
          onGenerate = { viewModel.onGenerate() },
          onCancel = { viewModel.onCancelGeneration() }
        )

        Spacer(modifier = Modifier.height(16.dp))
      }
    }
  }

  // Settings Dialog for Modular Backend Configuration
  if (uiState.showSettingsDialog) {
    BackendSettingsDialog(
      currentBackendUrl = uiState.backendUrl,
      currentStandaloneMode = uiState.isStandaloneMode,
      isKiroApiKeyConfigured = uiState.isKiroApiKeyConfigured,
      maskedApiKey = uiState.maskedApiKey,
      onDismiss = { viewModel.onDismissSettings() },
      onSave = { url, standalone ->
        viewModel.onSaveSettings(url, standalone)
      }
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeyonTopAppBar(
  historyCount: Int,
  isLandscape: Boolean,
  onToggleOrientation: () -> Unit,
  onHistoryClicked: () -> Unit,
  onSettingsClicked: () -> Unit
) {
  TopAppBar(
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Logo Badge
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
              Brush.linearGradient(
                colors = listOf(CyanPrimary, VioletSecondary)
              )
            ),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
          )
        }

        Column {
          Text(
            text = "Seyon AI",
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.ExtraBold,
              letterSpacing = 0.5.sp
            ),
            color = TextPrimary
          )
          Text(
            text = "Image-to-Image AI Studio",
            style = MaterialTheme.typography.labelSmall,
            color = CyanLight
          )
        }
      }
    },
    actions = {
      // 1-Tap In-App Rotation Switcher (Portrait <-> Landscape)
      IconButton(
        onClick = onToggleOrientation,
        modifier = Modifier.testTag("rotate_screen_button")
      ) {
        Icon(
          imageVector = Icons.Default.ScreenRotation,
          contentDescription = if (isLandscape) "Switch to Portrait" else "Switch to Landscape",
          tint = if (isLandscape) CyanPrimary else TextSecondary
        )
      }

      // History Button with Badge
      Box(modifier = Modifier.padding(end = 4.dp)) {
        IconButton(
          onClick = onHistoryClicked,
          modifier = Modifier.testTag("history_button")
        ) {
          Icon(
            imageVector = Icons.Default.History,
            contentDescription = "Generation History",
            tint = TextSecondary
          )
        }
        if (historyCount > 0) {
          Surface(
            shape = CircleShape,
            color = CyanPrimary,
            modifier = Modifier
              .align(Alignment.TopEnd)
              .padding(top = 4.dp, end = 4.dp)
          ) {
            Text(
              text = if (historyCount > 99) "99+" else historyCount.toString(),
              color = Color.Black,
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
              modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
            )
          }
        }
      }

      IconButton(
        onClick = onSettingsClicked,
        modifier = Modifier.testTag("backend_settings_button")
      ) {
        Icon(
          imageVector = Icons.Default.Settings,
          contentDescription = "Backend Settings",
          tint = TextSecondary
        )
      }
    },
    colors = TopAppBarDefaults.topAppBarColors(
      containerColor = DarkBackground
    )
  )
}

@Composable
fun ImagePreviewCard(
  uiState: SeyonUiState,
  onPickImage: () -> Unit,
  onClearImage: () -> Unit,
  onTabSelected: (PreviewDisplayMode) -> Unit,
  onToggleOrientation: (() -> Unit)? = null,
  onRotateImage: (() -> Unit)? = null
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("image_preview_card"),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = DarkSurface),
    border = CardDefaults.outlinedCardBorder().copy(
      brush = Brush.linearGradient(
        colors = listOf(DarkBorder, DarkBorder.copy(alpha = 0.4f))
      )
    )
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {

      // Mode Switcher Tabs - Always available, fully clickable, touch target >= 44dp
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 12.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(DarkSurfaceElevated)
          .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        PreviewTabItem(
          title = "Preview",
          selected = uiState.previewDisplayMode == PreviewDisplayMode.GENERATED,
          onClick = { onTabSelected(PreviewDisplayMode.GENERATED) },
          modifier = Modifier.weight(1f),
          testTag = "preview_tab"
        )
        PreviewTabItem(
          title = "Reference",
          selected = uiState.previewDisplayMode == PreviewDisplayMode.REFERENCE,
          onClick = { onTabSelected(PreviewDisplayMode.REFERENCE) },
          modifier = Modifier.weight(1f),
          testTag = "preview_tab_reference"
        )
        PreviewTabItem(
          title = "Side-by-Side",
          selected = uiState.previewDisplayMode == PreviewDisplayMode.SPLIT_COMPARE,
          onClick = { onTabSelected(PreviewDisplayMode.SPLIT_COMPARE) },
          modifier = Modifier.weight(1f),
          testTag = "preview_tab_split"
        )
      }

      // Dynamic Preview Aspect Ratio based on user selection
      val previewAspectRatio = when (uiState.selectedAspectRatio) {
        "4:5" -> 4f / 5f
        "16:9" -> 16f / 9f
        "9:16" -> 9f / 16f
        else -> 1f
      }

      // Main Visual Stage
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .aspectRatio(previewAspectRatio)
          .clip(RoundedCornerShape(16.dp))
          .background(DarkSurfaceElevated),
        contentAlignment = Alignment.Center
      ) {
        when (uiState.previewDisplayMode) {
          // 1. Split comparison mode
          PreviewDisplayMode.SPLIT_COMPARE -> {
            if (uiState.generatedBitmap != null && uiState.referenceBitmap != null) {
              Row(modifier = Modifier.fillMaxSize()) {
                Box(
                  modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                ) {
                  Image(
                    bitmap = uiState.referenceBitmap.asImageBitmap(),
                    contentDescription = "Original Reference Photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                  )
                  Surface(
                    modifier = Modifier
                      .padding(8.dp)
                      .align(Alignment.BottomStart),
                    shape = RoundedCornerShape(6.dp),
                    color = Color.Black.copy(alpha = 0.7f)
                  ) {
                    Text(
                      text = "Reference",
                      style = MaterialTheme.typography.labelSmall,
                      color = Color.White,
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                  }
                }

                Box(
                  modifier = Modifier
                    .width(2.dp)
                    .fillMaxSize()
                    .background(CyanLight)
                )

                Box(
                  modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                ) {
                  Image(
                    bitmap = uiState.generatedBitmap.asImageBitmap(),
                    contentDescription = "AI Generated Result",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                  )
                  Surface(
                    modifier = Modifier
                      .padding(8.dp)
                      .align(Alignment.BottomEnd),
                    shape = RoundedCornerShape(6.dp),
                    color = CyanPrimary.copy(alpha = 0.85f)
                  ) {
                    Text(
                      text = "Generated",
                      style = MaterialTheme.typography.labelSmall,
                      color = Color.White,
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                  }
                }
              }
            } else {
              Column(
                modifier = Modifier
                  .fillMaxSize()
                  .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
              ) {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.CompareArrows,
                  contentDescription = null,
                  tint = CyanLight,
                  modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                  text = "Side-by-Side Comparison",
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                  color = TextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = if (uiState.referenceBitmap == null) {
                    "Upload a reference photo and generate an image to compare character consistency side-by-side."
                  } else {
                    "Tap 'Generate' below to create an image and compare it with your reference photo."
                  },
                  style = MaterialTheme.typography.bodySmall,
                  color = TextSecondary,
                  textAlign = TextAlign.Center
                )
                if (uiState.referenceBitmap == null) {
                  Spacer(modifier = Modifier.height(14.dp))
                  Button(
                    onClick = onPickImage,
                    modifier = Modifier.testTag("preview_upload_reference_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                  ) {
                    Icon(imageVector = Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Reference Photo")
                  }
                }
              }
            }
          }

          // 2. Reference Image View
          PreviewDisplayMode.REFERENCE -> {
            if (uiState.referenceBitmap != null) {
              Image(
                bitmap = uiState.referenceBitmap.asImageBitmap(),
                contentDescription = "Reference Image Preview",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
              )

              // Top Badge & Remove Button
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(12.dp)
                  .align(Alignment.TopStart),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = Color.Black.copy(alpha = 0.65f),
                  border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(DarkBorder, DarkBorderLight)))
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                  ) {
                    Icon(
                      imageVector = Icons.Default.Face,
                      contentDescription = null,
                      tint = CyanLight,
                      modifier = Modifier.size(14.dp)
                    )
                    Text(
                      text = "Reference Photo",
                      style = MaterialTheme.typography.labelSmall,
                      color = TextPrimary
                    )
                  }
                }

                IconButton(
                  onClick = onClearImage,
                  modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.7f))
                    .testTag("preview_remove_reference_button")
                ) {
                  Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove Reference Image",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                  )
                }
              }
            } else {
              Column(
                modifier = Modifier
                  .fillMaxSize()
                  .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
              ) {
                Box(
                  modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(DarkSurfaceVariant)
                    .border(1.dp, DarkBorder, CircleShape),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = null,
                    tint = CyanLight,
                    modifier = Modifier.size(32.dp)
                  )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                  text = "No Reference Image Selected",
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                  color = TextPrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                  text = "Upload a portrait or photo to preserve facial identity and transform pose, outfit, or environment.",
                  style = MaterialTheme.typography.bodySmall,
                  color = TextSecondary,
                  textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                  onClick = onPickImage,
                  modifier = Modifier.testTag("preview_upload_reference_button"),
                  shape = RoundedCornerShape(12.dp),
                  colors = ButtonDefaults.buttonColors(
                    containerColor = CyanPrimary
                  ),
                  contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.Upload,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = "Upload Reference Image",
                    fontWeight = FontWeight.SemiBold
                  )
                }
              }
            }
          }

          // 3. Generated / Preview View (Default)
          PreviewDisplayMode.GENERATED -> {
            if (uiState.generatedBitmap != null) {
              Image(
                bitmap = uiState.generatedBitmap.asImageBitmap(),
                contentDescription = "Generated Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
              )
              // Generated Badge
              Surface(
                modifier = Modifier
                  .padding(12.dp)
                  .align(Alignment.TopStart),
                shape = RoundedCornerShape(8.dp),
                color = Color.Black.copy(alpha = 0.65f),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(CyanLight, VioletLight)))
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = CyanLight,
                    modifier = Modifier.size(14.dp)
                  )
                  Text(
                    text = "Seyon AI Synthesis",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = TextPrimary
                  )
                }
              }
            } else {
              Column(
                modifier = Modifier
                  .fillMaxSize()
                  .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
              ) {
                Box(
                  modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(DarkSurfaceVariant)
                    .border(1.dp, DarkBorder, CircleShape),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = CyanLight,
                    modifier = Modifier.size(32.dp)
                  )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                  text = "Seyon AI Preview Stage",
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                  color = TextPrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                  text = "Enter your prompt below and tap Generate to create a new image, or optionally attach a reference photo for character consistency.",
                  style = MaterialTheme.typography.bodySmall,
                  color = TextSecondary,
                  textAlign = TextAlign.Center
                )
              }
            }
          }
        }

        // Generating progress overlay
        if (uiState.isGenerating) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(Color.Black.copy(alpha = 0.75f)),
            contentAlignment = Alignment.Center
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier.padding(24.dp)
            ) {
              CircularProgressIndicator(
                color = CyanLight,
                strokeWidth = 3.5.dp,
                modifier = Modifier.size(52.dp)
              )
              Spacer(modifier = Modifier.height(16.dp))
              Text(
                text = "Generating with Seyon AI...",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
              )
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = uiState.progressStageMessage,
                style = MaterialTheme.typography.bodySmall,
                color = CyanLight,
                textAlign = TextAlign.Center
              )
              Spacer(modifier = Modifier.height(12.dp))
              LinearProgressIndicator(
                progress = { uiState.generationProgress },
                modifier = Modifier
                  .fillMaxWidth(0.8f)
                  .height(6.dp)
                  .clip(RoundedCornerShape(3.dp)),
                color = CyanPrimary,
                trackColor = DarkSurfaceVariant
              )
            }
          }
        }

        // Action buttons directly on the preview stage: Rotate Image 90° & Rotate Screen Orientation
        Row(
          modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(10.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // 90° Image Rotation button (when an active image exists on canvas)
          val hasActiveImage = when (uiState.previewDisplayMode) {
            PreviewDisplayMode.REFERENCE -> uiState.referenceBitmap != null
            PreviewDisplayMode.GENERATED -> uiState.generatedBitmap != null
            PreviewDisplayMode.SPLIT_COMPARE -> uiState.referenceBitmap != null || uiState.generatedBitmap != null
          }
          if (onRotateImage != null && hasActiveImage) {
            IconButton(
              onClick = onRotateImage,
              modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.65f))
                .testTag("preview_rotate_image_button")
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.RotateRight,
                contentDescription = "Rotate Image 90°",
                tint = CyanLight,
                modifier = Modifier.size(20.dp)
              )
            }
          }

          // 1-Tap Screen Orientation Rotate button directly on the preview stage
          if (onToggleOrientation != null) {
            IconButton(
              onClick = onToggleOrientation,
              modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.65f))
                .testTag("preview_rotate_canvas_button")
            ) {
              Icon(
                imageVector = Icons.Default.ScreenRotation,
                contentDescription = "Rotate Screen Orientation",
                tint = CyanLight,
                modifier = Modifier.size(20.dp)
              )
            }
          }
        }
      }

      // Quick change button if reference is loaded
      if (uiState.referenceBitmap != null && !uiState.isGenerating) {
        Spacer(modifier = Modifier.height(10.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End
        ) {
          OutlinedButton(
            onClick = onPickImage,
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(DarkBorder, DarkBorderLight)))
          ) {
            Icon(
              imageVector = Icons.Default.Refresh,
              contentDescription = null,
              tint = CyanLight,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Change Reference Photo",
              style = MaterialTheme.typography.labelMedium,
              color = TextPrimary
            )
          }
        }
      }
    }
  }
}

@Composable
fun PreviewTabItem(
  title: String,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  testTag: String = ""
) {
  val backgroundColor by animateColorAsState(
    targetValue = if (selected) CyanPrimary else Color.Transparent,
    animationSpec = tween(200),
    label = "tab_bg"
  )
  val textColor by animateColorAsState(
    targetValue = if (selected) Color.Black else TextSecondary,
    animationSpec = tween(200),
    label = "tab_text"
  )

  Surface(
    onClick = onClick,
    modifier = modifier
      .defaultMinSize(minHeight = 44.dp)
      .testTag(testTag.ifBlank { "preview_tab_${title.lowercase().replace(" ", "_")}" }),
    shape = RoundedCornerShape(8.dp),
    color = backgroundColor
  ) {
    Box(
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = title,
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
        color = textColor,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

@Composable
fun GeneratedImageActions(
  onSave: () -> Unit,
  onShare: () -> Unit,
  onRegenerate: () -> Unit,
  onRotate: (() -> Unit)? = null
) {
  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Button(
        onClick = onSave,
        modifier = Modifier
          .weight(1f)
          .testTag("save_image_button"),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = DarkSurfaceElevated
        ),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(DarkBorder, DarkBorderLight)))
      ) {
        Icon(
          imageVector = Icons.Default.Download,
          contentDescription = null,
          tint = CyanLight,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "Save to Phone",
          color = TextPrimary,
          style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
        )
      }

      Button(
        onClick = onShare,
        modifier = Modifier
          .weight(1f)
          .testTag("share_image_button"),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = DarkSurfaceElevated
        ),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(DarkBorder, DarkBorderLight)))
      ) {
        Icon(
          imageVector = Icons.Default.Share,
          contentDescription = null,
          tint = VioletLight,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "Share Image",
          color = TextPrimary,
          style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
        )
      }

      if (onRotate != null) {
        IconButton(
          onClick = onRotate,
          modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurfaceElevated)
            .border(
              1.dp,
              Brush.linearGradient(listOf(DarkBorder, DarkBorderLight)),
              RoundedCornerShape(14.dp)
            )
            .testTag("action_rotate_image_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.RotateRight,
            contentDescription = "Rotate Image 90°",
            tint = CyanLight,
            modifier = Modifier.size(20.dp)
          )
        }
      }
    }

    // Regenerate variation button
    OutlinedButton(
      onClick = onRegenerate,
      modifier = Modifier
        .fillMaxWidth()
        .height(48.dp)
        .testTag("regenerate_button"),
      shape = RoundedCornerShape(14.dp),
      colors = ButtonDefaults.outlinedButtonColors(
        containerColor = DarkSurface
      ),
      border = CardDefaults.outlinedCardBorder().copy(
        brush = Brush.linearGradient(listOf(CyanLight.copy(alpha = 0.5f), VioletLight.copy(alpha = 0.5f)))
      )
    ) {
      Icon(
        imageVector = Icons.Default.Refresh,
        contentDescription = null,
        tint = CyanLight,
        modifier = Modifier.size(18.dp)
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = "Regenerate Variation",
        color = TextPrimary,
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
      )
    }
  }
}

@Composable
fun GenerationErrorCard(
  errorMessage: String,
  onRetry: () -> Unit,
  onOpenSettings: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("generation_error_card"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.10f)
    ),
    border = CardDefaults.outlinedCardBorder().copy(
      brush = Brush.linearGradient(
        listOf(
          MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
          MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
        )
      )
    )
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Warning,
          contentDescription = "Error icon",
          tint = MaterialTheme.colorScheme.error,
          modifier = Modifier.size(22.dp)
        )
        Text(
          text = "Generation Issue Encountered",
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
          color = TextPrimary
        )
      }

      Text(
        text = errorMessage,
        style = MaterialTheme.typography.bodySmall,
        color = TextSecondary
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Button(
          onClick = onRetry,
          modifier = Modifier
            .weight(1f)
            .testTag("retry_generation_button"),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error
          )
        ) {
          Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Retry",
            color = Color.White,
            fontWeight = FontWeight.Bold
          )
        }

        OutlinedButton(
          onClick = onOpenSettings,
          modifier = Modifier
            .weight(1f)
            .testTag("error_backend_settings_button"),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.outlinedButtonColors(
            containerColor = DarkSurfaceElevated
          ),
          border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(listOf(DarkBorder, DarkBorderLight))
          )
        ) {
          Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Backend Settings",
            color = TextPrimary
          )
        }
      }
    }
  }
}

@Composable
fun IdentityPreservationCard(
  preserveIdentity: Boolean,
  onToggle: (Boolean) -> Unit
) {
  Card(
    onClick = { onToggle(!preserveIdentity) },
    modifier = Modifier
      .fillMaxWidth()
      .testTag("identity_preservation_card"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = DarkSurface),
    border = CardDefaults.outlinedCardBorder().copy(
      brush = Brush.linearGradient(
        colors = if (preserveIdentity) {
          listOf(CyanLight.copy(alpha = 0.5f), VioletLight.copy(alpha = 0.3f))
        } else {
          listOf(DarkBorder, DarkBorder)
        }
      )
    )
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.weight(1f)
      ) {
        Box(
          modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(
              if (preserveIdentity) CyanPrimary.copy(alpha = 0.18f) else DarkSurfaceVariant
            ),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = if (preserveIdentity) Icons.Default.Lock else Icons.Default.LockOpen,
            contentDescription = null,
            tint = if (preserveIdentity) CyanLight else TextSecondary,
            modifier = Modifier.size(20.dp)
          )
        }

        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "Preserve Identity & Face",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
              color = TextPrimary
            )
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
              shape = RoundedCornerShape(4.dp),
              color = if (preserveIdentity) EmeraldSuccess.copy(alpha = 0.2f) else DarkSurfaceVariant
            ) {
              Text(
                text = if (preserveIdentity) "LOCKED" else "OFF",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                color = if (preserveIdentity) EmeraldSuccess else TextSecondary,
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
              )
            }
          }
          Text(
            text = if (preserveIdentity) {
              "Locks facial appearance while altering pose, clothing & scenery."
            } else {
              "AI may alter facial structures based on prompt."
            },
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
          )
        }
      }

      Spacer(modifier = Modifier.width(8.dp))

      Switch(
        checked = preserveIdentity,
        onCheckedChange = onToggle,
        modifier = Modifier.testTag("preserve_identity_switch"),
        colors = SwitchDefaults.colors(
          checkedThumbColor = Color.White,
          checkedTrackColor = CyanPrimary,
          uncheckedThumbColor = TextSecondary,
          uncheckedTrackColor = DarkSurfaceVariant
        )
      )
    }
  }
}

@Composable
fun PromptInputSection(
  prompt: String,
  hasReferenceImage: Boolean,
  isGenerating: Boolean,
  errorMessage: String?,
  suggestions: List<String>,
  onPromptChange: (String) -> Unit,
  onSuggestionClick: (String) -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("prompt_input_card"),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = DarkSurface),
    border = CardDefaults.outlinedCardBorder().copy(
      brush = Brush.linearGradient(
        listOf(
          CyanPrimary.copy(alpha = 0.5f),
          DarkBorder
        )
      )
    )
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = CyanLight,
            modifier = Modifier.size(20.dp)
          )
          Text(
            text = "Your Creative Prompt",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
          )
        }

        // Active Mode Pill
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = if (hasReferenceImage) CyanPrimary.copy(alpha = 0.2f) else VioletSecondary.copy(alpha = 0.2f),
          border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
              if (hasReferenceImage) listOf(CyanLight, CyanPrimary) else listOf(VioletLight, VioletSecondary)
            )
          )
        ) {
          Text(
            text = if (hasReferenceImage) "Mode 2: Ref + Prompt" else "Mode 1: Prompt Only",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = if (hasReferenceImage) CyanLight else VioletLight,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      OutlinedTextField(
        value = prompt,
        onValueChange = onPromptChange,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("prompt_input_field"),
        minLines = 3,
        maxLines = 6,
        singleLine = false,
        placeholder = {
          Text(
            text = if (hasReferenceImage) {
              "Describe changes to the reference character (e.g. In a tailored black blazer, golden hour studio lighting, luxury modern office, looking confident)..."
            } else {
              "Describe any image to create from scratch (e.g. Futuristic cyberpunk skyline at midnight in rain, neon holograms, hyperrealistic 8k)..."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = TextTertiary
          )
        },
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = DarkSurfaceElevated,
          unfocusedContainerColor = DarkSurfaceElevated,
          focusedBorderColor = CyanLight,
          unfocusedBorderColor = DarkBorder,
          focusedTextColor = TextPrimary,
          unfocusedTextColor = TextPrimary,
          cursorColor = CyanLight
        ),
        keyboardOptions = KeyboardOptions(
          capitalization = KeyboardCapitalization.Sentences,
          imeAction = ImeAction.Default
        ),
        enabled = !isGenerating
      )

      if (errorMessage != null) {
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = errorMessage,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.error
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Quick Inspiration Suggestion Chips
      Text(
        text = "Quick Inspiration",
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
        color = TextSecondary
      )

      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        suggestions.forEach { suggestion ->
          val label = suggestion.substringBefore(": ")
          Surface(
            onClick = { onSuggestionClick(suggestion) },
            modifier = Modifier.defaultMinSize(minHeight = 36.dp),
            shape = RoundedCornerShape(10.dp),
            color = DarkSurfaceElevated,
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(DarkBorder, DarkBorderLight)))
          ) {
            Text(
              text = label,
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
              color = TextPrimary,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
          }
        }
      }
    }
  }
}

@Composable
fun OptionalReferenceImageSection(
  referenceBitmap: Bitmap?,
  preserveIdentity: Boolean,
  onPickImage: () -> Unit,
  onClearImage: () -> Unit,
  onTogglePreserveIdentity: (Boolean) -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("optional_reference_image_card"),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = DarkSurface),
    border = CardDefaults.outlinedCardBorder().copy(
      brush = Brush.linearGradient(
        if (referenceBitmap != null) {
          listOf(CyanLight.copy(alpha = 0.6f), VioletLight.copy(alpha = 0.4f))
        } else {
          listOf(DarkBorder, DarkBorder.copy(alpha = 0.5f))
        }
      )
    )
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(
            imageVector = if (referenceBitmap != null) Icons.Default.Face else Icons.Default.PhotoCamera,
            contentDescription = null,
            tint = CyanLight,
            modifier = Modifier.size(20.dp)
          )
          Text(
            text = if (referenceBitmap != null) "Reference Character Attached" else "Reference Image (Optional)",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
          )
        }

        Surface(
          shape = RoundedCornerShape(6.dp),
          color = if (referenceBitmap != null) CyanPrimary.copy(alpha = 0.2f) else DarkSurfaceElevated,
          border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
              if (referenceBitmap != null) listOf(CyanLight, CyanPrimary) else listOf(DarkBorder, DarkBorderLight)
            )
          )
        ) {
          Text(
            text = if (referenceBitmap != null) "CONSISTENCY MODE" else "OPTIONAL",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              letterSpacing = 0.5.sp
            ),
            color = if (referenceBitmap != null) CyanLight else TextSecondary,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
          )
        }
      }

      if (referenceBitmap == null) {
        Text(
          text = "Upload a reference photo to maintain visual and character consistency across different poses, outfits, and backgrounds. If omitted, Seyon AI generates a completely new image from your prompt alone.",
          style = MaterialTheme.typography.bodySmall,
          color = TextSecondary
        )

        OutlinedButton(
          onClick = onPickImage,
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("upload_reference_button"),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.outlinedButtonColors(
            containerColor = DarkSurfaceElevated
          ),
          border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(listOf(CyanLight.copy(alpha = 0.7f), VioletLight.copy(alpha = 0.5f)))
          )
        ) {
          Icon(
            imageVector = Icons.Default.Upload,
            contentDescription = null,
            tint = CyanLight,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Add Reference Image (Optional)",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
          )
        }
      } else {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceElevated)
            .padding(10.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Image(
            bitmap = referenceBitmap.asImageBitmap(),
            contentDescription = "Selected Reference Photo",
            contentScale = ContentScale.Crop,
            modifier = Modifier
              .size(60.dp)
              .clip(RoundedCornerShape(8.dp))
              .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
          )

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Reference Photo Active",
              style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
              color = TextPrimary
            )
            Text(
              text = "Character identity will guide modifications.",
              style = MaterialTheme.typography.bodySmall,
              color = TextSecondary
            )
          }

          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            IconButton(
              onClick = onPickImage,
              modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(DarkSurfaceVariant)
                .testTag("change_reference_button")
            ) {
              Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Change Reference Image",
                tint = CyanLight,
                modifier = Modifier.size(20.dp)
              )
            }

            IconButton(
              onClick = onClearImage,
              modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(DarkSurfaceVariant)
                .testTag("clear_reference_button")
            ) {
              Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove Reference Image",
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
              )
            }
          }
        }

        // Identity Preservation toggle for Character Consistency
        IdentityPreservationCard(
          preserveIdentity = preserveIdentity,
          onToggle = onTogglePreserveIdentity
        )
      }
    }
  }
}

@Composable
fun StyleSelectorSection(
  selectedStyle: String,
  availableStyles: List<String>,
  onStyleSelected: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("style_selector_card"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = DarkSurface),
    border = CardDefaults.outlinedCardBorder().copy(
      brush = Brush.linearGradient(listOf(DarkBorder, DarkBorder.copy(alpha = 0.4f)))
    )
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Palette,
            contentDescription = null,
            tint = CyanLight,
            modifier = Modifier.size(18.dp)
          )
          Text(
            text = "Artistic Style",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
          )
        }
        Surface(
          shape = RoundedCornerShape(6.dp),
          color = CyanPrimary.copy(alpha = 0.15f),
          border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(listOf(CyanLight.copy(alpha = 0.5f), CyanPrimary.copy(alpha = 0.5f)))
          )
        ) {
          Text(
            text = selectedStyle,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = CyanLight,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        availableStyles.forEach { style ->
          val isSelected = style.equals(selectedStyle, ignoreCase = true)
          val emoji = when (style) {
            "Realistic" -> "📷"
            "Cinematic" -> "🎬"
            "Anime" -> "🌸"
            "3D" -> "🧊"
            "Ghibli-inspired" -> "🍃"
            "Fantasy" -> "🔮"
            "Vintage" -> "🎞️"
            "Watercolor" -> "🎨"
            "Custom" -> "✨"
            else -> "✨"
          }

          val borderColor by animateColorAsState(
            targetValue = if (isSelected) CyanLight else DarkBorder,
            label = "style_border"
          )
          val bgBrush = if (isSelected) {
            Brush.linearGradient(listOf(CyanPrimary.copy(alpha = 0.25f), VioletSecondary.copy(alpha = 0.25f)))
          } else {
            Brush.linearGradient(listOf(DarkSurfaceElevated, DarkSurfaceElevated))
          }

          Surface(
            onClick = { onStyleSelected(style) },
            modifier = Modifier
              .defaultMinSize(minHeight = 44.dp)
              .testTag("style_chip_$style"),
            shape = RoundedCornerShape(12.dp),
            color = Color.Transparent,
            border = CardDefaults.outlinedCardBorder().copy(
              brush = Brush.linearGradient(listOf(borderColor, borderColor))
            )
          ) {
            Box(
              modifier = Modifier
                .background(bgBrush)
                .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Text(text = emoji, fontSize = 16.sp)
                Text(
                  text = style,
                  style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                  ),
                  color = if (isSelected) TextPrimary else TextSecondary
                )
                if (isSelected) {
                  Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = CyanLight,
                    modifier = Modifier.size(14.dp)
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun AspectRatioSelectorSection(
  selectedRatio: String,
  availableRatios: List<String>,
  onRatioSelected: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("aspect_ratio_card"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = DarkSurface),
    border = CardDefaults.outlinedCardBorder().copy(
      brush = Brush.linearGradient(listOf(DarkBorder, DarkBorder.copy(alpha = 0.4f)))
    )
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(
            imageVector = Icons.Default.AspectRatio,
            contentDescription = null,
            tint = CyanLight,
            modifier = Modifier.size(18.dp)
          )
          Text(
            text = "Aspect Ratio",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
          )
        }

        val label = when (selectedRatio) {
          "1:1" -> "Square (1:1)"
          "4:5" -> "Portrait (4:5)"
          "16:9" -> "Landscape (16:9)"
          "9:16" -> "Story / Reel (9:16)"
          else -> selectedRatio
        }
        Text(
          text = label,
          style = MaterialTheme.typography.labelSmall,
          color = CyanLight
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        availableRatios.forEach { ratio ->
          val isSelected = ratio == selectedRatio
          val borderColor by animateColorAsState(
            targetValue = if (isSelected) CyanLight else DarkBorder,
            label = "ratio_border"
          )
          val bgBrush = if (isSelected) {
            Brush.linearGradient(listOf(CyanPrimary.copy(alpha = 0.25f), VioletSecondary.copy(alpha = 0.25f)))
          } else {
            Brush.linearGradient(listOf(DarkSurfaceElevated, DarkSurfaceElevated))
          }

          val ratioName = when (ratio) {
            "1:1" -> "Square"
            "4:5" -> "Portrait"
            "16:9" -> "Cinema"
            "9:16" -> "Story"
            else -> ratio
          }

          Surface(
            onClick = { onRatioSelected(ratio) },
            modifier = Modifier
              .weight(1f)
              .defaultMinSize(minHeight = 64.dp)
              .testTag("aspect_ratio_$ratio"),
            shape = RoundedCornerShape(12.dp),
            color = Color.Transparent,
            border = CardDefaults.outlinedCardBorder().copy(
              brush = Brush.linearGradient(listOf(borderColor, borderColor))
            )
          ) {
            Column(
              modifier = Modifier
                .background(bgBrush)
                .padding(vertical = 10.dp, horizontal = 4.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center
            ) {
              Box(
                modifier = Modifier.size(24.dp),
                contentAlignment = Alignment.Center
              ) {
                val boxModifier = when (ratio) {
                  "1:1" -> Modifier.size(18.dp)
                  "4:5" -> Modifier.size(width = 16.dp, height = 20.dp)
                  "16:9" -> Modifier.size(width = 22.dp, height = 13.dp)
                  "9:16" -> Modifier.size(width = 13.dp, height = 22.dp)
                  else -> Modifier.size(18.dp)
                }
                Box(
                  modifier = boxModifier
                    .border(
                      width = 1.5.dp,
                      color = if (isSelected) CyanLight else TextTertiary,
                      shape = RoundedCornerShape(3.dp)
                    )
                    .background(
                      if (isSelected) CyanLight.copy(alpha = 0.3f) else Color.Transparent,
                      RoundedCornerShape(3.dp)
                    )
                )
              }

              Spacer(modifier = Modifier.height(6.dp))

              Text(
                text = ratio,
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                ),
                color = if (isSelected) TextPrimary else TextSecondary
              )

              Text(
                text = ratioName,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = if (isSelected) CyanLight else TextTertiary
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun AdvancedSettingsSection(
  isExpanded: Boolean,
  onToggleExpanded: () -> Unit,
  seedInput: String,
  onSeedChanged: (String) -> Unit,
  onRandomizeSeed: () -> Unit,
  guidanceScale: Float,
  onGuidanceScaleChanged: (Float) -> Unit,
  numInferenceSteps: Int,
  onInferenceStepsChanged: (Int) -> Unit,
  numImages: Int,
  onNumImagesChanged: (Int) -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("advanced_settings_card"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = DarkSurface),
    border = CardDefaults.outlinedCardBorder().copy(
      brush = Brush.linearGradient(listOf(DarkBorder, DarkBorder.copy(alpha = 0.4f)))
    )
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .defaultMinSize(minHeight = 44.dp)
          .clip(RoundedCornerShape(8.dp))
          .clickable { onToggleExpanded() }
          .testTag("advanced_settings_toggle")
          .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Tune,
            contentDescription = null,
            tint = CyanLight,
            modifier = Modifier.size(18.dp)
          )
          Text(
            text = "Advanced Settings",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
          )
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Text(
            text = if (isExpanded) "Hide" else "Customize",
            style = MaterialTheme.typography.labelSmall,
            color = CyanLight
          )
          Icon(
            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
            contentDescription = if (isExpanded) "Collapse" else "Expand",
            tint = TextSecondary,
            modifier = Modifier.size(20.dp)
          )
        }
      }

      AnimatedVisibility(visible = isExpanded) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp),
          verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          // 1. Seed Setting
          Column {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Generation Seed",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = TextPrimary
              )
              Text(
                text = if (seedInput.isBlank()) "Randomized" else "Fixed: $seedInput",
                style = MaterialTheme.typography.labelSmall,
                color = if (seedInput.isBlank()) CyanLight else VioletLight
              )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              OutlinedTextField(
                value = seedInput,
                onValueChange = onSeedChanged,
                placeholder = { Text("Leave blank for random seed", color = TextTertiary, fontSize = 13.sp) },
                modifier = Modifier
                  .weight(1f)
                  .testTag("seed_input"),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                  focusedContainerColor = DarkSurfaceElevated,
                  unfocusedContainerColor = DarkSurfaceElevated,
                  focusedBorderColor = CyanLight,
                  unfocusedBorderColor = DarkBorder,
                  focusedTextColor = TextPrimary,
                  unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(10.dp)
              )
              Button(
                onClick = onRandomizeSeed,
                modifier = Modifier.testTag("randomize_seed_button"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
                border = CardDefaults.outlinedCardBorder().copy(
                  brush = Brush.linearGradient(listOf(DarkBorder, DarkBorderLight))
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Casino,
                  contentDescription = "Randomize Seed",
                  tint = CyanLight,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "Dice",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                  color = TextPrimary
                )
              }
            }
          }

          // 2. Guidance Scale
          Column {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Guidance Scale (CFG)",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = TextPrimary
              )
              Text(
                text = String.format(Locale.US, "%.1f", guidanceScale),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = CyanLight
              )
            }
            Slider(
              value = guidanceScale,
              onValueChange = onGuidanceScaleChanged,
              valueRange = 1.0f..20.0f,
              steps = 37,
              colors = SliderDefaults.colors(
                thumbColor = CyanPrimary,
                activeTrackColor = CyanLight,
                inactiveTrackColor = DarkSurfaceVariant
              ),
              modifier = Modifier.fillMaxWidth()
            )
            Text(
              text = "Higher values strictly follow prompt details; lower values introduce more creative variation.",
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
              color = TextTertiary
            )
          }

          // 3. Inference Steps
          Column {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Inference Steps",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = TextPrimary
              )
              Text(
                text = "$numInferenceSteps steps",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = CyanLight
              )
            }
            Slider(
              value = numInferenceSteps.toFloat(),
              onValueChange = { onInferenceStepsChanged(it.toInt()) },
              valueRange = 10f..50f,
              steps = 7,
              colors = SliderDefaults.colors(
                thumbColor = CyanPrimary,
                activeTrackColor = CyanLight,
                inactiveTrackColor = DarkSurfaceVariant
              ),
              modifier = Modifier.fillMaxWidth()
            )
            Text(
              text = "More steps yield sharper texture, lighting realism, and facial precision.",
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
              color = TextTertiary
            )
          }

          // 4. Number of Images
          Column {
            Text(
              text = "Batch Output Count",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
              color = TextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              listOf(1, 2, 4).forEach { count ->
                val isSelected = count == numImages
                Surface(
                  onClick = { onNumImagesChanged(count) },
                  modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 40.dp)
                    .testTag("num_images_$count"),
                  shape = RoundedCornerShape(10.dp),
                  color = if (isSelected) CyanPrimary.copy(alpha = 0.2f) else DarkSurfaceElevated,
                  border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(
                      listOf(
                        if (isSelected) CyanLight else DarkBorder,
                        if (isSelected) CyanPrimary else DarkBorder
                      )
                    )
                  )
                ) {
                  Box(
                    modifier = Modifier.padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      text = "$count ${if (count == 1) "Image" else "Images"}",
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                      ),
                      color = if (isSelected) CyanLight else TextSecondary
                    )
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun GenerationControlsSection(
  uiState: SeyonUiState,
  onGenerate: () -> Unit,
  onCancel: () -> Unit
) {
  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val glowAlpha by infiniteTransition.animateFloat(
    initialValue = 0.75f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(1200, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "glow"
  )

  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    if (uiState.isGenerating) {
      // Clear Progress Card
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = CardDefaults.outlinedCardBorder().copy(
          brush = Brush.linearGradient(listOf(CyanLight, CyanPrimary))
        )
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = CyanLight,
                strokeWidth = 2.dp
              )
              Text(
                text = "Generating Image...",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
              )
            }

            Text(
              text = "${(uiState.generationProgress * 100).toInt()}%",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
              color = CyanLight
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          LinearProgressIndicator(
            progress = { uiState.generationProgress },
            modifier = Modifier
              .fillMaxWidth()
              .height(8.dp)
              .clip(RoundedCornerShape(4.dp)),
            color = CyanPrimary,
            trackColor = DarkSurfaceVariant
          )

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = uiState.progressStageMessage.ifBlank { "Processing generation pipeline..." },
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            textAlign = TextAlign.Center
          )
        }
      }

      // Large Prominent Cancel Generation Button
      Button(
        onClick = onCancel,
        modifier = Modifier
          .fillMaxWidth()
          .height(54.dp)
          .testTag("cancel_generation_button"),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
        ),
        border = CardDefaults.outlinedCardBorder().copy(
          brush = Brush.linearGradient(
            listOf(
              MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
              MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
            )
          )
        )
      ) {
        Icon(
          imageVector = Icons.Default.Close,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.error,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Cancel Generation",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.error
        )
      }
    } else {
      // Large Prominent Generate Button
      Button(
        onClick = onGenerate,
        enabled = !uiState.isGenerating,
        modifier = Modifier
          .fillMaxWidth()
          .height(58.dp)
          .testTag("generate_button"),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = Color.Transparent,
          disabledContainerColor = DarkSurfaceVariant
        ),
        contentPadding = PaddingValues(0.dp)
      ) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(
              Brush.horizontalGradient(
                colors = listOf(
                  GlowGradientStart.copy(alpha = glowAlpha),
                  GlowGradientEnd.copy(alpha = glowAlpha)
                )
              )
            ),
          contentAlignment = Alignment.Center
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Icon(
              imageVector = Icons.Default.AutoAwesome,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(24.dp)
            )
            Text(
              text = if (uiState.referenceBitmap != null) "✨ Generate with Reference Photo" else "✨ Generate from Prompt",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
              ),
              color = Color.White
            )
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackendSettingsDialog(
  currentBackendUrl: String,
  currentStandaloneMode: Boolean,
  isKiroApiKeyConfigured: Boolean,
  maskedApiKey: String,
  onDismiss: () -> Unit,
  onSave: (String, Boolean) -> Unit
) {
  var urlInput by rememberSaveable { mutableStateOf(currentBackendUrl) }
  var standaloneChecked by rememberSaveable { mutableStateOf(currentStandaloneMode) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.Settings,
          contentDescription = null,
          tint = CyanLight,
          modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("Kiro API & Backend Config", color = TextPrimary, fontWeight = FontWeight.Bold)
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // Kiro API Credentials Status Card
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
        ) {
          Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(
                imageVector = if (isKiroApiKeyConfigured) Icons.Default.CheckCircle else Icons.Default.Info,
                contentDescription = null,
                tint = if (isKiroApiKeyConfigured) EmeraldSuccess else AmberWarning,
                modifier = Modifier.size(20.dp)
              )
              Text(
                text = if (isKiroApiKeyConfigured) "Kiro API Key Active" else "Kiro API Key Not Configured",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = if (isKiroApiKeyConfigured) EmeraldSuccess else AmberWarning
              )
            }
            Text(
              text = if (isKiroApiKeyConfigured) {
                "Key: $maskedApiKey\nLoaded securely from environment variable (KIRO_API_KEY). Zero keys are hardcoded in the APK."
              } else {
                "Please configure your existing Kiro API key in the AI Studio Secrets panel or .env file as KIRO_API_KEY. No key was detected."
              },
              style = MaterialTheme.typography.bodySmall,
              color = TextSecondary
            )
          }
        }

        Column {
          Text(
            text = "Kiro / Backend API Base URL",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = TextPrimary
          )
          Spacer(modifier = Modifier.height(6.dp))
          OutlinedTextField(
            value = urlInput,
            onValueChange = { urlInput = it },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("backend_url_input_field"),
            shape = RoundedCornerShape(10.dp),
            placeholder = { Text("https://api.xkiro.com/") },
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = DarkSurfaceElevated,
              unfocusedContainerColor = DarkSurfaceElevated,
              focusedBorderColor = CyanLight,
              unfocusedBorderColor = DarkBorder,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            ),
            singleLine = true
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Direct Kiro Endpoint: POST /v1/images/edits (model: gpt-image). Default: https://api.xkiro.com/",
            style = MaterialTheme.typography.labelSmall,
            color = TextTertiary
          )
        }

        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Live Kiro Generation Pipeline",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = TextPrimary
              )
              Text(
                text = if (!standaloneChecked) {
                  "Active: All generation & editing dispatches directly to the configured Kiro API endpoint."
                } else {
                  "Fallback mode."
                },
                style = MaterialTheme.typography.labelSmall,
                color = if (!standaloneChecked) CyanLight else TextSecondary
              )
            }
            Switch(
              checked = !standaloneChecked,
              onCheckedChange = { standaloneChecked = !it },
              modifier = Modifier.testTag("live_backend_switch"),
              colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = CyanPrimary
              )
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = { onSave(urlInput, standaloneChecked) },
        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
      ) {
        Text("Save Configuration", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel", color = TextSecondary)
      }
    },
    containerColor = DarkSurface,
    shape = RoundedCornerShape(18.dp)
  )
}

fun Context.findActivity(): Activity? {
  var currentContext = this
  while (currentContext is ContextWrapper) {
    if (currentContext is Activity) {
      return currentContext
    }
    currentContext = currentContext.baseContext
  }
  return null
}
