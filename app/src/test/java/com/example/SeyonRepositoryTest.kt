package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.repository.SeyonRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SeyonRepositoryTest {

  private lateinit var repository: SeyonRepository

  @Before
  fun setup() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    repository = SeyonRepository(context)
  }

  @Test
  fun backendUrl_defaultsToV1WithoutUserManualEntry() {
    val url = repository.getBackendUrl()
    assertTrue("Default URL should target v1 endpoint", url.contains("/v1") || url.startsWith("https://api.xkiro.com/v1"))
  }

  @Test
  fun normalizedBaseUrl_hasTrailingSlashAndV1() {
    val normalized = repository.getNormalizedBaseUrl()
    assertTrue("Normalized URL should end with slash", normalized.endsWith("/"))
    assertTrue("Normalized URL should contain /v1/", normalized.contains("/v1/"))
  }

  @Test
  fun setBackendUrl_updatesAndNormalizes() {
    repository.setBackendUrl("https://custom.backend.example.com")
    val current = repository.getBackendUrl()
    assertTrue(current.contains("custom.backend.example.com"))
  }
}
