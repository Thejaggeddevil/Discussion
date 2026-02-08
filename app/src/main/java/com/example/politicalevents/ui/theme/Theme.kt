package com.example.politicalevents.ui.theme


import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Material 3 Black & White Color Palette
 * Clean, neutral, trustworthy design for civic engagement
 */

// Light Theme Colors
private val LightBackground = Color(0xFFFFFFFF)
private val LightForeground = Color(0xFF000000)
private val LightPrimary = Color(0xFF000000)
private val LightPrimaryForeground = Color(0xFFFFFFFF)
private val LightSecondary = Color(0xFFF2F2F2)
private val LightSecondaryForeground = Color(0xFF000000)
private val LightTertiary = Color(0xFF333333)
private val LightTertiaryForeground = Color(0xFFFFFFFF)
private val LightMuted = Color(0xFFD9D9D9)
private val LightMutedForeground = Color(0xFF595959)
private val LightBorder = Color(0xFFE5E5E5)
private val LightError = Color(0xFFDC2626)
private val LightErrorContainer = Color(0xFFFEE2E2)

// Dark Theme Colors
private val DarkBackground = Color(0xFF0A0A0A)
private val DarkForeground = Color(0xFFFFFFFF)
private val DarkPrimary = Color(0xFFFFFFFF)
private val DarkPrimaryForeground = Color(0xFF000000)
private val DarkSecondary = Color(0xFF2D2D2D)
private val DarkSecondaryForeground = Color(0xFFFFFFFF)
private val DarkTertiary = Color(0xFF1F1F1F)
private val DarkTertiaryForeground = Color(0xFFFFFFFF)
private val DarkMuted = Color(0xFF525252)
private val DarkMutedForeground = Color(0xFFB3B3B3)
private val DarkBorder = Color(0xFF333333)
private val DarkError = Color(0xFFEF4444)
private val DarkErrorContainer = Color(0xFF7F1D1D)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightPrimaryForeground,
    primaryContainer = LightSecondary,
    onPrimaryContainer = LightSecondaryForeground,
    secondary = LightSecondary,
    onSecondary = LightSecondaryForeground,
    secondaryContainer = LightMuted,
    onSecondaryContainer = LightMutedForeground,
    tertiary = LightTertiary,
    onTertiary = LightTertiaryForeground,
    tertiaryContainer = LightSecondary,
    onTertiaryContainer = LightSecondaryForeground,
    error = LightError,
    onError = Color.White,
    errorContainer = LightErrorContainer,
    onErrorContainer = LightError,
    background = LightBackground,
    onBackground = LightForeground,
    surface = Color.White,
    onSurface = LightForeground,
    surfaceVariant = LightSecondary,
    onSurfaceVariant = LightMutedForeground,
    outline = LightBorder,
    outlineVariant = LightMuted,
    scrim = Color.Black.copy(alpha = 0.32f),
    inverseSurface = DarkBackground,
    inverseOnSurface = DarkForeground,
    inversePrimary = DarkPrimary
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkPrimaryForeground,
    primaryContainer = DarkSecondary,
    onPrimaryContainer = DarkSecondaryForeground,
    secondary = DarkSecondary,
    onSecondary = DarkSecondaryForeground,
    secondaryContainer = DarkTertiary,
    onSecondaryContainer = DarkTertiaryForeground,
    tertiary = DarkTertiary,
    onTertiary = DarkTertiaryForeground,
    tertiaryContainer = DarkSecondary,
    onTertiaryContainer = DarkSecondaryForeground,
    error = DarkError,
    onError = Color.Black,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkError,
    background = DarkBackground,
    onBackground = DarkForeground,
    surface = Color(0xFF1A1A1A),
    onSurface = DarkForeground,
    surfaceVariant = DarkSecondary,
    onSurfaceVariant = DarkMutedForeground,
    outline = DarkBorder,
    outlineVariant = DarkMuted,
    scrim = Color.Black.copy(alpha = 0.32f),
    inverseSurface = LightBackground,
    inverseOnSurface = LightForeground,
    inversePrimary = LightPrimary
)

@Composable
fun EngagementAppTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}
