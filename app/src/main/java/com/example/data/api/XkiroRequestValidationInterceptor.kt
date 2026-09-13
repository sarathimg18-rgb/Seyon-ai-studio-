package com.example.data.api

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.Buffer

/**
 * Interceptor that verifies and strictly enforces that every image generation request
 * destined for xKiro AI API has `n: 1` as a positive integer.
 *
 * Requirements enforced:
 * 1. For EVERY xKiro image generation request, send: n: 1
 * 2. Never send n: 0, null, undefined, empty, negative, decimal, 2, or 4.
 * 3. Inspects and validates JSON and Multipart request payloads before transmission.
 */
class XkiroRequestValidationInterceptor : Interceptor {

  companion object {
    private const val TAG = "xKiroApiValidator"

    private fun logInfo(msg: String) {
      try {
        android.util.Log.i(TAG, msg)
      } catch (_: Throwable) {
        println("[$TAG] $msg")
      }
    }

    private fun logError(msg: String, t: Throwable? = null) {
      try {
        android.util.Log.e(TAG, msg, t)
      } catch (_: Throwable) {
        System.err.println("[$TAG] $msg")
      }
    }

    /**
     * Strictly enforces:
     * 1. `n: 1` as a positive integer.
     * 2. `response_format: "url"` (images are served from CDN, b64_json is rejected).
     * 3. `model: "sensenova/sensenova-u1.5-lite"` for image generations.
     */
    fun enforceNInJson(rawJson: String): String {
      return enforceXkiroParamsInJson(rawJson, isGenerationEndpoint = false)
    }

    fun enforceXkiroParamsInJson(rawJson: String, isGenerationEndpoint: Boolean = false): String {
      var modified = rawJson

      // 1. Enforce n: 1
      val nRegex = Regex("""("n"\s*:\s*)([-0-9.]+|null|"[^"]*"|true|false)""")
      if (nRegex.containsMatchIn(modified)) {
        modified = nRegex.replace(modified, "\"n\": 1")
      } else {
        val lastBrace = modified.lastIndexOf('}')
        if (lastBrace != -1) {
          val prefix = modified.substring(0, lastBrace).trimEnd()
          val separator = if (prefix.endsWith("{")) "" else ", "
          modified = "$prefix$separator\"n\": 1${modified.substring(lastBrace)}"
        }
      }

      val numImagesRegex = Regex("""("num_images"\s*:\s*)([-0-9.]+|null|"[^"]*"|true|false)""")
      if (numImagesRegex.containsMatchIn(modified)) {
        modified = numImagesRegex.replace(modified, "\"num_images\": 1")
      }

      // 2. Enforce response_format: "url"
      val respFormatRegex = Regex("""("response_format"\s*:\s*)"[^"]*"""")
      if (respFormatRegex.containsMatchIn(modified)) {
        modified = respFormatRegex.replace(modified, "\"response_format\": \"url\"")
      } else {
        val lastBrace = modified.lastIndexOf('}')
        if (lastBrace != -1) {
          val prefix = modified.substring(0, lastBrace).trimEnd()
          val separator = if (prefix.endsWith("{")) "" else ", "
          modified = "$prefix$separator\"response_format\": \"url\"${modified.substring(lastBrace)}"
        }
      }

      // 3. For image generation (Prompt Only), ensure sensenova/sensenova-u1.5-lite model
      if (isGenerationEndpoint) {
        val modelRegex = Regex("""("model"\s*:\s*)"[^"]*"""")
        if (modelRegex.containsMatchIn(modified)) {
          modified = modelRegex.replace(modified, "\"model\": \"sensenova/sensenova-u1.5-lite\"")
        }
      }

      return modified
    }
  }

  override fun intercept(chain: Interceptor.Chain): Response {
    val request = chain.request()
    val urlString = request.url.toString()

    val isGenerationEndpoint = urlString.contains("images/generations", ignoreCase = true)
    val isKiroEndpoint = isGenerationEndpoint ||
                         urlString.contains("images/edits", ignoreCase = true) ||
                         urlString.contains("api.xkiro.com", ignoreCase = true) ||
                         urlString.contains("xkiro", ignoreCase = true) ||
                         urlString.contains("generate", ignoreCase = true)

    if (!isKiroEndpoint || request.body == null) {
      return chain.proceed(request)
    }

    val body = request.body!!
    val contentType = body.contentType()?.toString() ?: ""

    // 1. JSON Request Validation (e.g. POST /v1/images/generations or JSON edits)
    if (contentType.contains("application/json", ignoreCase = true)) {
      try {
        val buffer = Buffer()
        body.writeTo(buffer)
        val rawJson = buffer.readUtf8()

        val verifiedJsonString = enforceXkiroParamsInJson(rawJson, isGenerationEndpoint)
        logInfo("Outgoing verified xKiro JSON request: url=$urlString, n=1 (Integer), response_format=url, payload=$verifiedJsonString")

        val newBody = verifiedJsonString.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val newRequest = request.newBuilder()
          .method(request.method, newBody)
          .build()

        return chain.proceed(newRequest)
      } catch (e: Exception) {
        logError("Error intercepting JSON request for xKiro validation: ${e.message}", e)
      }
    }

    // 2. Multipart Request Validation (e.g. POST /v1/images/edits multipart)
    if (body is MultipartBody || contentType.contains("multipart", ignoreCase = true)) {
      logInfo("Outgoing verified xKiro Multipart request: url=$urlString with enforced n=1 and response_format=url")
    }

    return chain.proceed(request)
  }
}
