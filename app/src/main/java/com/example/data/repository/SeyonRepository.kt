package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import com.example.data.api.SeyonApiService
import com.example.data.api.XkiroRequestValidationInterceptor
import com.example.data.local.AppDatabase
import com.example.data.local.entity.HistoryEntity
import com.example.data.model.GenerationRequest
import com.example.data.model.GenerationResponse
import com.example.data.model.KiroEditJsonRequest
import com.example.data.model.KiroGenerationJsonRequest
import com.example.util.ImageUtils
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.json.JSONObject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

sealed class GenerationStep {
  data class Progress(val percentage: Float, val stageDescription: String) : GenerationStep()
  data class Success(val bitmap: Bitmap, val executionTimeSeconds: Double, val seedUsed: Long) : GenerationStep()
  data class Error(val message: String) : GenerationStep()
}

class SeyonRepository(private val context: Context) {

  private val prefs = context.getSharedPreferences("seyon_ai_prefs", Context.MODE_PRIVATE)
  private val database = AppDatabase.getDatabase(context)
  private val historyDao = database.historyDao()

  val allHistory: Flow<List<HistoryEntity>> = historyDao.getAllHistory()

  suspend fun saveToHistory(
    id: String,
    prompt: String,
    referenceUri: Uri?,
    bitmap: Bitmap,
    preserveIdentity: Boolean,
    stylePreset: String,
    aspectRatio: String,
    seed: Long?,
    guidanceScale: Float,
    numInferenceSteps: Int,
    numImages: Int
  ): HistoryEntity {
    val file = ImageUtils.saveBitmapToInternalStorage(context, bitmap, id)
    val entity = HistoryEntity(
      id = id,
      prompt = prompt,
      referenceImageUri = referenceUri?.toString(),
      localImagePath = file.absolutePath,
      timestamp = System.currentTimeMillis(),
      preserveIdentity = preserveIdentity,
      stylePreset = stylePreset,
      aspectRatio = aspectRatio,
      seed = seed,
      guidanceScale = guidanceScale,
      numInferenceSteps = numInferenceSteps,
      numImages = numImages
    )
    historyDao.insert(entity)
    return entity
  }

  suspend fun deleteHistoryItem(id: String, localImagePath: String) {
    ImageUtils.deleteInternalImage(localImagePath)
    historyDao.deleteById(id)
  }

  suspend fun clearAllHistory() {
    historyDao.clearAll()
  }

  companion object {
    private const val PREF_BACKEND_URL = "backend_url"
    private const val PREF_USE_STANDALONE_MODE = "use_standalone_mode"
    const val DEFAULT_KIRO_API_URL = "https://api.xkiro.com/v1"
    const val DEFAULT_BACKEND_URL = "https://api.xkiro.com/v1"
  }

  fun getKiroApiKey(): String {
    val buildKey = try {
      com.example.BuildConfig.KIRO_API_KEY.trim()
    } catch (e: Throwable) {
      ""
    }
    if (buildKey.isNotBlank() && buildKey != "YOUR_KIRO_API_KEY" && buildKey != "MY_KIRO_API_KEY") {
      return buildKey
    }
    return ""
  }

  fun isKiroApiKeyConfigured(): Boolean {
    val key = getKiroApiKey()
    return key.isNotBlank() && key != "YOUR_KIRO_API_KEY" && key != "MY_KIRO_API_KEY"
  }

  fun getMaskedApiKey(): String {
    val key = getKiroApiKey()
    if (key.length <= 8) return if (key.isNotEmpty()) "••••••••" else "Not Configured"
    return "${key.take(4)}••••••••${key.takeLast(4)}"
  }

  fun getBackendUrl(): String {
    val defaultUrl = try {
      val envUrl = com.example.BuildConfig.KIRO_API_URL.trim()
      if (envUrl.isNotBlank()) envUrl else DEFAULT_KIRO_API_URL
    } catch (e: Throwable) {
      DEFAULT_KIRO_API_URL
    }
    val saved = prefs.getString(PREF_BACKEND_URL, null)?.trim()
    // Do not require manual backend URL in Settings: if unset or legacy default, use environment default
    if (saved.isNullOrBlank() || saved == "https://api.xkiro.com/" || saved == "https://api.xkiro.com") {
      return defaultUrl
    }
    return saved
  }

  fun getNormalizedBaseUrl(): String {
    var url = getBackendUrl().trim()
    if (url.isBlank()) {
      url = DEFAULT_KIRO_API_URL
    }
    if (url.contains("api.xkiro.com") && !url.contains("/v1")) {
      url = if (url.endsWith("/")) "${url}v1/" else "$url/v1/"
    }
    if (!url.endsWith("/")) {
      url = "$url/"
    }
    return url
  }

  fun setBackendUrl(url: String) {
    prefs.edit().putString(PREF_BACKEND_URL, url.trim().let { if (!it.endsWith("/")) "$it/" else it }).apply()
    rebuildApiService()
  }

  fun isStandaloneMode(): Boolean {
    return prefs.getBoolean(PREF_USE_STANDALONE_MODE, false)
  }

  fun setStandaloneMode(enabled: Boolean) {
    prefs.edit().putBoolean(PREF_USE_STANDALONE_MODE, enabled).apply()
  }

  private var apiService: SeyonApiService? = null
  private var okHttpClient: OkHttpClient? = null

  init {
    rebuildApiService()
  }

  private fun extractErrorMessage(errorBodyString: String?, defaultMsg: String): String {
    if (errorBodyString.isNullOrBlank()) return defaultMsg
    return try {
      val json = org.json.JSONObject(errorBodyString)
      if (json.has("error")) {
        val errObj = json.optJSONObject("error")
        if (errObj != null && errObj.has("message")) {
          errObj.getString("message")
        } else {
          json.optString("error", defaultMsg)
        }
      } else if (json.has("message")) {
        json.getString("message")
      } else {
        defaultMsg
      }
    } catch (e: Exception) {
      errorBodyString.take(300)
    }
  }

  private fun rebuildApiService() {
    try {
      val logging = HttpLoggingInterceptor { message ->
        android.util.Log.d("xKiroHttp", message)
      }.apply {
        level = HttpLoggingInterceptor.Level.BODY
      }

      val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor { chain ->
          val original = chain.request()
          val builder = original.newBuilder()
          val apiKey = getKiroApiKey()
          if (apiKey.isNotBlank()) {
            builder.header("Authorization", "Bearer $apiKey")
            builder.header("x-api-key", apiKey)
            builder.header("x-kiro-api-key", apiKey)
          }
          chain.proceed(builder.build())
        }
        .addInterceptor(XkiroRequestValidationInterceptor())
        .addInterceptor(logging)
        .build()

      okHttpClient = client

      val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

      val baseUrlToUse = getNormalizedBaseUrl()
      val retrofit = Retrofit.Builder()
        .baseUrl(baseUrlToUse)
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

      apiService = retrofit.create(SeyonApiService::class.java)
    } catch (e: Exception) {
      android.util.Log.e("SeyonRepository", "Error initializing apiService", e)
      apiService = null
    }
  }

  fun generateImageStream(
    prompt: String,
    referenceUri: Uri?,
    preserveIdentity: Boolean,
    stylePreset: String?,
    aspectRatio: String = "1:1",
    seed: Long? = null,
    guidanceScale: Float = 7.5f,
    numInferenceSteps: Int = 30,
    numImages: Int = 1
  ): Flow<GenerationStep> = flow {
    emit(GenerationStep.Progress(0.08f, "Validating reference image & input prompt..."))
    delay(200)

    val trimmedPrompt = prompt.trim()
    if (trimmedPrompt.isEmpty()) {
      emit(GenerationStep.Error("Prompt required: Please enter a description of the image you want to create or edit."))
      return@flow
    }

    val sourceBitmap: Bitmap? = if (referenceUri != null) {
      ImageUtils.loadBitmapFromUri(context, referenceUri) ?: run {
        emit(GenerationStep.Error("Failed to decode reference image: Please select a valid photo from your gallery."))
        return@flow
      }
    } else {
      null
    }
    val hasReference = sourceBitmap != null

    val apiKey = getKiroApiKey()
    val backendUrl = getBackendUrl()
    val isDirectKiroApi = backendUrl.contains("xkiro", ignoreCase = true)

    if (isDirectKiroApi && !isKiroApiKeyConfigured()) {
      val msg = buildString {
        appendLine("xKiro API Key Required:")
        appendLine("Please configure KIRO_API_KEY securely in the Secrets panel in AI Studio (or in .env).")
        appendLine()
        appendLine("Current Target: $backendUrl")
      }
      emit(GenerationStep.Error(msg.trim()))
      return@flow
    }

    if (hasReference) {
      emit(GenerationStep.Progress(0.20f, "Preparing reference photo & character consistency instructions..."))
    } else {
      emit(GenerationStep.Progress(0.20f, "Preparing prompt-only generation with xKiro sensenova/sensenova-u1.5-lite..."))
    }

    // Compose prompt based on mode
    val finalPrompt = if (hasReference) {
      // MODE 2 — REFERENCE + PROMPT: Maintain visual/character consistency while modifying the image
      val identityInstruction = if (preserveIdentity) {
        "Strict Instruction: Maintain visual consistency and preserve 100% of the reference character's facial identity, facial features, eyes, nose, lips, jawline, and unique likeness as closely as the model supports. Do not alter their age or facial geometry. Apply modifications: $trimmedPrompt"
      } else {
        "Image-to-image transformation based on reference photo. Apply modification: $trimmedPrompt"
      }
      if (!stylePreset.isNullOrBlank() && stylePreset != "Custom") {
        "$identityInstruction. Aesthetic Style: $stylePreset."
      } else {
        identityInstruction
      }
    } else {
      // MODE 1 — PROMPT ONLY: Generate completely new image using natural-language prompt
      if (!stylePreset.isNullOrBlank() && stylePreset != "Custom") {
        "$trimmedPrompt, aesthetic style: $stylePreset, highly detailed, photorealistic quality"
      } else {
        trimmedPrompt
      }
    }

    val sizeStr = when (aspectRatio) {
      "9:16" -> "1024x1792"
      "16:9" -> "1792x1024"
      "4:5" -> "1024x1280"
      "3:2" -> "1536x1024"
      else -> "1024x1024"
    }

    val generationSeed = seed ?: System.currentTimeMillis()
    val startTime = System.currentTimeMillis()

    emit(GenerationStep.Progress(0.35f, "Connecting to xKiro image service..."))

    val service = apiService ?: run {
      emit(GenerationStep.Error("Backend service initialization failed: Please check the Backend URL in Settings ($backendUrl)."))
      return@flow
    }

    try {
      var resultBitmap: Bitmap? = null

      if (!hasReference) {
        // ==========================================
        // MODE 1 — PROMPT ONLY (No reference image)
        // ==========================================
        if (isDirectKiroApi) {
          emit(GenerationStep.Progress(0.45f, "Generating image with xKiro AI (sensenova/sensenova-u1.5-lite)..."))
          // Explicitly construct request ensuring n = 1 strictly as a positive integer.
          // Never send n: 0, null, undefined, empty, negative, decimal, 2, or 4.
          // Do NOT derive n from the batch-output UI value.
          val jsonPayload = JSONObject().apply {
            put("prompt", finalPrompt)
            put("model", "sensenova/sensenova-u1.5-lite")
            put("size", sizeStr)
            put("n", 1) // explicitly set to positive integer 1
            put("response_format", "url")
          }.toString()

          // Log/debug outgoing request structure WITHOUT logging the API key (Requirement 8)
          android.util.Log.i(
            "xKiroApi",
            "Outgoing xKiro request: POST /v1/images/generations, model=sensenova/sensenova-u1.5-lite, n=1, response_format=url, prompt=${finalPrompt.take(40)}..., payload=$jsonPayload"
          )

          val jsonReqBody = jsonPayload.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
          val response = try {
            service.kiroImageGenerationJsonBody(jsonReqBody)
          } catch (e: Exception) {
            android.util.Log.e("xKiroApi", "Exception calling kiroImageGenerationJsonBody: ${e.message}", e)
            null
          }

          if (response != null && response.isSuccessful && response.body() != null) {
            val rawJson = response.body()!!.string()
            resultBitmap = parseKiroResponseAndPoll(rawJson, service) { progress, msg ->
              emit(GenerationStep.Progress(progress, msg))
            }
          } else if (response != null) {
            val errCode = response.code()
            val rawErr = response.errorBody()?.string()
            val parsedMsg = extractErrorMessage(rawErr, response.message())
            val msg = buildString {
              appendLine("xKiro Error (HTTP $errCode):")
              appendLine(parsedMsg)
              if (errCode == 401) {
                appendLine()
                appendLine("Authentication failed: Ensure your xKiro API key is configured in AI Studio Secrets (KIRO_API_KEY).")
              }
            }
            emit(GenerationStep.Error(msg.trim()))
            return@flow
          } else {
            emit(GenerationStep.Error("Failed to connect to xKiro image service. Please check your network and API endpoint."))
            return@flow
          }
        } else {
          // Custom backend proxy (Prompt only)
          emit(GenerationStep.Progress(0.50f, "Dispatching text-to-image request to backend..."))
          val proxyRequest = GenerationRequest(
            prompt = finalPrompt,
            referenceImageBase64 = null,
            preserveIdentity = false,
            stylePreset = stylePreset,
            aspectRatio = aspectRatio,
            seed = generationSeed,
            guidanceScale = guidanceScale,
            numInferenceSteps = numInferenceSteps,
            numImages = 1, // Enforce 1 for xKiro compatibility
            n = 1,          // Enforce 1 strictly
            model = "sensenova/sensenova-u1.5-lite"
          )
          val genResponse = service.generateImage(proxyRequest)

          if (genResponse.isSuccessful && genResponse.body() != null) {
            val body = genResponse.body()!!
            if (!body.imageBase64.isNullOrBlank()) {
              resultBitmap = ImageUtils.base64ToBitmap(body.imageBase64)
            } else if (!body.imageUrl.isNullOrBlank() && okHttpClient != null) {
              resultBitmap = ImageUtils.downloadBitmap(okHttpClient!!, body.imageUrl)
            } else if (!body.error.isNullOrBlank()) {
              emit(GenerationStep.Error("Backend reported error: ${body.error}"))
              return@flow
            }
          } else {
            // If 404, try direct Kiro generations endpoint
            if (genResponse.code() == 404) {
              val kiroJson = JSONObject().apply {
                put("prompt", finalPrompt)
                put("model", "sensenova/sensenova-u1.5-lite")
                put("size", sizeStr)
                put("n", 1) // explicitly positive integer 1
                put("response_format", "url")
              }.toString()
              android.util.Log.i("xKiroApi", "Direct xKiro fallback: POST /v1/images/generations, model=sensenova/sensenova-u1.5-lite, n=1, response_format=url, payload=$kiroJson")
              val kiroReqBody = kiroJson.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
              val kiroRes = service.kiroImageGenerationJsonBody(kiroReqBody)
              if (kiroRes.isSuccessful && kiroRes.body() != null) {
                resultBitmap = parseKiroResponseAndPoll(kiroRes.body()!!.string(), service) { progress, msg ->
                  emit(GenerationStep.Progress(progress, msg))
                }
              }
            }
            if (resultBitmap == null) {
              val errorText = genResponse.errorBody()?.string()?.take(300) ?: genResponse.message()
              emit(GenerationStep.Error("Backend Server Error (HTTP ${genResponse.code()}): $errorText"))
              return@flow
            }
          }
        }
      } else {
        // ==========================================
        // MODE 2 — REFERENCE + PROMPT (Image editing)
        // ==========================================
        val refBitmap = sourceBitmap!!

        if (isDirectKiroApi) {
          emit(GenerationStep.Progress(0.45f, "Processing reference portrait with xKiro AI (gpt-image)..."))
          val pngBytes = ImageUtils.bitmapToByteArray(refBitmap)
          val imageReqBody = pngBytes.toRequestBody("image/png".toMediaTypeOrNull())

          // Build clean multipart form-data without content-type header on integer/text fields
          val multipartBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("image", "reference.png", imageReqBody)
            .addFormDataPart("prompt", finalPrompt)
            .addFormDataPart("model", "gpt-image")
            .addFormDataPart("size", sizeStr)
            .addFormDataPart("n", "1") // Form field: positive integer 1
            .addFormDataPart("response_format", "url")
            .build()

          android.util.Log.i("xKiroApi", "Sending verified Mode 2 multipart request: n=1, response_format=url, prompt=${finalPrompt.take(40)}...")

          var response = try {
            service.kiroImageEditMultipartBody(multipartBody)
          } catch (e: Exception) {
            android.util.Log.e("xKiroApi", "Exception calling kiroImageEditMultipartBody: ${e.message}", e)
            null
          }

          // If multipart returned error (other than 401), also attempt JSON edit endpoint with base64 and n=1
          if ((response == null || !response.isSuccessful) && response?.code() != 401) {
            val base64Img = "data:image/png;base64," + ImageUtils.bitmapToBase64(refBitmap)
            val jsonEditPayload = JSONObject().apply {
              put("prompt", finalPrompt)
              put("image", base64Img)
              put("model", "gpt-image")
              put("size", sizeStr)
              put("n", 1) // explicitly set to positive integer 1
              put("response_format", "url")
            }.toString()

            android.util.Log.i(
              "xKiroApi",
              "Attempting JSON fallback for Mode 2 edits: POST /v1/images/edits, model=gpt-image, n=1, response_format=url, prompt=${finalPrompt.take(40)}..., payload=$jsonEditPayload"
            )

            val jsonReqBody = jsonEditPayload.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
            val jsonResponse = try {
              service.kiroImageEditJsonBody(jsonReqBody)
            } catch (e: Exception) {
              null
            }
            if (jsonResponse != null && jsonResponse.isSuccessful) {
              response = jsonResponse
            }
          }

          if (response != null && response.isSuccessful && response.body() != null) {
            val rawJson = response.body()!!.string()
            resultBitmap = parseKiroResponseAndPoll(rawJson, service) { progress, msg ->
              emit(GenerationStep.Progress(progress, msg))
            }
          } else if (response != null) {
            val errCode = response.code()
            val rawErr = response.errorBody()?.string()
            val parsedMsg = extractErrorMessage(rawErr, response.message())
            val msg = buildString {
              appendLine("xKiro Error (HTTP $errCode):")
              appendLine(parsedMsg)
              if (errCode == 401) {
                appendLine()
                appendLine("Authentication failed: Ensure your xKiro API key is configured in AI Studio Secrets (KIRO_API_KEY).")
              }
            }
            emit(GenerationStep.Error(msg.trim()))
            return@flow
          } else {
            emit(GenerationStep.Error("Failed to connect to xKiro image editing service. Please check your network and API endpoint."))
            return@flow
          }
        } else {
          // Custom backend or proxy service
          emit(GenerationStep.Progress(0.50f, "Dispatching generation request to backend server..."))
          val base64Image = ImageUtils.bitmapToBase64(refBitmap)
          val request = GenerationRequest(
            prompt = finalPrompt,
            referenceImageBase64 = base64Image,
            preserveIdentity = preserveIdentity,
            stylePreset = stylePreset,
            aspectRatio = aspectRatio,
            seed = generationSeed,
            guidanceScale = guidanceScale,
            numInferenceSteps = numInferenceSteps,
            numImages = 1, // Enforce 1 for xKiro compatibility
            n = 1,          // Enforce 1 strictly
            model = "gpt-image"
          )
          val genResponse = service.generateImage(request)

          if (genResponse.isSuccessful && genResponse.body() != null) {
            val body = genResponse.body()!!
            if (!body.imageBase64.isNullOrBlank()) {
              resultBitmap = ImageUtils.base64ToBitmap(body.imageBase64)
            } else if (!body.imageUrl.isNullOrBlank() && okHttpClient != null) {
              resultBitmap = ImageUtils.downloadBitmap(okHttpClient!!, body.imageUrl)
            } else if (!body.error.isNullOrBlank()) {
              emit(GenerationStep.Error("Backend reported error: ${body.error}"))
              return@flow
            }
          } else {
            // If 404, also attempt direct kiroImageEditMultipartBody
            if (genResponse.code() == 404) {
              val pngBytes = ImageUtils.bitmapToByteArray(refBitmap)
              val imageReqBody = pngBytes.toRequestBody("image/png".toMediaTypeOrNull())
              val fallbackMultipart = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", "reference.png", imageReqBody)
                .addFormDataPart("prompt", finalPrompt)
                .addFormDataPart("model", "gpt-image")
                .addFormDataPart("size", sizeStr)
                .addFormDataPart("n", "1")
                .addFormDataPart("response_format", "url")
                .build()
              val multiResponse = service.kiroImageEditMultipartBody(fallbackMultipart)
              if (multiResponse.isSuccessful && multiResponse.body() != null) {
                resultBitmap = parseKiroResponseAndPoll(multiResponse.body()!!.string(), service) { progress, msg ->
                  emit(GenerationStep.Progress(progress, msg))
                }
              }
            }
            if (resultBitmap == null) {
              val errorText = genResponse.errorBody()?.string()?.take(300) ?: genResponse.message()
              val msg = buildString {
                appendLine("Backend Server Error (HTTP ${genResponse.code()}):")
                appendLine(errorText)
                appendLine()
                appendLine("Active Backend URL: $backendUrl")
                appendLine("Check backend server status and URL in Settings.")
              }
              emit(GenerationStep.Error(msg.trim()))
              return@flow
            }
          }
        }
      }

      if (resultBitmap != null) {
        val duration = (System.currentTimeMillis() - startTime) / 1000.0
        emit(GenerationStep.Progress(1.0f, "Finalizing portrait..."))
        emit(GenerationStep.Success(resultBitmap, duration, generationSeed))
      } else {
        emit(GenerationStep.Error("Generation completed without returning an image. Please verify API response format or model availability."))
      }
    } catch (e: java.net.ConnectException) {
      val msg = buildString {
        appendLine("Connection Failed: Cannot reach server at $backendUrl")
        appendLine()
        appendLine("Verify that your Kiro API endpoint or backend server is running and reachable.")
        appendLine("You can configure the backend URL in the Settings menu (gear icon).")
      }
      emit(GenerationStep.Error(msg.trim()))
    } catch (e: java.net.UnknownHostException) {
      val msg = buildString {
        appendLine("Host Not Found: ${e.message}")
        appendLine()
        appendLine("The domain at $backendUrl could not be resolved. Please check your internet connection and backend URL.")
      }
      emit(GenerationStep.Error(msg.trim()))
    } catch (e: java.net.SocketTimeoutException) {
      emit(GenerationStep.Error("Request timed out after waiting for Kiro model generation. Please tap Retry to try again."))
    } catch (e: Exception) {
      val msg = buildString {
        appendLine("Generation Pipeline Error: ${e.localizedMessage ?: e.javaClass.simpleName}")
        appendLine()
        appendLine("Target URL: $backendUrl")
      }
      emit(GenerationStep.Error(msg.trim()))
    }
  }

  private suspend fun parseKiroResponseAndPoll(
    rawJson: String,
    service: SeyonApiService,
    onProgress: (suspend (Float, String) -> Unit)? = null
  ): Bitmap? {
    try {
      val json = org.json.JSONObject(rawJson)

      // Direct data array
      if (json.has("data")) {
        val dataArray = json.optJSONArray("data")
        if (dataArray != null && dataArray.length() > 0) {
          val first = dataArray.optJSONObject(0)
          if (first != null) {
            if (first.has("url") && !first.isNull("url")) {
              val url = first.getString("url")
              val bmp = ImageUtils.downloadBitmap(okHttpClient, url)
              if (bmp != null) return bmp
            }
            if (first.has("b64_json") && !first.isNull("b64_json")) {
              val b64 = first.getString("b64_json")
              val bmp = ImageUtils.base64ToBitmap(b64)
              if (bmp != null) return bmp
            }
          } else {
            val url = dataArray.optString(0, "")
            if (url.isNotBlank()) {
              val bmp = ImageUtils.downloadBitmap(okHttpClient, url)
              if (bmp != null) return bmp
            }
          }
        }
      }

      // Check root level image_base64 / image_url / url
      if (json.has("image_url") && !json.isNull("image_url")) {
        val bmp = ImageUtils.downloadBitmap(okHttpClient, json.getString("image_url"))
        if (bmp != null) return bmp
      }
      if (json.has("url") && !json.isNull("url")) {
        val bmp = ImageUtils.downloadBitmap(okHttpClient, json.getString("url"))
        if (bmp != null) return bmp
      }
      if (json.has("image_base64") && !json.isNull("image_base64")) {
        val bmp = ImageUtils.base64ToBitmap(json.getString("image_base64"))
        if (bmp != null) return bmp
      }

      // Check asynchronous job ID
      val jobId = when {
        json.has("id") -> json.getString("id")
        json.has("job_id") -> json.getString("job_id")
        else -> null
      }

      if (!jobId.isNullOrBlank()) {
        onProgress?.invoke(0.50f, "Generating image with sensenova-u1.5-lite...")
        // Poll for completion (up to 30 attempts x 2.0s = 60 seconds)
        for (attempt in 1..30) {
          delay(2000)
          val progressPct = 0.50f + (attempt * 0.015f).coerceAtMost(0.42f)
          onProgress?.invoke(progressPct, "Rendering image (${attempt * 2}s)...")

          val pollResponse = try {
            val genResp = service.getKiroGenerationJob(jobId)
            if (genResp.isSuccessful) genResp else service.getKiroEditJob(jobId)
          } catch (e: Exception) {
            try {
              service.getKiroEditJob(jobId)
            } catch (e2: Exception) {
              null
            }
          }

          if (pollResponse != null && pollResponse.isSuccessful && pollResponse.body() != null) {
            val pollJson = org.json.JSONObject(pollResponse.body()!!.string())
            val status = pollJson.optString("status", "")

            if (status.equals("completed", ignoreCase = true) || status.equals("succeeded", ignoreCase = true)) {
              if (pollJson.has("data")) {
                val array = pollJson.optJSONArray("data")
                if (array != null && array.length() > 0) {
                  val item = array.optJSONObject(0)
                  if (item != null) {
                    if (item.has("url") && !item.isNull("url")) {
                      val bmp = ImageUtils.downloadBitmap(okHttpClient, item.getString("url"))
                      if (bmp != null) return bmp
                    }
                    if (item.has("b64_json") && !item.isNull("b64_json")) {
                      val bmp = ImageUtils.base64ToBitmap(item.getString("b64_json"))
                      if (bmp != null) return bmp
                    }
                  } else {
                    val itemUrl = array.optString(0, "")
                    if (itemUrl.isNotBlank()) {
                      val bmp = ImageUtils.downloadBitmap(okHttpClient, itemUrl)
                      if (bmp != null) return bmp
                    }
                  }
                }
              }
            } else if (status.equals("failed", ignoreCase = true)) {
              val err = pollJson.optString("error", "Kiro job processing failed")
              throw IllegalStateException(err)
            }
          }
        }
      }
    } catch (e: Exception) {
      e.printStackTrace()
      if (e is IllegalStateException) throw e
    }
    return null
  }
}

