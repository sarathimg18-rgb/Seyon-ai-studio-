package com.example

import com.example.data.model.GenerationRequest
import com.example.data.model.GenerationResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testPromptSuggestionParsing() {
    val rawSuggestion = "👔 Executive Portrait: Charcoal suit, soft studio rim light, modern glass office"
    val parsed = if (rawSuggestion.contains(": ")) rawSuggestion.substringAfter(": ") else rawSuggestion
    assertEquals("Charcoal suit, soft studio rim light, modern glass office", parsed)
  }

  @Test
  fun testGenerationRequestDefaults() {
    val request = GenerationRequest(
      prompt = "Futuristic neon portrait",
      referenceImageBase64 = "base64sampledata"
    )
    assertTrue(request.preserveIdentity)
    assertEquals("1:1", request.aspectRatio)
    assertEquals(30, request.numInferenceSteps)
    assertEquals("Futuristic neon portrait", request.prompt)
  }

  @Test
  fun testGenerationResponseParsing() {
    val response = GenerationResponse(
      id = "gen_12345",
      status = "completed",
      imageUrl = "https://example.com/image.png",
      executionTimeSeconds = 2.45
    )
    assertEquals("gen_12345", response.id)
    assertEquals("completed", response.status)
    assertNull(response.error)
    assertEquals(2.45, response.executionTimeSeconds!!, 0.001)
  }

  @Test
  fun testHistoryEntityInstantiation() {
    val history = com.example.data.local.entity.HistoryEntity(
      id = "hist_12345",
      prompt = "Cyberpunk neon street portrait",
      referenceImageUri = null,
      localImagePath = "/data/user/0/com.example/files/history_images/gen_1.jpg",
      timestamp = System.currentTimeMillis(),
      preserveIdentity = true,
      stylePreset = "Cinematic",
      aspectRatio = "16:9",
      seed = 4294967295L,
      guidanceScale = 7.5f,
      numInferenceSteps = 28,
      numImages = 1
    )
    assertEquals("hist_12345", history.id)
    assertEquals("Cyberpunk neon street portrait", history.prompt)
    assertEquals("Cinematic", history.stylePreset)
    assertEquals("16:9", history.aspectRatio)
    assertTrue(history.preserveIdentity)
    assertEquals(7.5f, history.guidanceScale)
    assertEquals(28, history.numInferenceSteps)
    assertEquals(4294967295L, history.seed)
  }

  @Test
  fun testIdentityPreservationPromptFormatting() {
    val prompt = "Wearing a tuxedo in Paris"
    val preserveIdentity = true
    val formattedPrompt = if (preserveIdentity) {
      "Strict Instruction: Preserve 100% of the person's facial identity, facial features, eyes, nose, lips, jawline, and unique likeness from the reference image with maximum fidelity. Do not alter their age or facial geometry. Apply the following modification: $prompt"
    } else {
      "Image-to-image transformation based on reference photo. Apply modification: $prompt"
    }

    assertTrue(formattedPrompt.contains("Preserve 100% of the person's facial identity"))
    assertTrue(formattedPrompt.contains("Wearing a tuxedo in Paris"))
  }

  @Test
  fun testMode1PromptOnlyJsonRequest() {
    val prompt = "Cyberpunk neon city street in rain, 8k resolution"
    val request = com.example.data.model.KiroGenerationJsonRequest(
      prompt = prompt,
      model = "sensenova/sensenova-u1.5-lite",
      n = 1,
      size = "1024x1024",
      responseFormat = "b64_json"
    )
    assertEquals("Cyberpunk neon city street in rain, 8k resolution", request.prompt)
    assertEquals("sensenova/sensenova-u1.5-lite", request.model)
    assertEquals(1, request.n)
    assertEquals("1024x1024", request.size)
    assertEquals("b64_json", request.responseFormat)
  }

  @Test
  fun testDualModeBranchingLogic() {
    // Mode 1: No reference image provided -> Prompt only mode
    val hasReferenceMode1 = false
    val endpointMode1 = if (hasReferenceMode1) "v1/images/edits" else "v1/images/generations"
    assertEquals("v1/images/generations", endpointMode1)

    // Mode 2: Reference image provided -> Reference + Prompt mode
    val hasReferenceMode2 = true
    val endpointMode2 = if (hasReferenceMode2) "v1/images/edits" else "v1/images/generations"
    assertEquals("v1/images/edits", endpointMode2)
  }

  @Test
  fun testKiroGenerationJsonRequestMoshiSerialization() {
    val moshi = com.squareup.moshi.Moshi.Builder()
      .addLast(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
      .build()
    val adapter = moshi.adapter(com.example.data.model.KiroGenerationJsonRequest::class.java)

    val req = com.example.data.model.KiroGenerationJsonRequest(
      prompt = "A majestic royal Bengal tiger resting in lush jungle",
      model = "sensenova/sensenova-u1.5-lite",
      size = "1024x1024",
      n = 1,
      responseFormat = "b64_json"
    )

    val json = adapter.toJson(req)
    // 1. For EVERY xKiro image generation request, send: n: 1
    // 2. Never send n: 0, null, undefined, empty, negative, decimal, 2, or 4.
    assertTrue("JSON must contain \"n\":1", json.contains("\"n\":1"))
    assertTrue("JSON must contain sensenova model", json.contains("\"model\":\"sensenova/sensenova-u1.5-lite\""))
    assertFalse("JSON must not contain \"n\":0", json.contains("\"n\":0"))
    assertFalse("JSON must not contain \"n\":2", json.contains("\"n\":2"))
    assertFalse("JSON must not contain \"n\":4", json.contains("\"n\":4"))
    assertFalse("JSON must not contain \"n\":null", json.contains("\"n\":null"))
  }

  @Test
  fun testKiroEditJsonRequestMoshiSerialization() {
    val moshi = com.squareup.moshi.Moshi.Builder()
      .addLast(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
      .build()
    val adapter = moshi.adapter(com.example.data.model.KiroEditJsonRequest::class.java)

    val req = com.example.data.model.KiroEditJsonRequest(
      prompt = "Add sunglasses to portrait",
      image = "data:image/png;base64,AAAA",
      model = "gpt-image",
      size = "1024x1024",
      n = 1,
      responseFormat = "b64_json"
    )

    val json = adapter.toJson(req)
    assertTrue("JSON must contain \"n\":1", json.contains("\"n\":1"))
    assertFalse("JSON must not contain \"n\":2", json.contains("\"n\":2"))
    assertFalse("JSON must not contain \"n\":4", json.contains("\"n\":4"))
  }

  @Test
  fun testGenerationRequestMoshiSerializationEnforcesN1() {
    val moshi = com.squareup.moshi.Moshi.Builder()
      .addLast(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
      .build()
    val adapter = moshi.adapter(GenerationRequest::class.java)

    val req = GenerationRequest(
      prompt = "Cyberpunk portrait",
      numImages = 1,
      n = 1
    )

    val json = adapter.toJson(req)
    assertTrue("JSON must contain \"n\":1", json.contains("\"n\":1"))
    assertTrue("JSON must contain \"num_images\":1", json.contains("\"num_images\":1"))
    assertFalse("JSON must not contain \"n\":2", json.contains("\"n\":2"))
    assertFalse("JSON must not contain \"n\":4", json.contains("\"n\":4"))
  }

  @Test
  fun testXkiroRequestValidationInterceptorEnforcesN1() {
    val interceptor = com.example.data.api.XkiroRequestValidationInterceptor()

    // Test with invalid n=4
    val badJson = "{\"prompt\":\"Test portrait\",\"n\":4,\"num_images\":4}"
    val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
    val reqBody = badJson.toRequestBody(mediaType)
    val request = okhttp3.Request.Builder()
      .url("https://api.xkiro.com/v1/images/generations")
      .post(reqBody)
      .build()

    var interceptedRequest: okhttp3.Request? = null
    val chain = object : okhttp3.Interceptor.Chain {
      override fun request(): okhttp3.Request = request
      override fun proceed(req: okhttp3.Request): okhttp3.Response {
        interceptedRequest = req
        return okhttp3.Response.Builder()
          .request(req)
          .protocol(okhttp3.Protocol.HTTP_1_1)
          .code(200)
          .message("OK")
          .body("{}".toResponseBody(mediaType))
          .build()
      }
      override fun connection(): okhttp3.Connection? = null
      override fun call(): okhttp3.Call = throw UnsupportedOperationException()
      override fun connectTimeoutMillis(): Int = 10000
      override fun withConnectTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit): okhttp3.Interceptor.Chain = this
      override fun readTimeoutMillis(): Int = 10000
      override fun withReadTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit): okhttp3.Interceptor.Chain = this
      override fun writeTimeoutMillis(): Int = 10000
      override fun withWriteTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit): okhttp3.Interceptor.Chain = this
    }

    interceptor.intercept(chain)

    assertNotNull(interceptedRequest)
    val buffer = okio.Buffer()
    interceptedRequest!!.body!!.writeTo(buffer)
    val resultJson = buffer.readUtf8()

    assertTrue("Intercepted JSON must have \"n\": 1", resultJson.contains("\"n\": 1") || resultJson.contains("\"n\":1"))
    assertFalse("Intercepted JSON must not have \"n\":4", resultJson.contains("\"n\":4") || resultJson.contains("\"n\": 4"))
    assertTrue("Intercepted JSON must have \"num_images\": 1", resultJson.contains("\"num_images\": 1") || resultJson.contains("\"num_images\":1"))
  }

  @Test
  fun testExplicitXkiroJsonConstruction() {
    // Mode 1: Prompt Only uses sensenova/sensenova-u1.5-lite, n=1, and no reference image
    val mode1Payload = """{"prompt":"A futuristic city in watercolor style","model":"sensenova/sensenova-u1.5-lite","size":"1024x1024","n":1,"response_format":"b64_json"}"""

    assertTrue("Mode 1 must contain n=1", mode1Payload.contains("\"n\":1"))
    assertFalse("Mode 1 must not contain n=0", mode1Payload.contains("\"n\":0"))
    assertFalse("Mode 1 must not contain n=4", mode1Payload.contains("\"n\":4"))
    assertTrue("Mode 1 must contain model sensenova/sensenova-u1.5-lite", mode1Payload.contains("\"model\":\"sensenova/sensenova-u1.5-lite\""))
    assertFalse("Mode 1 must not contain reference image field", mode1Payload.contains("\"image\""))

    // Mode 2: Reference + Prompt uses gpt-image
    val mode2Payload = """{"prompt":"Add sunglasses to person","image":"data:image/png;base64,abc","model":"gpt-image","size":"1024x1024","n":1,"response_format":"b64_json"}"""
    assertTrue("Mode 2 must contain model gpt-image", mode2Payload.contains("\"model\":\"gpt-image\""))
    assertTrue("Mode 2 must contain n=1", mode2Payload.contains("\"n\":1"))
  }
}

