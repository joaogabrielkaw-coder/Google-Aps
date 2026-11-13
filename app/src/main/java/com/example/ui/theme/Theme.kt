package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.example.model.AccessibilitySettings
import com.example.model.ThemeCustomization
import com.example.model.ThemeSurfaceStyle

@Immutable
data class DevRoutineBrandPalette(
    val primary: Color = PurplePrimary,
    val primaryLight: Color = PurplePrimaryLight,
    val accent: Color = RedAccent,
    val accentLight: Color = RedAccentLight,
    val containerDark: Color = PurpleContainerDark,
    val darkBg: Color = PurpleDarkBg
)

val LocalDevRoutineColors = staticCompositionLocalOf { DevRoutineBrandPalette() }
val LocalAccessibilitySettings = staticCompositionLocalOf { AccessibilitySettings() }

private val DefaultDarkColorScheme = darkColorScheme(
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

private val DefaultLightColorScheme = lightColorScheme(
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

private fun contrastingContentColor(background: Color): Color {
    return if (background.luminance() > 0.42f) Color(0xFF140D1C) else Color(0xFFFCFAFF)
}

private fun tuneForDarkSurface(base: Color): Color {
    val lum = base.luminance()
    return if (lum < 0.14f) lerp(base, Color.White, 0.28f) else base
}

private fun tuneForLightSurface(base: Color): Color {
    val lum = base.luminance()
    return if (lum > 0.56f) lerp(base, Color(0xFF141218), 0.34f) else base
}

fun buildColorSchemeFromCustomization(
    customization: ThemeCustomization,
    darkTheme: Boolean
): ColorScheme {
    if (customization.isDefaultPalette) {
        return if (darkTheme) DefaultDarkColorScheme else DefaultLightColorScheme
    }

    val rawPrimary = Color(customization.primaryColorHex)
    val rawSecondary = Color(customization.secondaryColorHex)
    val rawTertiary = Color(customization.tertiaryColorHex)
    val tintAmount = customization.backgroundTintIntensity.coerceIn(0f, 1f)

    return if (darkTheme) {
        val primary = tuneForDarkSurface(rawPrimary)
        val secondary = tuneForDarkSurface(rawSecondary)
        val tertiary = tuneForDarkSurface(rawTertiary)

        val baseBg = when (customization.surfaceStyle) {
            ThemeSurfaceStyle.TINTED -> lerp(Color(0xFF0A090F), rawPrimary, 0.05f + 0.14f * tintAmount)
            ThemeSurfaceStyle.AMOLED -> lerp(Color(0xFF020205), rawPrimary, 0.02f * tintAmount)
            ThemeSurfaceStyle.SLATE -> lerp(Color(0xFF0D1117), rawPrimary, 0.03f * tintAmount)
            ThemeSurfaceStyle.SOFT_WARM -> lerp(Color(0xFF141013), rawPrimary, 0.04f * tintAmount)
        }
        val surface = when (customization.surfaceStyle) {
            ThemeSurfaceStyle.TINTED -> lerp(Color(0xFF12101B), rawPrimary, 0.08f + 0.16f * tintAmount)
            ThemeSurfaceStyle.AMOLED -> lerp(Color(0xFF0B0B10), rawPrimary, 0.04f * tintAmount)
            ThemeSurfaceStyle.SLATE -> lerp(Color(0xFF161B22), rawPrimary, 0.05f * tintAmount)
            ThemeSurfaceStyle.SOFT_WARM -> lerp(Color(0xFF1E181C), rawPrimary, 0.06f * tintAmount)
        }
        val surfaceVariant = when (customization.surfaceStyle) {
            ThemeSurfaceStyle.TINTED -> lerp(surface, rawPrimary, 0.14f + 0.12f * tintAmount)
            ThemeSurfaceStyle.AMOLED -> lerp(surface, rawPrimary, 0.08f)
            ThemeSurfaceStyle.SLATE -> lerp(surface, rawPrimary, 0.09f)
            ThemeSurfaceStyle.SOFT_WARM -> lerp(surface, rawPrimary, 0.10f)
        }
        val primaryContainer = lerp(surfaceVariant, rawPrimary, 0.34f)
        val secondaryContainer = lerp(surfaceVariant, rawSecondary, 0.34f)
        val tertiaryContainer = lerp(surfaceVariant, rawTertiary, 0.32f)
        val outline = lerp(surfaceVariant, primary, 0.42f)
        val outlineVariant = lerp(surfaceVariant, primary, 0.24f)

        darkColorScheme(
            primary = primary,
            onPrimary = contrastingContentColor(primary),
            primaryContainer = primaryContainer,
            onPrimaryContainer = lerp(primary, Color.White, 0.72f),
            secondary = secondary,
            onSecondary = contrastingContentColor(secondary),
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = lerp(secondary, Color.White, 0.72f),
            tertiary = tertiary,
            onTertiary = contrastingContentColor(tertiary),
            tertiaryContainer = tertiaryContainer,
            onTertiaryContainer = lerp(tertiary, Color.White, 0.72f),
            background = baseBg,
            onBackground = Color(0xFFF9F7FD),
            surface = surface,
            onSurface = Color(0xFFF9F7FD),
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = lerp(Color(0xFFC9C4D6), rawPrimary, 0.12f),
            outline = outline,
            outlineVariant = outlineVariant
        )
    } else {
        val primary = tuneForLightSurface(rawPrimary)
        val secondary = tuneForLightSurface(rawSecondary)
        val tertiary = tuneForLightSurface(rawTertiary)

        val baseBg = when (customization.surfaceStyle) {
            ThemeSurfaceStyle.TINTED -> lerp(Color(0xFFFAFAFD), rawPrimary, 0.03f + 0.08f * tintAmount)
            ThemeSurfaceStyle.AMOLED -> Color(0xFFFFFFFF)
            ThemeSurfaceStyle.SLATE -> Color(0xFFF4F6FA)
            ThemeSurfaceStyle.SOFT_WARM -> Color(0xFFFDF8F3)
        }
        val surface = when (customization.surfaceStyle) {
            ThemeSurfaceStyle.AMOLED -> Color(0xFFFFFFFF)
            else -> lerp(Color(0xFFFFFFFF), rawPrimary, 0.02f * tintAmount)
        }
        val surfaceVariant = when (customization.surfaceStyle) {
            ThemeSurfaceStyle.TINTED -> lerp(Color(0xFFF2F0F8), rawPrimary, 0.08f + 0.10f * tintAmount)
            ThemeSurfaceStyle.AMOLED -> lerp(Color(0xFFF5F6F9), rawPrimary, 0.04f)
            ThemeSurfaceStyle.SLATE -> lerp(Color(0xFFEBEFF5), rawPrimary, 0.05f)
            ThemeSurfaceStyle.SOFT_WARM -> lerp(Color(0xFFF5ECE6), rawPrimary, 0.06f)
        }
        val primaryContainer = lerp(Color.White, rawPrimary, 0.18f)
        val secondaryContainer = lerp(Color.White, rawSecondary, 0.18f)
        val tertiaryContainer = lerp(Color.White, rawTertiary, 0.18f)
        val outline = lerp(surfaceVariant, primary, 0.36f)
        val outlineVariant = lerp(surfaceVariant, primary, 0.18f)

        lightColorScheme(
            primary = primary,
            onPrimary = contrastingContentColor(primary),
            primaryContainer = primaryContainer,
            onPrimaryContainer = lerp(primary, Color.Black, 0.55f),
            secondary = secondary,
            onSecondary = contrastingContentColor(secondary),
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = lerp(secondary, Color.Black, 0.55f),
            tertiary = tertiary,
            onTertiary = contrastingContentColor(tertiary),
            tertiaryContainer = tertiaryContainer,
            onTertiaryContainer = lerp(tertiary, Color.Black, 0.55f),
            background = baseBg,
            onBackground = Color(0xFF17131E),
            surface = surface,
            onSurface = Color(0xFF17131E),
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = lerp(Color(0xFF524B5C), rawPrimary, 0.10f),
            outline = outline,
            outlineVariant = outlineVariant
        )
    }
}

fun buildBrandPaletteFromCustomization(
    customization: ThemeCustomization,
    darkTheme: Boolean,
    colorScheme: ColorScheme
): DevRoutineBrandPalette {
    if (customization.isDefaultPalette) {
        return DevRoutineBrandPalette(
            primary = PurplePrimary,
            primaryLight = PurplePrimaryLight,
            accent = RedAccent,
            accentLight = RedAccentLight,
            containerDark = PurpleContainerDark,
            darkBg = PurpleDarkBg
        )
    }
    val rawPrimary = Color(customization.primaryColorHex)
    val rawSecondary = Color(customization.secondaryColorHex)
    return DevRoutineBrandPalette(
        primary = if (darkTheme) tuneForDarkSurface(rawPrimary) else colorScheme.primary,
        primaryLight = lerp(rawPrimary, Color.White, 0.32f),
        accent = if (darkTheme) tuneForDarkSurface(rawSecondary) else colorScheme.secondary,
        accentLight = lerp(rawSecondary, Color.White, 0.30f),
        containerDark = colorScheme.primaryContainer,
        darkBg = colorScheme.background
    )
}

@Composable
fun DevRoutineTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    customization: ThemeCustomization = ThemeCustomization(),
    accessibilitySettings: AccessibilitySettings = AccessibilitySettings(),
    content: @Composable () -> Unit
) {
    val baseColorScheme = remember(customization, darkTheme) {
        buildColorSchemeFromCustomization(customization, darkTheme)
    }
    val colorScheme = remember(baseColorScheme, accessibilitySettings.highContrastBorders) {
        if (!accessibilitySettings.highContrastBorders) {
            baseColorScheme
        } else {
            baseColorScheme.copy(
                outline = lerp(baseColorScheme.outline, baseColorScheme.onSurface, 0.55f),
                outlineVariant = lerp(baseColorScheme.outlineVariant, baseColorScheme.onSurface, 0.35f)
            )
        }
    }
    val brandPalette = remember(customization, darkTheme, colorScheme) {
        buildBrandPaletteFromCustomization(customization, darkTheme, colorScheme)
    }
    val currentDensity = LocalDensity.current
    val scaledDensity = remember(currentDensity, accessibilitySettings.fontScalePreset) {
        Density(
            density = currentDensity.density,
            fontScale = (currentDensity.fontScale * accessibilitySettings.fontScalePreset.scale)
                .coerceIn(0.8f, 1.6f)
        )
    }

    CompositionLocalProvider(
        LocalDevRoutineColors provides brandPalette,
        LocalAccessibilitySettings provides accessibilitySettings,
        LocalDensity provides scaledDensity
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
