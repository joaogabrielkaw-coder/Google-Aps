package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PurplePrimaryLight,
    onPrimary = Color(0xFF230047),
    primaryContainer = PurpleContainerDark,
    onPrimaryContainer = Color(0xFFF1D8FF),

    secondary = RedAccentLight,
    onSecondary = Color(0xFF4A0012),
    secondaryContainer = RedContainerDark,
    onSecondaryContainer = Color(0xFFFFD9E2),

    tertiary = RedAccent,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF8B1229),
    onTertiaryContainer = Color(0xFFFFDADE),

    background = PurpleDarkBg,
    onBackground = TextWhite,

    surface = PurpleDarkSurface,
    onSurface = TextWhite,
    surfaceVariant = PurpleDarkSurfaceVariant,
    onSurfaceVariant = TextMutedDark,

    outline = PurpleDarkCardBorder,
    outlineVariant = Color(0xFF381B5E)
)

private val LightColorScheme = lightColorScheme(
    primary = PurpleLightPrimary,
    onPrimary = Color.White,
    primaryContainer = PurpleLightPrimaryContainer,
    onPrimaryContainer = Color(0xFF2C004F),

    secondary = RedSecondary,
    onSecondary = Color.White,
    secondaryContainer = RedLightSecondaryContainer,
    onSecondaryContainer = Color(0xFF4A0012),

    tertiary = RedLightSecondary,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFD8E1),
    onTertiaryContainer = Color(0xFF3D0010),

    background = PurpleLightBg,
    onBackground = TextDark,

    surface = PurpleLightSurface,
    onSurface = TextDark,
    surfaceVariant = PurpleLightSurfaceVariant,
    onSurfaceVariant = TextMutedLight,

    outline = Color(0xFFD4C2EB),
    outlineVariant = Color(0xFFE8DEF2)
)

@Composable
fun DevRoutineTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
