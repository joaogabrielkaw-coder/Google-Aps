package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.Language
import com.example.model.ThemeCustomization
import com.example.model.ThemePreference
import com.example.model.ThemePreset
import com.example.model.ThemeSurfaceStyle
import com.example.ui.theme.FullSpectrumPaletteHexes
import com.example.ui.theme.buildColorSchemeFromCustomization
import com.example.ui.theme.colorToHsvArray
import com.example.ui.theme.composeColorToArgbLong
import com.example.ui.theme.hsvToComposeColor
import com.example.ui.theme.suggestHarmoniousAccents

private enum class ColorRoleTab(val labelPt: String, val labelEn: String) {
    PRIMARY("Primária", "Primary"),
    SECONDARY("Secundária", "Secondary"),
    TERTIARY("Terciária", "Tertiary"),
    SURFACE("Fundo & Modo", "Surface & Mode")
}

@Composable
fun PresetPaletteChip(
    preset: ThemePreset,
    isSelected: Boolean,
    isPt: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = Color(preset.primaryHex)
    val secondaryColor = Color(preset.secondaryHex)
    val tertiaryColor = Color(preset.tertiaryHex)
    val isDefault = preset == ThemePreset.PURPLE_CRIMSON

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) primaryColor.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) primaryColor else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("preset_palette_${preset.id}")
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy((-6).dp)) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(primaryColor)
                            .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(secondaryColor)
                            .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(tertiaryColor)
                            .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape)
                    )
                }

                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(16.dp)
                    )
                } else if (isDefault) {
                    Text(
                        text = if (isPt) "Padrão" else "Default",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                }
            }

            Text(
                text = if (isPt) preset.namePt else preset.nameEn,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FullColorPaletteDialog(
    initialCustomization: ThemeCustomization,
    language: Language,
    isLoggedIn: Boolean,
    onApply: (ThemeCustomization) -> Unit,
    onResetDefault: () -> Unit,
    onDismiss: () -> Unit
) {
    val isPt = language == Language.PT
    var draft by remember(initialCustomization) { mutableStateOf(initialCustomization) }
    var selectedRole by remember { mutableStateOf(ColorRoleTab.PRIMARY) }

    val activeRoleHex = when (selectedRole) {
        ColorRoleTab.PRIMARY -> draft.primaryColorHex
        ColorRoleTab.SECONDARY -> draft.secondaryColorHex
        ColorRoleTab.TERTIARY -> draft.tertiaryColorHex
        ColorRoleTab.SURFACE -> draft.primaryColorHex
    }

    val initialHsv = remember(activeRoleHex, selectedRole) {
        colorToHsvArray(Color(activeRoleHex))
    }
    var hue by remember(selectedRole) { mutableFloatStateOf(initialHsv[0]) }
    var saturation by remember(selectedRole) { mutableFloatStateOf(initialHsv[1]) }
    var brightness by remember(selectedRole) { mutableFloatStateOf(initialHsv[2]) }
    var hexInput by remember(selectedRole, activeRoleHex) {
        mutableStateOf(ThemeCustomization.formatHex(activeRoleHex))
    }
    var hexError by remember { mutableStateOf(false) }

    LaunchedEffect(selectedRole) {
        val hsv = colorToHsvArray(Color(activeRoleHex))
        hue = hsv[0]
        saturation = hsv[1]
        brightness = hsv[2]
        hexInput = ThemeCustomization.formatHex(activeRoleHex)
        hexError = false
    }

    LaunchedEffect(activeRoleHex) {
        val currentPickerHex = composeColorToArgbLong(hsvToComposeColor(hue, saturation, brightness))
        if (currentPickerHex != activeRoleHex) {
            val hsv = colorToHsvArray(Color(activeRoleHex))
            hue = hsv[0]
            saturation = hsv[1]
            brightness = hsv[2]
        }
        val formatted = ThemeCustomization.formatHex(activeRoleHex)
        if (hexInput != formatted && !hexError) {
            hexInput = formatted
        }
    }

    fun applyColorHexToSelectedRole(newHex: Long) {
        draft = when (selectedRole) {
            ColorRoleTab.PRIMARY -> draft.copy(
                presetId = ThemeCustomization.CUSTOM_PRESET_ID,
                primaryColorHex = newHex
            )
            ColorRoleTab.SECONDARY -> draft.copy(
                presetId = ThemeCustomization.CUSTOM_PRESET_ID,
                secondaryColorHex = newHex
            )
            ColorRoleTab.TERTIARY -> draft.copy(
                presetId = ThemeCustomization.CUSTOM_PRESET_ID,
                tertiaryColorHex = newHex
            )
            ColorRoleTab.SURFACE -> draft
        }
    }

    val systemDark = isSystemInDarkTheme()
    val previewDark = when (draft.themeMode) {
        ThemePreference.DARK -> true
        ThemePreference.LIGHT -> false
        ThemePreference.SYSTEM -> systemDark
    }
    val previewScheme = remember(draft, previewDark) {
        buildColorSchemeFromCustomization(draft, previewDark)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(
                1.5.dp,
                Brush.linearGradient(listOf(Color(draft.primaryColorHex), Color(draft.secondaryColorHex)))
            ),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 760.dp)
                .testTag("dialog_full_color_palette")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(draft.primaryColorHex), Color(draft.secondaryColorHex))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isPt) "Estúdio Completo de Cores" else "Full Color Studio",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CloudDone,
                                    contentDescription = null,
                                    tint = Color(draft.primaryColorHex),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isLoggedIn) {
                                        if (isPt) "Salvo na sua conta e localmente" else "Saved to account & locally"
                                    } else {
                                        if (isPt) "Salvo localmente (entre para sincronizar)" else "Saved locally (sign in to sync)"
                                    },
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("btn_close_color_studio")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = if (isPt) "Fechar" else "Close"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Live App Theme Preview Card
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = previewScheme.background,
                        border = BorderStroke(1.dp, previewScheme.outline),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("color_studio_live_preview")
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(previewScheme.primary, previewScheme.secondary)
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = previewScheme.onPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = if (isPt) "Prévia ao Vivo" else "Live Preview",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = previewScheme.onBackground
                                        )
                                        Text(
                                            text = if (draft.isDefaultPalette) {
                                                if (isPt) "Padrão • Roxo & Carmesim" else "Default • Purple & Crimson"
                                            } else {
                                                val preset = ThemePreset.fromId(draft.presetId)
                                                if (preset != null) {
                                                    if (isPt) preset.namePt else preset.nameEn
                                                } else {
                                                    if (isPt) "Paleta Personalizada" else "Custom Palette"
                                                }
                                            },
                                            fontSize = 10.sp,
                                            color = previewScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = previewScheme.secondaryContainer
                                ) {
                                    Text(
                                        text = "250 XP",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = previewScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = previewScheme.surfaceVariant
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (isPt) "Meta Diária Dev" else "Daily Dev Goal",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = previewScheme.onSurface
                                        )
                                        Text(
                                            text = "72%",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = previewScheme.tertiary
                                        )
                                    }
                                    LinearProgressIndicator(
                                        progress = { 0.72f },
                                        color = previewScheme.primary,
                                        trackColor = previewScheme.surface,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = previewScheme.primary,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = if (isPt) "Primária" else "Primary",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = previewScheme.onPrimary,
                                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 8.dp)
                                            )
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = previewScheme.secondary,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = if (isPt) "Destaque" else "Accent",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = previewScheme.onSecondary,
                                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 8.dp)
                                            )
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = previewScheme.tertiaryContainer,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = if (isPt) "Terciária" else "Tertiary",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = previewScheme.onTertiaryContainer,
                                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 8.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Role Selector Tabs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ColorRoleTab.entries.forEach { role ->
                            val isTabSelected = selectedRole == role
                            val dotColor = when (role) {
                                ColorRoleTab.PRIMARY -> Color(draft.primaryColorHex)
                                ColorRoleTab.SECONDARY -> Color(draft.secondaryColorHex)
                                ColorRoleTab.TERTIARY -> Color(draft.tertiaryColorHex)
                                ColorRoleTab.SURFACE -> previewScheme.background
                            }
                            FilterChip(
                                selected = isTabSelected,
                                onClick = { selectedRole = role },
                                label = {
                                    Text(
                                        text = if (isPt) role.labelPt else role.labelEn,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                leadingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(dotColor)
                                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(draft.primaryColorHex).copy(alpha = 0.22f)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("role_tab_${role.name.lowercase()}")
                            )
                        }
                    }

                    if (selectedRole != ColorRoleTab.SURFACE) {
                        // Interactive 2D Saturation x Value Color Picker
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isPt) "Seletor de Espectro Completo" else "Full Spectrum Color Picker",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                TextButton(
                                    onClick = {
                                        val (sec, tert) = suggestHarmoniousAccents(draft.primaryColorHex)
                                        draft = draft.copy(
                                            presetId = ThemeCustomization.CUSTOM_PRESET_ID,
                                            secondaryColorHex = sec,
                                            tertiaryColorHex = tert
                                        )
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.testTag("btn_auto_harmony")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isPt) "Harmonia Auto" else "Auto Harmony",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // 2D Saturation & Brightness Box
                            SaturationValueBox(
                                hue = hue,
                                saturation = saturation,
                                value = brightness,
                                onSatValChange = { newSat, newVal ->
                                    saturation = newSat
                                    brightness = newVal
                                    val chosen = hsvToComposeColor(hue, newSat, newVal)
                                    val hexLong = composeColorToArgbLong(chosen)
                                    hexInput = ThemeCustomization.formatHex(hexLong)
                                    hexError = false
                                    applyColorHexToSelectedRole(hexLong)
                                }
                            )

                            // Rainbow Hue Slider
                            RainbowHueBar(
                                hue = hue,
                                onHueChange = { newHue ->
                                    hue = newHue
                                    val chosen = hsvToComposeColor(newHue, saturation, brightness)
                                    val hexLong = composeColorToArgbLong(chosen)
                                    hexInput = ThemeCustomization.formatHex(hexLong)
                                    hexError = false
                                    applyColorHexToSelectedRole(hexLong)
                                }
                            )

                            // Hex Manual Input Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(activeRoleHex))
                                        .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                                )

                                OutlinedTextField(
                                    value = hexInput,
                                    onValueChange = { raw ->
                                        hexInput = raw.uppercase().take(7)
                                        val parsed = ThemeCustomization.parseHexColorOrNull(hexInput)
                                        if (parsed != null) {
                                            hexError = false
                                            val hsv = colorToHsvArray(Color(parsed))
                                            hue = hsv[0]
                                            saturation = hsv[1]
                                            brightness = hsv[2]
                                            applyColorHexToSelectedRole(parsed)
                                        } else {
                                            hexError = true
                                        }
                                    },
                                    label = {
                                        Text(
                                            text = if (isPt) "Código Hex (#RRGGBB)" else "Hex Code (#RRGGBB)",
                                            fontSize = 11.sp
                                        )
                                    },
                                    isError = hexError,
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(activeRoleHex)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("input_hex_color")
                                )
                            }

                            // Complete 72-Color Spectrum Swatch Palette
                            Text(
                                text = if (isPt) "Paleta Completa (72 Cores)" else "Complete Palette (72 Colors)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )

                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                maxItemsInEachRow = 8
                            ) {
                                FullSpectrumPaletteHexes.forEach { swatchHex ->
                                    val isCurrent = (activeRoleHex and 0x00FFFFFFL) == (swatchHex and 0x00FFFFFFL)
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(Color(swatchHex))
                                            .border(
                                                width = if (isCurrent) 2.5.dp else 1.dp,
                                                color = if (isCurrent) MaterialTheme.colorScheme.onSurface else Color.White.copy(alpha = 0.25f),
                                                shape = CircleShape
                                            )
                                            .clickable {
                                                val hsv = colorToHsvArray(Color(swatchHex))
                                                hue = hsv[0]
                                                saturation = hsv[1]
                                                brightness = hsv[2]
                                                hexInput = ThemeCustomization.formatHex(swatchHex)
                                                hexError = false
                                                applyColorHexToSelectedRole(swatchHex)
                                            }
                                            .testTag("swatch_${ThemeCustomization.formatHex(swatchHex)}"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isCurrent) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = if (Color(swatchHex).red * 0.299f + Color(swatchHex).green * 0.587f + Color(swatchHex).blue * 0.114f > 0.55f) Color.Black else Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Surface Style & Theme Mode Tab
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = if (isPt) "Modo de Brilho" else "Brightness Mode",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val modeOptions = listOf(
                                    Triple(ThemePreference.DARK, if (isPt) "Escuro" else "Dark", Icons.Default.DarkMode),
                                    Triple(ThemePreference.LIGHT, if (isPt) "Claro" else "Light", Icons.Default.LightMode),
                                    Triple(ThemePreference.SYSTEM, if (isPt) "Sistema" else "System", Icons.Default.PhoneAndroid)
                                )
                                modeOptions.forEach { (mode, label, icon) ->
                                    val selected = draft.themeMode == mode
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (selected) Color(draft.primaryColorHex).copy(alpha = 0.22f)
                                        else MaterialTheme.colorScheme.surfaceVariant,
                                        border = BorderStroke(
                                            if (selected) 2.dp else 1.dp,
                                            if (selected) Color(draft.primaryColorHex) else MaterialTheme.colorScheme.outlineVariant
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { draft = draft.copy(themeMode = mode) }
                                            .testTag("studio_mode_${mode.code}")
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(vertical = 10.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = null,
                                                tint = if (selected) Color(draft.primaryColorHex) else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = label,
                                                fontSize = 11.sp,
                                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    }
                                }
                            }

                            Text(
                                text = if (isPt) "Estilo de Fundo & Superfícies" else "Background & Surface Style",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                ThemeSurfaceStyle.entries.chunked(2).forEach { rowStyles ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        rowStyles.forEach { style ->
                                            val selected = draft.surfaceStyle == style
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = if (selected) Color(draft.primaryColorHex).copy(alpha = 0.22f)
                                                else MaterialTheme.colorScheme.surfaceVariant,
                                                border = BorderStroke(
                                                    if (selected) 2.dp else 1.dp,
                                                    if (selected) Color(draft.primaryColorHex) else MaterialTheme.colorScheme.outlineVariant
                                                ),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .clickable { draft = draft.copy(surfaceStyle = style) }
                                                    .testTag("surface_style_${style.id}")
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        text = if (isPt) style.labelPt else style.labelEn,
                                                        fontSize = 12.sp,
                                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    if (selected) {
                                                        Icon(
                                                            imageVector = Icons.Default.Check,
                                                            contentDescription = null,
                                                            tint = Color(draft.primaryColorHex),
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = if (isPt) "Intensidade do Tom no Fundo" else "Background Tint Intensity",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "${(draft.backgroundTintIntensity * 100).toInt()}%",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(draft.primaryColorHex)
                                    )
                                }
                                Slider(
                                    value = draft.backgroundTintIntensity,
                                    onValueChange = { intensity ->
                                        draft = draft.copy(
                                            presetId = if (draft.presetId == ThemeCustomization.DEFAULT_PRESET_ID) ThemeCustomization.CUSTOM_PRESET_ID else draft.presetId,
                                            backgroundTintIntensity = intensity.coerceIn(0f, 1f)
                                        )
                                    },
                                    colors = SliderDefaults.colors(
                                        thumbColor = Color(draft.primaryColorHex),
                                        activeTrackColor = Color(draft.primaryColorHex)
                                    )
                                )
                            }
                        }
                    }

                    // Curated Presets Quick Bar inside Studio too
                    Text(
                        text = if (isPt) "Paletas Prontas (12 Temas)" else "Ready Presets (12 Themes)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ThemePreset.entries.chunked(3).forEach { rowPresets ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowPresets.forEach { preset ->
                                    val selected = draft.presetId == preset.id &&
                                        (draft.primaryColorHex and 0x00FFFFFFL) == (preset.primaryHex and 0x00FFFFFFL) &&
                                        (draft.secondaryColorHex and 0x00FFFFFFL) == (preset.secondaryHex and 0x00FFFFFFL)
                                    PresetPaletteChip(
                                        preset = preset,
                                        isSelected = selected,
                                        isPt = isPt,
                                        onClick = {
                                            draft = ThemeCustomization.fromPreset(
                                                preset = preset,
                                                themeMode = draft.themeMode,
                                                surfaceStyle = draft.surfaceStyle,
                                                backgroundTintIntensity = draft.backgroundTintIntensity
                                            )
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Footer Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            draft = ThemeCustomization()
                            onResetDefault()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("btn_studio_reset_default")
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isPt) "Padrão" else "Default", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            onApply(draft)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(draft.primaryColorHex)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_studio_save_apply")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isPt) "Salvar & Aplicar Tema" else "Save & Apply Theme",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SaturationValueBox(
    hue: Float,
    saturation: Float,
    value: Float,
    onSatValChange: (Float, Float) -> Unit
) {
    val pureHueColor = remember(hue) { hsvToComposeColor(hue, 1f, 1f) }
    val currentOnSatValChange by rememberUpdatedState(onSatValChange)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(148.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val s = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                    val v = (1f - offset.y / size.height.toFloat()).coerceIn(0f, 1f)
                    currentOnSatValChange(s, v)
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    change.consume()
                    val s = (change.position.x / size.width.toFloat()).coerceIn(0f, 1f)
                    val v = (1f - change.position.y / size.height.toFloat()).coerceIn(0f, 1f)
                    currentOnSatValChange(s, v)
                }
            }
            .testTag("picker_saturation_value_box")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRoundRect(
                brush = Brush.horizontalGradient(listOf(Color.White, pureHueColor)),
                cornerRadius = CornerRadius(14.dp.toPx())
            )
            drawRoundRect(
                brush = Brush.verticalGradient(listOf(Color.Transparent, Color.Black)),
                cornerRadius = CornerRadius(14.dp.toPx())
            )

            val cx = (saturation.coerceIn(0f, 1f) * size.width)
            val cy = ((1f - value.coerceIn(0f, 1f)) * size.height)
            drawCircle(
                color = Color.Black.copy(alpha = 0.55f),
                radius = 12.dp.toPx(),
                center = Offset(cx, cy),
                style = Stroke(width = 3.dp.toPx())
            )
            drawCircle(
                color = Color.White,
                radius = 10.dp.toPx(),
                center = Offset(cx, cy),
                style = Stroke(width = 2.5.dp.toPx())
            )
        }
    }
}

@Composable
private fun RainbowHueBar(
    hue: Float,
    onHueChange: (Float) -> Unit
) {
    val currentOnHueChange by rememberUpdatedState(onHueChange)
    val rainbowColors = remember {
        listOf(
            Color(0xFFFF0000),
            Color(0xFFFFFF00),
            Color(0xFF00FF00),
            Color(0xFF00FFFF),
            Color(0xFF0000FF),
            Color(0xFFFF00FF),
            Color(0xFFFF0000)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val newHue = ((offset.x / size.width.toFloat()).coerceIn(0f, 1f)) * 360f
                    currentOnHueChange(newHue)
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    change.consume()
                    val newHue = ((change.position.x / size.width.toFloat()).coerceIn(0f, 1f)) * 360f
                    currentOnHueChange(newHue)
                }
            }
            .testTag("picker_hue_bar"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(22.dp)
        ) {
            drawRoundRect(
                brush = Brush.horizontalGradient(rainbowColors),
                cornerRadius = CornerRadius(11.dp.toPx())
            )
            val xPos = (hue.coerceIn(0f, 360f) / 360f) * size.width
            val yPos = size.height / 2f
            drawCircle(
                color = hsvToComposeColor(hue, 1f, 1f),
                radius = 13.dp.toPx(),
                center = Offset(xPos, yPos)
            )
            drawCircle(
                color = Color.White,
                radius = 13.dp.toPx(),
                center = Offset(xPos, yPos),
                style = Stroke(width = 2.5.dp.toPx())
            )
        }
    }
}
