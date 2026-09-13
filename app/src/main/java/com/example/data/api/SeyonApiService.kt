package com.example.data.api

import com.example.data.model.GenerationRequest
import com.example.data.model.GenerationResponse
import com.example.data.model.KiroEditJsonRequest
import com.example.data.model.KiroGenerationJsonRequest
import com.example.data.model.KiroImageResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Url

/**
 * Modular API service for Seyon AI image generation and image-to-image editing.
 * Communicates with Kiro AI API (or secure backend proxy) using secure environment
 * credentials (KIRO_API_KEY) without hardcoded keys.
 */
interface SeyonApiService {

  // Kiro Text-to-Image / Generations API (Prompt-only Mode 1)
  @POST("/v1/images/generations")
  suspend fun kiroImageGeneration(
    @Body request: KiroGenerationJsonRequest
  ): Response<ResponseBody>

  @POST("/v1/images/generations")
  suspend fun kiroImageGenerationJsonBody(
    @Body body: RequestBody
  ): Response<ResponseBody>

  // Kiro Image-to-Image / Edits API (MultipartBody: recommended for Reference + Prompt Mode 2)
  @POST("/v1/images/edits")
  suspend fun kiroImageEditMultipartBody(
    @Body body: MultipartBody
  ): Response<ResponseBody>

  // Kiro Image-to-Image / Edits API (Multipart: parts method)
  @Multipart
  @POST("/v1/images/edits")
  suspend fun kiroImageEditMultipart(
    @Part image: MultipartBody.Part,
    @Part("prompt") prompt: RequestBody,
    @Part("model") model: RequestBody,
    @Part("size") size: RequestBody?,
    @Part("n") n: RequestBody?,
    @Part("response_format") responseFormat: RequestBody?
  ): Response<ResponseBody>

  // Kiro Image-to-Image / Edits API (JSON payload)
  @POST("/v1/images/edits")
  suspend fun kiroImageEditJson(
    @Body request: KiroEditJsonRequest
  ): Response<ResponseBody>

  @POST("/v1/images/edits")
  suspend fun kiroImageEditJsonBody(
    @Body body: RequestBody
  ): Response<ResponseBody>

  // Kiro Polling endpoint for asynchronous generation / edit jobs
  @GET("/v1/images/generations/{id}")
  suspend fun getKiroGenerationJob(
    @Path("id") id: String
  ): Response<ResponseBody>

  @GET("/v1/images/edits/{id}")
  suspend fun getKiroEditJob(
    @Path("id") id: String
  ): Response<ResponseBody>

  @GET
  suspend fun getJobByUrl(
    @Url url: String
  ): Response<ResponseBody>

  // Seyon / Backend Proxy API
  @POST("api/v1/generate")
  suspend fun generateImage(
    @Body request: GenerationRequest
  ): Response<GenerationResponse>

  @GET("api/v1/generate/{id}")
  suspend fun getGenerationStatus(
    @Path("id") id: String
  ): Response<GenerationResponse>

  @Multipart
  @POST("api/v1/generate-multipart")
  suspend fun generateImageMultipart(
    @Part image: MultipartBody.Part?,
    @Part("prompt") prompt: RequestBody,
    @Part("preserve_identity") preserveIdentity: RequestBody,
    @Part("style_preset") stylePreset: RequestBody?,
    @Part("aspect_ratio") aspectRatio: RequestBody?
  ): Response<GenerationResponse>

  @GET("api/v1/health")
  suspend fun checkHealth(): Response<Map<String, String>>
}

