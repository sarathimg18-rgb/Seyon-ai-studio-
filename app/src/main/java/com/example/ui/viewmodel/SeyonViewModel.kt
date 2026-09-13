package com.example.ui.viewmodel

import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.HistoryEntity
import com.example.data.repository.GenerationStep
import com.example.data.repository.SeyonRepository
import com.example.util.ImageUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

enum class PreviewDisplayMode {
  GENERATED,
  REFERENCE,
  SPLIT_COMPARE
}

data class SeyonUiState(
  val referenceUri: Uri? = null,
  val referenceBitmap: Bitmap? = null,
  val prompt: String = "",
  val preserveIdentity: Boolean = true,
  val selectedStylePreset: String = "Realistic",
  val selectedAspectRatio: String = "1:1",
  val isAdvancedSettingsExpanded: Boolean = false,
  val seedInput: String = "",
  val guidanceScale: Float = 7.5f,
  val numInferenceSteps: Int = 30,
  val numImages: Int = 1,
  val isGenerating: Boolean = false,
  val generationProgress: Float = 0f,
  val progressStageMessage: String = "",
  val generatedBitmap: Bitmap? = null,
  val generatedUri: Uri? = null,
  val previewDisplayMode: PreviewDisplayMode = PreviewDisplayMode.GENERATED,
  val errorMessage: String? = null,
  val statusMessage: String? = null,
  val backendUrl: String = SeyonRepository.DEFAULT_BACKEND_URL,
  val isStandaloneMode: Boolean = false,
  val isKiroApiKeyConfigured: Boolean = false,
  val maskedApiKey: String = "",
  val showSettingsDialog: Boolean = false,
  val isHistoryScreenVisible: Boolean = false,
  val historyEntities: List<HistoryEntity> = emptyList(),
  val selectedHistoryItem: HistoryEntity? = null
)

class SeyonViewModel(application: Application) : AndroidViewModel(application) {

  private val repository = SeyonRepository(application)

  private val _uiState = MutableStateFlow(
    SeyonUiState(
      backendUrl = repository.getBackendUrl(),
      isStandaloneMode = repository.isStandaloneMode(),
      isKiroApiKeyConfigured = repository.isKiroApiKeyConfigured(),
      maskedApiKey = repository.getMaskedApiKey()
    )
  )
  val uiState: StateFlow<SeyonUiState> = _uiState.asStateFlow()

  private var generationJob: Job? = null

  val availableStyles = listOf(
    "Realistic",
    "Cinematic",
    "Anime",
    "3D",
    "Ghibli-inspired",
    "Fantasy",
    "Vintage",
    "Watercolor",
    "Custom"
  )

  val availableAspectRatios = listOf(
    "1:1",
    "4:5",
    "16:9",
    "9:16"
  )

  val promptSuggestions = listOf(
    "👔 Executive Portrait: Charcoal suit, soft studio rim light, modern glass office",
    "🌆 Cyberpunk Neon: Holographic bomber jacket, Tokyo rain reflections, midnight glow",
    "🌅 Golden Hour Glow: Cinematic warm sunlight, field of lavender, linen shirt",
    "🎞️ 35mm Vintage Film: 1980s film grain, retro leather jacket, moody atmosphere",
    "🎨 Renaissance Oil Painting: Opulent velvet robe, golden embroidery, dramatic chiaroscuro",
    "🚀 Deep Space Explorer: Futuristic helmet visor reflecting galaxy nebulae, sleek tech suit"
  )

  init {
    viewModelScope.launch {
      repository.allHistory.collect { list ->
        _uiState.update { it.copy(historyEntities = list) }
      }
    }
  }

  fun onReferenceImageSelected(uri: Uri) {
    viewModelScope.launch {
      val bitmap = ImageUtils.loadBitmapFromUri(getApplication(), uri)
      _uiState.update {
        it.copy(
          referenceUri = uri,
          referenceBitmap = bitmap,
          previewDisplayMode = PreviewDisplayMode.REFERENCE,
          errorMessage = null
        )
      }
    }
  }

  fun onClearReferenceImage() {
    _uiState.update {
      it.copy(
        referenceUri = null,
        referenceBitmap = null,
        previewDisplayMode = if (it.generatedBitmap != null) PreviewDisplayMode.GENERATED else PreviewDisplayMode.REFERENCE
      )
    }
  }

  fun onRotatePreviewImage(clockwise: Boolean = true) {
    val angle = if (clockwise) 90f else -90f
    _uiState.update { state ->
      when (state.previewDisplayMode) {
        PreviewDisplayMode.REFERENCE -> {
          val rotated = state.referenceBitmap?.let { ImageUtils.rotateBitmap(it, angle) }
          state.copy(referenceBitmap = rotated)
        }
        PreviewDisplayMode.GENERATED -> {
          val rotated = state.generatedBitmap?.let { ImageUtils.rotateBitmap(it, angle) }
          state.copy(generatedBitmap = rotated)
        }
        PreviewDisplayMode.SPLIT_COMPARE -> {
          val refRotated = state.referenceBitmap?.let { ImageUtils.rotateBitmap(it, angle) }
          val genRotated = state.generatedBitmap?.let { ImageUtils.rotateBitmap(it, angle) }
          state.copy(
            referenceBitmap = refRotated ?: state.referenceBitmap,
            generatedBitmap = genRotated ?: state.generatedBitmap
          )
        }
      }
    }
  }

  fun onPromptChanged(newPrompt: String) {
    _uiState.update { it.copy(prompt = newPrompt, errorMessage = null) }
  }

  fun onApplyPromptSuggestion(suggestionText: String) {
    val textToInsert = if (suggestionText.contains(": ")) {
      suggestionText.substringAfter(": ")
    } else {
      suggestionText
    }
    _uiState.update { it.copy(prompt = textToInsert) }
  }

  fun onTogglePreserveIdentity(enabled: Boolean) {
    _uiState.update { it.copy(preserveIdentity = enabled) }
  }

  fun onSelectStylePreset(preset: String) {
    _uiState.update { it.copy(selectedStylePreset = preset) }
  }

  fun onSelectAspectRatio(aspectRatio: String) {
    _uiState.update { it.copy(selectedAspectRatio = aspectRatio) }
  }

  fun onToggleAdvancedSettings() {
    _uiState.update { it.copy(isAdvancedSettingsExpanded = !it.isAdvancedSettingsExpanded) }
  }

  fun onSeedChanged(seed: String) {
    _uiState.update { it.copy(seedInput = seed.filter { ch -> ch.isDigit() }) }
  }

  fun onRandomizeSeed() {
    val randomSeed = (100000000L..999999999L).random()
    _uiState.update { it.copy(seedInput = randomSeed.toString()) }
  }

  fun onGuidanceScaleChanged(scale: Float) {
    _uiState.update { it.copy(guidanceScale = scale) }
  }

  fun onInferenceStepsChanged(steps: Int) {
    _uiState.update { it.copy(numInferenceSteps = steps) }
  }

  fun onNumImagesChanged(num: Int) {
    _uiState.update { it.copy(numImages = num) }
  }

  fun setPreviewMode(mode: PreviewDisplayMode) {
    _uiState.update { it.copy(previewDisplayMode = mode) }
  }

  fun onGenerate() {
    val parsedSeed = _uiState.value.seedInput.toLongOrNull()
    executeGeneration(parsedSeed)
  }

  fun onRegenerate() {
    val newSeed = System.currentTimeMillis() + (1000..9999).random()
    _uiState.update { it.copy(seedInput = newSeed.toString()) }
    executeGeneration(newSeed)
  }

  fun onRetry() {
    val parsedSeed = _uiState.value.seedInput.toLongOrNull()
    executeGeneration(parsedSeed)
  }

  private fun executeGeneration(seed: Long?) {
    val currentState = _uiState.value
    val currentPrompt = currentState.prompt.trim()
    if (currentPrompt.isEmpty()) {
      _uiState.update {
        it.copy(
          errorMessage = "Please enter a prompt describing the image you want to create or edit."
        )
      }
      return
    }

    generationJob?.cancel()
    generationJob = viewModelScope.launch {
      _uiState.update {
        it.copy(
          isGenerating = true,
          generationProgress = 0.05f,
          progressStageMessage = "Initializing generation pipeline...",
          errorMessage = null
        )
      }

      repository.generateImageStream(
        prompt = currentPrompt,
        referenceUri = currentState.referenceUri,
        preserveIdentity = currentState.preserveIdentity,
        stylePreset = currentState.selectedStylePreset,
        aspectRatio = currentState.selectedAspectRatio,
        seed = seed,
        guidanceScale = currentState.guidanceScale,
        numInferenceSteps = currentState.numInferenceSteps,
        numImages = 1 // Enforce n=1 strictly for xKiro API compliance
      ).collect { step ->
        when (step) {
          is GenerationStep.Progress -> {
            _uiState.update {
              it.copy(
                generationProgress = step.percentage,
                progressStageMessage = step.stageDescription
              )
            }
          }
          is GenerationStep.Success -> {
            val historyId = UUID.randomUUID().toString()
            val historyEntity = repository.saveToHistory(
              id = historyId,
              prompt = currentPrompt,
              referenceUri = currentState.referenceUri,
              bitmap = step.bitmap,
              preserveIdentity = currentState.preserveIdentity,
              stylePreset = currentState.selectedStylePreset,
              aspectRatio = currentState.selectedAspectRatio,
              seed = step.seedUsed,
              guidanceScale = currentState.guidanceScale,
              numInferenceSteps = currentState.numInferenceSteps,
              numImages = currentState.numImages
            )

            val cachedFile = ImageUtils.saveBitmapToCache(
              getApplication(),
              step.bitmap,
              "seyon_${historyId}.png"
            )
            val generatedUri = Uri.fromFile(cachedFile)

            _uiState.update {
              it.copy(
                isGenerating = false,
                generationProgress = 1.0f,
                generatedBitmap = step.bitmap,
                generatedUri = generatedUri,
                seedInput = step.seedUsed.toString(),
                previewDisplayMode = PreviewDisplayMode.GENERATED,
                selectedHistoryItem = historyEntity,
                statusMessage = "Image generated & saved to history!",
                errorMessage = null
              )
            }
          }
          is GenerationStep.Error -> {
            _uiState.update {
              it.copy(
                isGenerating = false,
                errorMessage = step.message
              )
            }
          }
        }
      }
    }
  }

  fun onCancelGeneration() {
    generationJob?.cancel()
    _uiState.update {
      it.copy(
        isGenerating = false,
        generationProgress = 0f,
        progressStageMessage = "",
        statusMessage = "Generation cancelled"
      )
    }
  }

  fun onSaveImage(onResult: (Boolean, String) -> Unit) {
    val bitmap = _uiState.value.generatedBitmap
    if (bitmap == null) {
      onResult(false, "No generated image to save")
      return
    }

    viewModelScope.launch {
      val savedUri = ImageUtils.saveBitmapToGallery(getApplication(), bitmap)
      if (savedUri != null) {
        onResult(true, "Image saved to Pictures/SeyonAI")
        _uiState.update { it.copy(statusMessage = "Saved to gallery") }
      } else {
        onResult(false, "Failed to save image to gallery")
      }
    }
  }

  fun createShareIntent(): Intent? {
    val bitmap = _uiState.value.generatedBitmap ?: return null
    val prompt = _uiState.value.prompt
    return ImageUtils.createShareIntent(getApplication(), bitmap, prompt)
  }

  // History Actions
  fun onOpenHistoryScreen() {
    _uiState.update { it.copy(isHistoryScreenVisible = true) }
  }

  fun onCloseHistoryScreen() {
    _uiState.update { it.copy(isHistoryScreenVisible = false) }
  }

  fun onOpenHistoryItem(item: HistoryEntity) {
    viewModelScope.launch {
      val loadedBitmap = ImageUtils.loadBitmapFromFile(item.localImagePath)
      val refUri = item.referenceImageUri?.let { Uri.parse(it) }
      val refBitmap = if (refUri != null) {
        ImageUtils.loadBitmapFromUri(getApplication(), refUri)
      } else null

      _uiState.update {
        it.copy(
          prompt = item.prompt,
          selectedStylePreset = item.stylePreset,
          selectedAspectRatio = item.aspectRatio,
          preserveIdentity = item.preserveIdentity,
          seedInput = item.seed?.toString() ?: "",
          guidanceScale = item.guidanceScale,
          numInferenceSteps = item.numInferenceSteps,
          numImages = item.numImages,
          referenceUri = refUri,
          referenceBitmap = refBitmap,
          generatedBitmap = loadedBitmap,
          previewDisplayMode = PreviewDisplayMode.GENERATED,
          selectedHistoryItem = item,
          isHistoryScreenVisible = false,
          errorMessage = null,
          statusMessage = "Loaded image from history"
        )
      }
    }
  }

  fun onDeleteHistoryItem(item: HistoryEntity) {
    viewModelScope.launch {
      repository.deleteHistoryItem(item.id, item.localImagePath)
      _uiState.update { current ->
        val updatedSelected = if (current.selectedHistoryItem?.id == item.id) null else current.selectedHistoryItem
        current.copy(
          selectedHistoryItem = updatedSelected,
          statusMessage = "Deleted from history"
        )
      }
    }
  }

  fun onSaveHistoryItemToGallery(item: HistoryEntity, onResult: (Boolean, String) -> Unit) {
    viewModelScope.launch {
      val bitmap = ImageUtils.loadBitmapFromFile(item.localImagePath)
      if (bitmap != null) {
        val uri = ImageUtils.saveBitmapToGallery(getApplication(), bitmap)
        if (uri != null) {
          onResult(true, "Image saved to Pictures/SeyonAI")
        } else {
          onResult(false, "Failed to save image")
        }
      } else {
        onResult(false, "Image file not found")
      }
    }
  }

  fun createShareIntentForHistory(item: HistoryEntity): Intent? {
    val bitmap = ImageUtils.loadBitmapFromFile(item.localImagePath) ?: return null
    return ImageUtils.createShareIntent(getApplication(), bitmap, item.prompt)
  }

  fun onRegenerateFromHistory(item: HistoryEntity) {
    onOpenHistoryItem(item)
    val newSeed = System.currentTimeMillis() + (1000..9999).random()
    _uiState.update { it.copy(seedInput = newSeed.toString()) }
    executeGeneration(newSeed)
  }

  fun onClearAllHistory() {
    viewModelScope.launch {
      repository.clearAllHistory()
      _uiState.update { it.copy(statusMessage = "History cleared") }
    }
  }

  fun onOpenSettings() {
    _uiState.update {
      it.copy(
        showSettingsDialog = true,
        isKiroApiKeyConfigured = repository.isKiroApiKeyConfigured(),
        maskedApiKey = repository.getMaskedApiKey()
      )
    }
  }

  fun onDismissSettings() {
    _uiState.update { it.copy(showSettingsDialog = false) }
  }

  fun onSaveSettings(newBackendUrl: String, standaloneMode: Boolean) {
    repository.setBackendUrl(newBackendUrl)
    repository.setStandaloneMode(standaloneMode)
    _uiState.update {
      it.copy(
        backendUrl = repository.getBackendUrl(),
        isStandaloneMode = repository.isStandaloneMode(),
        isKiroApiKeyConfigured = repository.isKiroApiKeyConfigured(),
        maskedApiKey = repository.getMaskedApiKey(),
        showSettingsDialog = false,
        statusMessage = "Settings updated"
      )
    }
  }

  fun clearStatusMessage() {
    _uiState.update { it.copy(statusMessage = null, errorMessage = null) }
  }
}

