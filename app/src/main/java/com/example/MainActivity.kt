package com.example

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.ui.screens.SeyonMainScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.SeyonViewModel

class MainActivity : ComponentActivity() {

  private val viewModel: SeyonViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    try {
      requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
    } catch (e: Throwable) {
      android.util.Log.w("MainActivity", "Orientation could not be set to fullSensor: ${e.message}")
    }
    setContent {
      MyApplicationTheme {
        SeyonMainScreen(
          viewModel = viewModel,
          modifier = Modifier.fillMaxSize()
        )
      }
    }
  }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
  MyApplicationTheme { Greeting("Seyon AI") }
}
