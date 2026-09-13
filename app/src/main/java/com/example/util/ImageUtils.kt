package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import androidx.core.content.FileProvider
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.math.max
import kotlin.math.min

object ImageUtils {

  fun loadBitmapFromUri(context: Context, uri: Uri, maxDim: Int = 1200): Bitmap? {
    return try {
      var input: InputStream? = context.contentResolver.openInputStream(uri) ?: return null
      val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
      BitmapFactory.decodeStream(input, null, options)
      input?.close()

      var sampleSize = 1
      val width = options.outWidth
      val height = options.outHeight
      if (width > maxDim || height > maxDim) {
        val halfHeight = height / 2
        val halfWidth = width / 2
        while ((halfHeight / sampleSize) >= maxDim && (halfWidth / sampleSize) >= maxDim) {
          sampleSize *= 2
        }
      }

      val decodeOptions = BitmapFactory.Options().apply {
        inSampleSize = sampleSize
        inPreferredConfig = Bitmap.Config.ARGB_8888
      }
      input = context.contentResolver.openInputStream(uri)
      val bitmap = BitmapFactory.decodeStream(input, null, decodeOptions)
      input?.close()
      bitmap
    } catch (e: Exception) {
      e.printStackTrace()
      null
    }
  }

  fun bitmapToBase64(bitmap: Bitmap, quality: Int = 85): String {
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
    val byteArray = outputStream.toByteArray()
    return Base64.encodeToString(byteArray, Base64.NO_WRAP)
  }

  fun bitmapToByteArray(bitmap: Bitmap, format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG, quality: Int = 95): ByteArray {
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(format, quality, outputStream)
    return outputStream.toByteArray()
  }

  fun base64ToBitmap(base64: String): Bitmap? {
    return try {
      val cleanBase64 = if (base64.contains(",")) base64.substringAfter(",") else base64
      val decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT)
      BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: Exception) {
      e.printStackTrace()
      null
    }
  }

  fun saveBitmapToGallery(context: Context, bitmap: Bitmap, title: String = "SeyonAI_Image"): Uri? {
    val filename = "${title}_${System.currentTimeMillis()}.png"
    val contentValues = ContentValues().apply {
      put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
      put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/SeyonAI")
        put(MediaStore.MediaColumns.IS_PENDING, 1)
      }
    }

    val resolver = context.contentResolver
    val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

    if (imageUri != null) {
      try {
        resolver.openOutputStream(imageUri)?.use { out ->
          bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
          contentValues.clear()
          contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
          resolver.update(imageUri, contentValues, null, null)
        }
        return imageUri
      } catch (e: Exception) {
        e.printStackTrace()
        try {
          resolver.delete(imageUri, null, null)
        } catch (_: Exception) {}
      }
    }
    return null
  }

  fun saveBitmapToCache(context: Context, bitmap: Bitmap, filename: String = "generated_preview.png"): File {
    val cacheDir = File(context.cacheDir, "images").apply { if (!exists()) mkdirs() }
    val file = File(cacheDir, filename)
    FileOutputStream(file).use { out ->
      bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
    }
    return file
  }

  fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap, id: String): File {
    val dir = File(context.filesDir, "history_images").apply { if (!exists()) mkdirs() }
    val file = File(dir, "seyon_${id}.png")
    FileOutputStream(file).use { out ->
      bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
    }
    return file
  }

  fun loadBitmapFromFile(filePath: String): Bitmap? {
    return try {
      val file = File(filePath)
      if (file.exists()) {
        BitmapFactory.decodeFile(file.absolutePath)
      } else {
        null
      }
    } catch (e: Exception) {
      e.printStackTrace()
      null
    }
  }

  fun deleteInternalImage(filePath: String): Boolean {
    return try {
      val file = File(filePath)
      if (file.exists()) file.delete() else false
    } catch (e: Exception) {
      false
    }
  }

  fun downloadBitmap(okHttpClient: OkHttpClient?, url: String): Bitmap? {
    try {
      if (okHttpClient != null) {
        val request = okhttp3.Request.Builder().url(url).build()
        val response = okHttpClient.newCall(request).execute()
        if (response.isSuccessful) {
          val bytes = response.body?.bytes()
          if (bytes != null && bytes.isNotEmpty()) {
            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            if (bmp != null) return bmp
          }
        }
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
    // Fallback: direct HTTP connection
    return try {
      java.net.URL(url).openStream().use { stream ->
        BitmapFactory.decodeStream(stream)
      }
    } catch (e: Exception) {
      e.printStackTrace()
      null
    }
  }

  fun createShareIntent(context: Context, bitmap: Bitmap, prompt: String): Intent {
    val file = saveBitmapToCache(context, bitmap, "seyon_share_${System.currentTimeMillis()}.png")
    val contentUri = FileProvider.getUriForFile(
      context,
      "${context.packageName}.fileprovider",
      file
    )

    return Intent(Intent.ACTION_SEND).apply {
      type = "image/png"
      putExtra(Intent.EXTRA_STREAM, contentUri)
      putExtra(Intent.EXTRA_SUBJECT, "Generated with Seyon AI")
      putExtra(Intent.EXTRA_TEXT, "Created with Seyon AI: \"$prompt\"")
      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
  }

  /**
   * Generates a stylized artistic rendering directly on the device
   * preserving facial identity while applying dramatic prompt-driven lighting,
   * color grading, and ambient atmosphere for interactive offline and preview testing.
   */
  fun synthesizeArtisticTransformation(
    sourceBitmap: Bitmap,
    prompt: String,
    stylePreset: String?,
    preserveIdentity: Boolean,
    seed: Long = System.currentTimeMillis()
  ): Bitmap {
    val width = sourceBitmap.width
    val height = sourceBitmap.height
    val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)
    val random = java.util.Random(seed)

    val lowerPrompt = prompt.lowercase()
    val isCyberpunk = lowerPrompt.contains("cyber") || lowerPrompt.contains("neon") || stylePreset == "Cyberpunk"
    val isVintage = lowerPrompt.contains("vintage") || lowerPrompt.contains("film") || lowerPrompt.contains("35mm") || stylePreset == "Vintage Film"
    val isOilPainting = lowerPrompt.contains("oil") || lowerPrompt.contains("painting") || lowerPrompt.contains("art") || stylePreset == "Oil Painting"
    val isGoldenHour = lowerPrompt.contains("golden") || lowerPrompt.contains("sunset") || lowerPrompt.contains("warm") || stylePreset == "Golden Hour"
    val isStudio = lowerPrompt.contains("studio") || lowerPrompt.contains("executive") || lowerPrompt.contains("headshot") || stylePreset == "Studio Headshot"

    // 1. Color filter adjustment
    val colorMatrix = ColorMatrix()
    when {
      isCyberpunk -> {
        colorMatrix.set(floatArrayOf(
          1.15f, 0f, 0.25f, 0f, 10f,
          0f, 1.05f, 0.35f, 0f, 5f,
          0.3f, 0.2f, 1.4f, 0f, 25f,
          0f, 0f, 0f, 1f, 0f
        ))
      }
      isVintage -> {
        colorMatrix.set(floatArrayOf(
          1.1f, 0.1f, 0f, 0f, 20f,
          0.1f, 1.0f, 0.1f, 0f, 15f,
          0f, 0.1f, 0.85f, 0f, -10f,
          0f, 0f, 0f, 1f, 0f
        ))
      }
      isGoldenHour -> {
        colorMatrix.set(floatArrayOf(
          1.25f, 0.1f, 0f, 0f, 30f,
          0.1f, 1.15f, 0f, 0f, 20f,
          0f, 0f, 0.8f, 0f, -15f,
          0f, 0f, 0f, 1f, 0f
        ))
      }
      isOilPainting -> {
        // High saturation and contrast
        colorMatrix.setSaturation(1.35f)
      }
      isStudio -> {
        // High dynamic range contrast
        colorMatrix.set(floatArrayOf(
          1.2f, 0f, 0f, 0f, -10f,
          0f, 1.2f, 0f, 0f, -10f,
          0f, 0f, 1.2f, 0f, -10f,
          0f, 0f, 0f, 1f, 0f
        ))
      }
      else -> {
        colorMatrix.setSaturation(1.15f)
      }
    }

    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      colorFilter = ColorMatrixColorFilter(colorMatrix)
    }

    // Draw base filtered image (preserving subject)
    canvas.drawBitmap(sourceBitmap, 0f, 0f, paint)

    // 2. Add atmospheric lighting and styling overlays
    val overlayPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    if (isCyberpunk) {
      // Neon cyan rim light on left, magenta on right
      val cyanGlow = RadialGradient(
        0f, height * 0.4f, width * 0.6f,
        Color.argb(80, 6, 182, 212), Color.TRANSPARENT,
        Shader.TileMode.CLAMP
      )
      overlayPaint.shader = cyanGlow
      canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), overlayPaint)

      val magentaGlow = RadialGradient(
        width.toFloat(), height * 0.6f, width * 0.6f,
        Color.argb(80, 217, 70, 239), Color.TRANSPARENT,
        Shader.TileMode.CLAMP
      )
      overlayPaint.shader = magentaGlow
      canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), overlayPaint)
    } else if (isGoldenHour) {
      // Warm sun flare from top-right corner
      val sunFlare = RadialGradient(
        width * 0.9f, height * 0.1f, width * 0.7f,
        Color.argb(90, 251, 191, 36), Color.TRANSPARENT,
        Shader.TileMode.CLAMP
      )
      overlayPaint.shader = sunFlare
      canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), overlayPaint)
    } else if (isStudio) {
      // Vignette effect to focus on face
      val vignette = RadialGradient(
        width * 0.5f, height * 0.45f, max(width, height) * 0.7f,
        Color.TRANSPARENT, Color.argb(120, 10, 15, 25),
        Shader.TileMode.CLAMP
      )
      overlayPaint.shader = vignette
      canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), overlayPaint)
    } else if (isVintage) {
      // Warm sepia overlay and subtle vignette
      val sepiaGradient = LinearGradient(
        0f, 0f, 0f, height.toFloat(),
        Color.argb(35, 217, 119, 6), Color.argb(45, 120, 53, 15),
        Shader.TileMode.CLAMP
      )
      overlayPaint.shader = sepiaGradient
      canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), overlayPaint)
    }

    // 3. Subtle bottom cinematic gradient bar
    val cinemaGradient = LinearGradient(
      0f, height * 0.8f, 0f, height.toFloat(),
      Color.TRANSPARENT, Color.argb(60, 0, 0, 0),
      Shader.TileMode.CLAMP
    )
    val cinemaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { shader = cinemaGradient }
    canvas.drawRect(0f, height * 0.8f, width.toFloat(), height.toFloat(), cinemaPaint)

    return output
  }

  /**
   * Generates a sample AI portrait if user starts without an uploaded reference
   */
  fun createSampleReferenceAvatar(): Bitmap {
    val size = 720
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // Background gradient
    val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      shader = LinearGradient(
        0f, 0f, size.toFloat(), size.toFloat(),
        Color.parseColor("#1E1B4B"), Color.parseColor("#312E81"),
        Shader.TileMode.CLAMP
      )
    }
    canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), bgPaint)

    // Person silhouette / face placeholder with soft gradients
    val facePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      shader = RadialGradient(
        size * 0.5f, size * 0.45f, size * 0.25f,
        Color.parseColor("#FDE68A"), Color.parseColor("#D97706"),
        Shader.TileMode.CLAMP
      )
    }
    canvas.drawCircle(size * 0.5f, size * 0.42f, size * 0.2f, facePaint)

    // Shoulders
    val shoulderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.parseColor("#4338CA")
    }
    canvas.drawRoundRect(
      size * 0.2f, size * 0.62f, size * 0.8f, size.toFloat(),
      60f, 60f, shoulderPaint
    )

    return bitmap
  }

  /**
   * Rotates a bitmap by the specified degrees (e.g. 90, 180, 270)
   */
  fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
    if (degrees % 360f == 0f) return bitmap
    val matrix = android.graphics.Matrix().apply { postRotate(degrees) }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
  }
}
