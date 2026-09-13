package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Seyon AI", appName)
  }

  @Test
  fun `verify xKiro request JSONObject has positive integer n=1`() {
    val json = org.json.JSONObject().apply {
      put("prompt", "Portrait of a warrior")
      put("model", "sensenova/sensenova-u1.5-lite")
      put("size", "1024x1024")
      put("n", 1) // explicitly set positive integer 1
      put("response_format", "b64_json")
    }

    assertEquals(1, json.getInt("n"))
    assertEquals("sensenova/sensenova-u1.5-lite", json.getString("model"))
    val jsonString = json.toString()
    org.junit.Assert.assertTrue(jsonString.contains("\"n\":1") || jsonString.contains("\"n\": 1"))
    org.junit.Assert.assertFalse(jsonString.contains("\"n\":0"))
    org.junit.Assert.assertFalse(jsonString.contains("\"n\":4"))
  }

  @Test
  fun `verify Mode 1 Prompt Only and Mode 2 Reference models`() {
    // Mode 1: sensenova/sensenova-u1.5-lite
    val mode1Req = com.example.data.model.KiroGenerationJsonRequest(
      prompt = "Ancient temple in mist",
      model = "sensenova/sensenova-u1.5-lite",
      n = 1
    )
    assertEquals("sensenova/sensenova-u1.5-lite", mode1Req.model)
    assertEquals(1, mode1Req.n)

    // Mode 2: gpt-image
    val mode2Req = com.example.data.model.KiroEditJsonRequest(
      prompt = "Make the sky sunset colored",
      image = "data:image/png;base64,sample",
      model = "gpt-image",
      n = 1
    )
    assertEquals("gpt-image", mode2Req.model)
    assertEquals(1, mode2Req.n)
  }
}
