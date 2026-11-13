package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Purple Palette
val PurpleDarkBg = Color(0xFF0F061C)
val PurpleDarkSurface = Color(0xFF190C2F)
val PurpleDarkSurfaceVariant = Color(0xFF261245)
val PurpleDarkCardBorder = Color(0xFF452474)

val PurplePrimary = Color(0xFF9D4EDD)
val PurplePrimaryLight = Color(0xFFC77DFF)
val PurpleContainerDark = Color(0xFF491475)

val PurpleLightBg = Color(0xFFFAF7FF)
val PurpleLightSurface = Color(0xFFFFFFFF)
val PurpleLightSurfaceVariant = Color(0xFFF3EAFC)
val PurpleLightPrimary = Color(0xFF6A0DAD)
val PurpleLightPrimaryContainer = Color(0xFFECD7FF)

// Red Palette (Crimson & Ruby)
val RedAccent = Color(0xFFFF2A55)
val RedAccentLight = Color(0xFFFF4D6D)
val RedSecondary = Color(0xFFD90429)
val RedContainerDark = Color(0xFF6B081A)
val RedLightSecondary = Color(0xFFC9184A)
val RedLightSecondaryContainer = Color(0xFFFFD9E2)

// Text and neutral accents
val TextWhite = Color(0xFFFBF8FF)
val TextMutedDark = Color(0xFFC5B8D8)
val TextDark = Color(0xFF1B1028)
val TextMutedLight = Color(0xFF5A4D6B)

// Tech badge colors
val TechPythonColor = Color(0xFF4D96FF)
val TechSqlColor = Color(0xFFC77DFF)
val TechHtmlColor = Color(0xFFFF5436)
val TechCssColor = Color(0xFF26A69A)
val TechJavaColor = Color(0xFFFF7043)
val TechMySqlColor = Color(0xFF00B4D8)
val TechPostgresColor = Color(0xFF9C27B0)
val TechMongoColor = Color(0xFF00E676)
val TechJsColor = Color(0xFFFFD600)
val TechTsColor = Color(0xFF3178C6)
val TechKotlinColor = Color(0xFF7F52FF)
val TechGoColor = Color(0xFF00ADD8)

/**
 * Paleta completa do espectro (72 cores em 8 famílias de matiz × 9 variações de luminosidade/saturação).
 */
val FullSpectrumPaletteHexes: List<Long> = listOf(
    // Vermelho & Rubi
    0xFFFFCDD2L, 0xFFFF8A80L, 0xFFFF5252L, 0xFFFF2A55L, 0xFFFF1744L, 0xFFE53935L, 0xFFD50000L, 0xFFB71C1CL,
    // Laranja & Coral
    0xFFFFE0B2L, 0xFFFFAB91L, 0xFFFF7043L, 0xFFFF6D00L, 0xFFFF7B00L, 0xFFF4511EL, 0xFFE65100L, 0xFFBF360CL,
    // Âmbar & Ouro
    0xFFFFF8E1L, 0xFFFFE082L, 0xFFFFD54FL, 0xFFFFCA28L, 0xFFFFB300L, 0xFFFFAB00L, 0xFFFF8F00L, 0xFFE65100L,
    // Lima & Esmeralda
    0xFFDCEDC8L, 0xFFB2FF59L, 0xFF76FF03L, 0xFF64DD17L, 0xFF00E676L, 0xFF00C853L, 0xFF2E7D32L, 0xFF1B5E20L,
    // Menta & Ciano
    0xFFB2DFDBL, 0xFFA7FFEBL, 0xFF1DE9B6L, 0xFF00BFA5L, 0xFF00E5FFL, 0xFF00B4D8L, 0xFF0097A7L, 0xFF006064L,
    // Azul Celeste & Cobalto
    0xFFBBDEFBL, 0xFF80D8FFL, 0xFF40C4FFL, 0xFF00B0FFL, 0xFF3A86FFL, 0xFF2979FFL, 0xFF1E88E5L, 0xFF0D47A1L,
    // Índigo & Violeta
    0xFFC5CAE9L, 0xFF8C9EFFL, 0xFF536DFEL, 0xFF7C4DFFL, 0xFF9D4EDDL, 0xFF7F52FFL, 0xFF6A0DADL, 0xFF4A148CL,
    // Magenta, Rosa & Neutros
    0xFFF8BBD0L, 0xFFFF80ABL, 0xFFFF4081L, 0xFFE91E63L, 0xFFE040FBL, 0xFFC77DFFL, 0xFFAD1457L, 0xFF880E4FL,
    // Neutros / Grafite / Platina
    0xFFECEFF1L, 0xFFCFD8DCL, 0xFFB0BEC5L, 0xFF90A4AEL, 0xFF78909CL, 0xFF607D8BL, 0xFF455A64L, 0xFF263238L
)

fun colorToHsvArray(color: Color): FloatArray {
    val argb = android.graphics.Color.argb(
        255,
        (color.red * 255f).toInt().coerceIn(0, 255),
        (color.green * 255f).toInt().coerceIn(0, 255),
        (color.blue * 255f).toInt().coerceIn(0, 255)
    )
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(argb, hsv)
    return hsv
}

fun hsvToComposeColor(hue: Float, saturation: Float, value: Float): Color {
    val clampedH = ((hue % 360f) + 360f) % 360f
    val clampedS = saturation.coerceIn(0f, 1f)
    val clampedV = value.coerceIn(0f, 1f)
    val argb = android.graphics.Color.HSVToColor(floatArrayOf(clampedH, clampedS, clampedV))
    return Color(argb)
}

fun composeColorToArgbLong(color: Color): Long {
    val r = (color.red * 255f).toInt().coerceIn(0, 255)
    val g = (color.green * 255f).toInt().coerceIn(0, 255)
    val b = (color.blue * 255f).toInt().coerceIn(0, 255)
    return 0xFF000000L or (r.toLong() shl 16) or (g.toLong() shl 8) or b.toLong()
}

/**
 * Sugere cores secundária e terciária harmônicas a partir de uma cor primária escolhida.
 */
fun suggestHarmoniousAccents(primaryHex: Long): Pair<Long, Long> {
    val hsv = colorToHsvArray(Color(primaryHex))
    val baseHue = hsv[0]
    val sat = hsv[1].coerceAtLeast(0.65f)
    val value = hsv[2].coerceAtLeast(0.85f)
    val secondary = hsvToComposeColor((baseHue + 55f) % 360f, (sat + 0.1f).coerceAtMost(1f), value)
    val tertiary = hsvToComposeColor((baseHue + 325f) % 360f, (sat * 0.85f).coerceAtLeast(0.45f), (value + 0.1f).coerceAtMost(1f))
    return composeColorToArgbLong(secondary) to composeColorToArgbLong(tertiary)
}

