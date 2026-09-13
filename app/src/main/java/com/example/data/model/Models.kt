package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GenerationRequest(
  @Json(name = "prompt")
  val prompt: String,

  @Json(name = "reference_image_base64")
  val referenceImageBase64: String? = null,

  @Json(name = "preserve_identity")
  val preserveIdentity: Boolean = true,

  @Json(name = "style_preset")
  val stylePreset: String? = null,

  @Json(name = "aspect_ratio")
  val aspectRatio: String = "1:1",

  @Json(name = "negative_prompt")
  val negativePrompt: String? = null,

  @Json(name = "seed")
  val seed: Long? = null,

  @Json(name = "guidance_scale")
  val guidanceScale: Float? = null,

  @Json(name = "num_inference_steps")
  val numInferenceSteps: Int? = 30,

  @Json(name = "num_images")
  val numImages: Int = 1,

  @Json(name = "n")
  val n: Int = 1,

  @Json(name = "model")
  val model: String? = "sensenova/sensenova-u1.5-lite"
)

@JsonClass(generateAdapter = true)
data class KiroImageItem(
  @Json(name = "url")
  val url: String? = null,
  @Json(name = "b64_json")
  val b64Json: String? = null
)

@JsonClass(generateAdapter = true)
data class KiroErrorDetail(
  @Json(name = "message")
  val message: String? = null,
  @Json(name = "type")
  val type: String? = null,
  @Json(name = "code")
  val code: String? = null
)

@JsonClass(generateAdapter = true)
data class KiroImageResponse(
  @Json(name = "id")
  val id: String? = null,
  @Json(name = "status")
  val status: String? = null,
  @Json(name = "created")
  val created: Long? = null,
  @Json(name = "data")
  val data: List<KiroImageItem>? = null,
  @Json(name = "error")
  val error: KiroErrorDetail? = null
)

@JsonClass(generateAdapter = true)
data class KiroGenerationJsonRequest(
  @Json(name = "prompt")
  val prompt: String,
  @Json(name = "model")
  val model: String = "sensenova/sensenova-u1.5-lite",
  @Json(name = "size")
  val size: String = "1024x1024",
  @Json(name = "n")
  val n: Int = 1,
  @Json(name = "response_format")
  val responseFormat: String = "url"
)

@JsonClass(generateAdapter = true)
data class KiroEditJsonRequest(
  @Json(name = "prompt")
  val prompt: String,
  @Json(name = "image")
  val image: String,
  @Json(name = "model")
  val model: String = "gpt-image",
  @Json(name = "size")
  val size: String = "1024x1024",
  @Json(name = "n")
  val n: Int = 1,
  @Json(name = "response_format")
  val responseFormat: String = "url"
)

@JsonClass(generateAdapter = true)
data class GenerationResponse(
  @Json(name = "id")
  val id: String? = null,

  @Json(name = "status")
  val status: String = "completed",

  @Json(name = "image_url")
  val imageUrl: String? = null,

  @Json(name = "image_base64")
  val imageBase64: String? = null,

  @Json(name = "error")
  val error: String? = null,

  @Json(name = "execution_time_seconds")
  val executionTimeSeconds: Double? = null,

  @Json(name = "seed_used")
  val seedUsed: Long? = null,

  @Json(name = "model")
  val model: String? = "seyon-identity-v1"
)

data class GenerationHistoryItem(
  val id: String,
  val prompt: String,
  val referenceImageUri: String?,
  val generatedImageUri: String,
  val timestamp: Long = System.currentTimeMillis(),
  val preserveIdentity: Boolean = true,
  val styleTag: String? = null,
  val seed: Long? = null
)
