package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
  primary = SukmaMintAccent,
  onPrimary = SukmaSlateDark,
  primaryContainer = SukmaDeepEmerald,
  onPrimaryContainer = Color.White,
  secondary = SukmaMintAccent,
  onSecondary = SukmaSlateDark,
  secondaryContainer = SukmaSlateMedium,
  onSecondaryContainer = Color.White,
  tertiary = SukmaSurgeAmber,
  onTertiary = Color.White,
  background = SukmaBackgroundDark,
  onBackground = Color(0xFFF1F5F9),
  surface = SukmaSurfaceDark,
  onSurface = Color(0xFFF1F5F9),
  surfaceVariant = SukmaSlateLight,
  onSurfaceVariant = Color(0xFFCBD5E1),
  outline = SukmaBorderDark,
  error = SukmaEmergencyRed,
  onError = Color.White
)

private val LightColorScheme = lightColorScheme(
  primary = SukmaEmeraldPrimary,
  onPrimary = Color.White,
  primaryContainer = SukmaMintLight,
  onPrimaryContainer = SukmaDeepEmerald,
  secondary = SukmaDeepEmerald,
  onSecondary = Color.White,
  secondaryContainer = Color(0xFFE2E8F0),
  onSecondaryContainer = SukmaSlateDark,
  tertiary = SukmaSurgeAmber,
  onTertiary = Color.White,
  background = SukmaBackgroundLight,
  onBackground = SukmaSlateDark,
  surface = SukmaSurfaceLight,
  onSurface = SukmaSlateDark,
  surfaceVariant = Color(0xFFF1F5F9),
  onSurfaceVariant = SukmaSlateSubtle,
  outline = SukmaBorderLight,
  error = SukmaEmergencyRed,
  onError = Color.White
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Use Go Sukma Drive distinctive branding by default
  content: @Composable () -> Unit,
) {
  val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      val context = LocalContext.current
      if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    darkTheme -> DarkColorScheme
    else -> LightColorScheme
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}

