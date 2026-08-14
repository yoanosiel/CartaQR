package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SleekLightColorScheme = lightColorScheme(
  primary = SleekPurple,
  onPrimary = Color.White,
  primaryContainer = SleekPurpleContainer,
  onPrimaryContainer = SleekOnPurpleContainer,
  secondary = SleekTextSecondary,
  secondaryContainer = SleekAccentPill,
  onSecondaryContainer = SleekTextPrimary,
  background = SleekBg,
  onBackground = SleekTextPrimary,
  surface = SleekSurface,
  onSurface = SleekTextPrimary,
  surfaceVariant = Color.White,
  onSurfaceVariant = SleekTextSecondary,
  outline = SleekCardBorder,
  outlineVariant = SleekSubtleBorder
)

private val SleekDarkColorScheme = darkColorScheme(
  primary = Color(0xFFD0BCFF),
  onPrimary = Color(0xFF381E72),
  primaryContainer = Color(0xFF4F378B),
  onPrimaryContainer = Color(0xFFEADDFF),
  secondary = Color(0xFFCCC2DC),
  secondaryContainer = Color(0xFF4A4458),
  onSecondaryContainer = Color(0xFFE8DEF8),
  background = Color(0xFF1C1B1F),
  onBackground = Color(0xFFE6E1E5),
  surface = Color(0xFF2B2930),
  onSurface = Color(0xFFE6E1E5),
  surfaceVariant = Color(0xFF36343B),
  onSurfaceVariant = Color(0xFFCAC4D0),
  outline = Color(0xFF938F99),
  outlineVariant = Color(0xFF49454F)
)

@Composable
fun CartaQRTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit
) {
  val colors = if (darkTheme) SleekDarkColorScheme else SleekLightColorScheme

  MaterialTheme(
    colorScheme = colors,
    typography = Typography,
    content = content
  )
}
