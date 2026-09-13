package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SeyonDarkColorScheme = darkColorScheme(
  primary = CyanLight,
  onPrimary = Color(0xFF03141F),
  primaryContainer = Color(0xFF0C354E),
  onPrimaryContainer = Color(0xFFC7E8FA),
  secondary = VioletLight,
  onSecondary = Color(0xFF1E0A3C),
  secondaryContainer = Color(0xFF3B1870),
  onSecondaryContainer = Color(0xFFEADBFF),
  tertiary = IndigoAccent,
  onTertiary = Color.White,
  background = DarkBackground,
  onBackground = TextPrimary,
  surface = DarkSurface,
  onSurface = TextPrimary,
  surfaceVariant = DarkSurfaceVariant,
  onSurfaceVariant = TextSecondary,
  outline = DarkBorder,
  outlineVariant = DarkBorderLight,
  error = CoralError,
  onError = Color.White
)

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = SeyonDarkColorScheme,
    typography = Typography,
    content = content
  )
}
