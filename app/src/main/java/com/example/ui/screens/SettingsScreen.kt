package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AccessibilitySettings
import com.example.model.AppGeneralSettings
import com.example.model.FontScalePreset
import com.example.model.Language
import com.example.model.ThemeCustomization
import com.example.model.ThemePreference
import com.example.model.ThemePreset
import com.example.model.UserAccount
import com.example.model.UserFeedbackNote
import com.example.ui.components.FullColorPaletteDialog
import com.example.ui.components.PresetPaletteChip
import com.example.ui.components.UserAvatar
import com.example.ui.theme.LocalDevRoutineColors

enum class SettingsCategory(
    val id: String,
    val labelPt: String,
    val labelEn: String,
    val icon: ImageVector
) {
    ALL("all", "Tudo", "All", Icons.Default.Tune),
    ACCESSIBILITY("accessibility", "Acessibilidade", "Accessibility", Icons.Default.AccessibilityNew),
    GENERAL("general", "Geral", "General", Icons.Default.Settings),
    THEME("theme", "Tema", "Theme", Icons.Default.Palette),
    FEEDBACKS("feedbacks", "Feedbacks", "Feedbacks", Icons.Default.Feedback)
}

@Composable
fun SettingsScreen(
    currentLanguage: Language,
    currentTheme: ThemePreference,
    themeCustomization: ThemeCustomization = ThemeCustomization(),
    accessibilitySettings: AccessibilitySettings = AccessibilitySettings(),
    appGeneralSettings: AppGeneralSettings = AppGeneralSettings(),
    userFeedbackNotes: List<UserFeedbackNote> = emptyList(),
    userAccount: UserAccount? = null,
    onBack: () -> Unit = {},
    onSetLanguage: (Language) -> Unit,
    onSetTheme: (ThemePreference) -> Unit,
    onSelectThemePreset: (ThemePreset) -> Unit = {},
    onUpdateThemeCustomization: (ThemeCustomization) -> Unit = {},
    onResetThemeDefault: () -> Unit = {},
    onUpdateAccessibility: (AccessibilitySettings) -> Unit = {},
    onResetAccessibility: () -> Unit = {},
    onUpdateAppGeneralSettings: (AppGeneralSettings) -> Unit = {},
    onSubmitFeedback: (category: String, rating: Int, message: String) -> Unit = { _, _, _ -> },
    onDeleteFeedback: (String) -> Unit = {},
    onOpenWeeklyAiSummary: () -> Unit = {},
    onOpenPhotoPicker: () -> Unit = {},
    onRedoAssessment: () -> Unit = {},
    onResetStarterData: () -> Unit = {},
    onSendTestNotification: () -> Unit = {}
) {
    val isPt = currentLanguage == Language.PT
    val brand = LocalDevRoutineColors.current
    val primaryColor = MaterialTheme.colorScheme.primary
    val accentColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val haptic = LocalHapticFeedback.current

    var selectedCategory by remember { mutableStateOf(SettingsCategory.ALL) }
    var showFullColorStudio by remember { mutableStateOf(false) }

    val feedbackCategories = remember(isPt) {
        if (isPt) listOf("Sugestão", "Elogio", "Acessibilidade", "Problema")
        else listOf("Suggestion", "Praise", "Accessibility", "Bug")
    }
    var selectedFeedbackCategory by remember(isPt) { mutableStateOf(feedbackCategories.first()) }
    var feedbackRating by remember { mutableIntStateOf(5) }
    var feedbackMessage by remember { mutableStateOf("") }
    var feedbackSubmittedBanner by remember { mutableStateOf(false) }

    val showAccessibility = selectedCategory == SettingsCategory.ALL || selectedCategory == SettingsCategory.ACCESSIBILITY
    val showGeneral = selectedCategory == SettingsCategory.ALL || selectedCategory == SettingsCategory.GENERAL
    val showTheme = selectedCategory == SettingsCategory.ALL || selectedCategory == SettingsCategory.THEME
    val showFeedbacks = selectedCategory == SettingsCategory.ALL || selectedCategory == SettingsCategory.FEEDBACKS

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("screen_settings"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Intuitive Top Header with Back Button & Gear Identity
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = BorderStroke(
                    1.2.dp,
                    Brush.horizontalGradient(listOf(brand.primary.copy(alpha = 0.65f), brand.accent.copy(alpha = 0.65f)))
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("btn_settings_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = if (isPt) "Voltar" else "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(brand.primary, brand.accent))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isPt) "Central de Configurações" else "Settings Hub",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
                        )
                        Text(
                            text = if (isPt) "Acessibilidade, opções gerais, tema e feedbacks em um só lugar"
                            else "Accessibility, general preferences, theme and feedbacks in one place",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 2. Quick Category Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(SettingsCategory.entries) { category ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = category },
                        label = {
                            Text(
                                text = if (isPt) category.labelPt else category.labelEn,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = category.icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = primaryColor,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.testTag("chip_settings_category_${category.id}")
                    )
                }
            }
        }

        // ============================================================
        // SECTION A: ACESSIBILIDADE (Accessibility)
        // ============================================================
        if (showAccessibility) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = BorderStroke(
                        1.dp,
                        Brush.linearGradient(listOf(primaryColor.copy(alpha = 0.55f), Color(0xFF00B4D8).copy(alpha = 0.55f)))
                    ),
                    modifier = Modifier.testTag("section_settings_accessibility")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        SettingsSectionHeader(
                            icon = Icons.Default.AccessibilityNew,
                            iconTint = Color(0xFF00B4D8),
                            title = if (isPt) "Acessibilidade" else "Accessibility",
                            subtitle = if (isPt) "Ajuste leitura, contraste e conforto visual ao seu estilo"
                            else "Tune text size, contrast and visual comfort for your needs"
                        )

                        // Text Size Presets
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.FormatSize,
                                    contentDescription = null,
                                    tint = primaryColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isPt) "Tamanho do Texto" else "Text Size",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                FontScalePreset.entries.forEach { preset ->
                                    val isChosen = accessibilitySettings.fontScalePreset == preset
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isChosen) primaryColor.copy(alpha = 0.22f)
                                        else MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(
                                            width = if (isChosen) 1.5.dp else 1.dp,
                                            color = if (isChosen) primaryColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .minimumInteractiveComponentSize()
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable {
                                                onUpdateAccessibility(accessibilitySettings.copy(fontScalePreset = preset))
                                            }
                                            .testTag("btn_font_scale_${preset.name.lowercase()}")
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = if (isPt) preset.labelPt else preset.labelEn,
                                                fontSize = 11.sp,
                                                fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isChosen) primaryColor else MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "${(preset.scale * 100).toInt()}%",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }

                            // Live preview box
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (isPt) "Prévia: Aprender código todos os dias fica ainda mais confortável!"
                                    else "Preview: Learning code every day feels even more comfortable!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        // High Contrast Borders Toggle
                        SettingsToggleRow(
                            icon = Icons.Default.Contrast,
                            iconTint = primaryColor,
                            title = if (isPt) "Alto Contraste nas Bordas" else "High Contrast Borders",
                            description = if (isPt) "Reforça contornos de cartões e botões para melhor leitura"
                            else "Strengthens card and button outlines for sharper readability",
                            checked = accessibilitySettings.highContrastBorders,
                            onCheckedChange = { checked ->
                                onUpdateAccessibility(accessibilitySettings.copy(highContrastBorders = checked))
                            },
                            testTag = "switch_high_contrast"
                        )

                        // Reduce Motion Toggle
                        SettingsToggleRow(
                            icon = Icons.Default.Animation,
                            iconTint = accentColor,
                            title = if (isPt) "Reduzir Animações" else "Reduce Motion",
                            description = if (isPt) "Suaviza brilhos contínuos e animações de pulsação"
                            else "Minimizes continuous sparkles and pulsing motion effects",
                            checked = accessibilitySettings.reduceMotion,
                            onCheckedChange = { checked ->
                                onUpdateAccessibility(accessibilitySettings.copy(reduceMotion = checked))
                            },
                            testTag = "switch_reduce_motion"
                        )

                        // Haptic Feedback Toggle
                        SettingsToggleRow(
                            icon = Icons.Default.Vibration,
                            iconTint = Color(0xFF4CAF50),
                            title = if (isPt) "Resposta Tátil (Vibração)" else "Haptic Feedback",
                            description = if (isPt) "Vibração leve e amigável ao interagir com botões e quizzes"
                            else "Gentle tactile vibration when tapping buttons and quizzes",
                            checked = accessibilitySettings.hapticFeedbackEnabled,
                            onCheckedChange = { checked ->
                                onUpdateAccessibility(accessibilitySettings.copy(hapticFeedbackEnabled = checked))
                                if (checked) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                            },
                            testTag = "switch_haptic_feedback"
                        )

                        if (accessibilitySettings != AccessibilitySettings()) {
                            OutlinedButton(
                                onClick = onResetAccessibility,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_reset_accessibility")
                            ) {
                                Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isPt) "Restaurar Acessibilidade Padrão" else "Reset Accessibility Defaults",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // ============================================================
        // SECTION B: OPÇÕES DE TEMA (Theme & Color Palette)
        // ============================================================
        if (showTheme) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = BorderStroke(
                        1.2.dp,
                        Brush.linearGradient(listOf(primaryColor.copy(alpha = 0.6f), accentColor.copy(alpha = 0.6f)))
                    ),
                    modifier = Modifier.testTag("section_settings_theme")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = null,
                                    tint = accentColor
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = if (isPt) "Opções de Tema & Cores" else "Theme & Color Options",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CloudDone,
                                            contentDescription = null,
                                            tint = primaryColor,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (userAccount?.isLoggedIn == true) {
                                                if (isPt) "Salvo na conta e neste dispositivo" else "Saved to your account & locally"
                                            } else {
                                                if (isPt) "Salvo localmente neste dispositivo" else "Saved locally on this device"
                                            },
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            // Active 3-color preview dots
                            Row(horizontalArrangement = Arrangement.spacedBy((-6).dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(Color(themeCustomization.primaryColorHex))
                                        .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(Color(themeCustomization.secondaryColorHex))
                                        .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(Color(themeCustomization.tertiaryColorHex))
                                        .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape)
                                )
                            }
                        }

                        // Brightness Mode Row (Dark / Light / System)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ThemeOptionCard(
                                title = if (isPt) "Escuro" else "Dark",
                                icon = Icons.Default.DarkMode,
                                isSelected = currentTheme == ThemePreference.DARK,
                                onClick = { onSetTheme(ThemePreference.DARK) },
                                modifier = Modifier.weight(1f)
                            )

                            ThemeOptionCard(
                                title = if (isPt) "Claro" else "Light",
                                icon = Icons.Default.LightMode,
                                isSelected = currentTheme == ThemePreference.LIGHT,
                                onClick = { onSetTheme(ThemePreference.LIGHT) },
                                modifier = Modifier.weight(1f)
                            )

                            ThemeOptionCard(
                                title = if (isPt) "Sistema" else "System",
                                icon = Icons.Default.PhoneAndroid,
                                isSelected = currentTheme == ThemePreference.SYSTEM,
                                onClick = { onSetTheme(ThemePreference.SYSTEM) },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Active palette info pill
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isPt) "Paleta Atual" else "Active Palette",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    val activePreset = ThemePreset.fromId(themeCustomization.presetId)
                                    val label = when {
                                        themeCustomization.isDefaultPalette -> if (isPt) "Roxo & Carmesim (Padrão Principal)" else "Purple & Crimson (Primary Default)"
                                        activePreset != null -> if (isPt) activePreset.namePt else activePreset.nameEn
                                        else -> if (isPt) "Customizada (${ThemeCustomization.formatHex(themeCustomization.primaryColorHex)})" else "Custom (${ThemeCustomization.formatHex(themeCustomization.primaryColorHex)})"
                                    }
                                    Text(
                                        text = label,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = primaryColor
                                    )
                                }
                                Text(
                                    text = if (isPt) themeCustomization.surfaceStyle.labelPt else themeCustomization.surfaceStyle.labelEn,
                                    fontSize = 11.sp,
                                    color = tertiaryColor,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Quick 12 Preset Palettes Grid
                        Text(
                            text = if (isPt) "Temas Prontos (Escolha Rápida)" else "Preset Palettes (Quick Pick)",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            ThemePreset.entries.chunked(3).forEach { rowPresets ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowPresets.forEach { preset ->
                                        val isPresetSelected = themeCustomization.presetId == preset.id &&
                                            (themeCustomization.primaryColorHex and 0x00FFFFFFL) == (preset.primaryHex and 0x00FFFFFFL) &&
                                            (themeCustomization.secondaryColorHex and 0x00FFFFFFL) == (preset.secondaryHex and 0x00FFFFFFL)
                                        PresetPaletteChip(
                                            preset = preset,
                                            isSelected = isPresetSelected,
                                            isPt = isPt,
                                            onClick = { onSelectThemePreset(preset) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }

                        // Full Spectrum Customizer Launcher + Reset
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { showFullColorStudio = true },
                                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_open_full_color_studio")
                            ) {
                                Icon(Icons.Default.ColorLens, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isPt) "Paleta Completa & Customizar" else "Full Palette & Customize",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }

                            if (!themeCustomization.isDefaultPalette || currentTheme != ThemePreference.DARK) {
                                OutlinedButton(
                                    onClick = onResetThemeDefault,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("btn_reset_default_theme")
                                ) {
                                    Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (isPt) "Padrão" else "Reset", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // ============================================================
        // SECTION C: CONFIGURAÇÃO GERAL DO APLICATIVO (General)
        // ============================================================
        if (showGeneral) {
            // Profile & Diagnostic Quick Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = CardDefaults.outlinedCardBorder().copy(width = 1.dp),
                    modifier = Modifier.testTag("section_settings_general_profile")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SettingsSectionHeader(
                            icon = Icons.Default.Settings,
                            iconTint = primaryColor,
                            title = if (isPt) "Configuração Geral do Aplicativo" else "General App Settings",
                            subtitle = if (isPt) "Perfil, idioma, notificações e comportamento do cronômetro"
                            else "Profile, language, notifications and study timer behavior"
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                UserAvatar(
                                    photoUrl = userAccount?.photoUrl,
                                    displayName = userAccount?.displayName ?: "Dev",
                                    size = 50.dp,
                                    borderWidth = 2.dp,
                                    borderColor = accentColor,
                                    showEditBadge = true,
                                    onClick = onOpenPhotoPicker
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = userAccount?.displayName ?: (if (isPt) "Desenvolvedor" else "Developer"),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = if (isPt) userAccount?.levelTitlePt ?: "Aspirante a Dev" else userAccount?.levelTitleEn ?: "Aspiring Dev",
                                        fontSize = 12.sp,
                                        color = accentColor,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Button(
                                onClick = onOpenPhotoPicker,
                                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = if (isPt) "Foto" else "Photo", fontSize = 12.sp)
                            }
                        }

                        Button(
                            onClick = onRedoAssessment,
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor.copy(alpha = 0.9f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_redo_diagnostic")
                        ) {
                            Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isPt) "Diagnóstico Inicial Dev (Opcional • 5 Desafios)" else "Initial Dev Diagnostic (Optional • 5 Challenges)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        // Language Switcher inside General Settings
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isPt) "Idioma do Aplicativo" else "App Language",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            LanguageOptionCard(
                                language = Language.PT,
                                isSelected = currentLanguage == Language.PT,
                                onClick = { onSetLanguage(Language.PT) },
                                modifier = Modifier.weight(1f)
                            )

                            LanguageOptionCard(
                                language = Language.EN,
                                isSelected = currentLanguage == Language.EN,
                                onClick = { onSetLanguage(Language.EN) },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Screen-Time Inactivity Auto-Pause Selector
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isPt) "Pausa Automática do Cronômetro por Inatividade"
                                    else "Study Timer Inactivity Auto-Pause",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            Text(
                                text = if (isPt) "Pausa o tempo de estudo automaticamente se não houver interação por:"
                                else "Automatically pauses active study tracking when idle for:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(1, 2, 5).forEach { mins ->
                                    val selected = appGeneralSettings.idlePauseMinutes == mins
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (selected) accentColor.copy(alpha = 0.2f)
                                        else MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(
                                            width = if (selected) 1.5.dp else 1.dp,
                                            color = if (selected) accentColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .minimumInteractiveComponentSize()
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable {
                                                onUpdateAppGeneralSettings(appGeneralSettings.copy(idlePauseMinutes = mins))
                                            }
                                            .testTag("btn_idle_timeout_${mins}m")
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(vertical = 10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = if (mins == 2) {
                                                    if (isPt) "$mins min (Padrão)" else "$mins min (Default)"
                                                } else "$mins min",
                                                fontSize = 11.sp,
                                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (selected) accentColor else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Notifications & Daily Reminders Toggle + Test
                        SettingsToggleRow(
                            icon = Icons.Default.Notifications,
                            iconTint = accentColor,
                            title = if (isPt) "Lembretes Diários (WorkManager)" else "Daily Study Reminders (WorkManager)",
                            description = if (isPt) "Notificações locais 15 min antes das rotinas agendadas no Calendário"
                            else "Local push notifications 15 min before scheduled Calendar sessions",
                            checked = appGeneralSettings.dailyRemindersEnabled,
                            onCheckedChange = { enabled ->
                                onUpdateAppGeneralSettings(appGeneralSettings.copy(dailyRemindersEnabled = enabled))
                            },
                            testTag = "switch_daily_reminders"
                        )

                        Button(
                            onClick = onSendTestNotification,
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_test_notification")
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isPt) "Disparar Notificação de Teste" else "Send Test Notification",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // About Covered Techs Card inside General
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isPt) "Tecnologias & Recursos Inclusos" else "Covered Technologies & Features",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (isPt) "• Linguagens & Web: Python, SQL, HTML, CSS, Java, JavaScript, TypeScript, Kotlin, Go\n• Bancos de Dados: MySQL, PostgreSQL, MongoDB & SQLite Studio\n• Inteligência Artificial: DevTutor AI, Resumo Semanal & Transcrição de Voz\n• Notificações Locais: WorkManager (Jetpack)"
                            else "• Languages & Web: Python, SQL, HTML, CSS, Java, JavaScript, TypeScript, Kotlin, Go\n• Databases: MySQL, PostgreSQL, MongoDB & SQLite Studio\n• Artificial Intelligence: DevTutor AI, Weekly Summary & Voice Transcription\n• Local Notifications: WorkManager (Jetpack)",
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 20.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // ============================================================
        // SECTION D: SOBRE OS FEEDBACKS (About Feedbacks & Experience)
        // ============================================================
        if (showFeedbacks) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = BorderStroke(
                        1.dp,
                        Brush.linearGradient(listOf(accentColor.copy(alpha = 0.6f), Color(0xFFFFD54F).copy(alpha = 0.6f)))
                    ),
                    modifier = Modifier.testTag("section_settings_feedbacks")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        SettingsSectionHeader(
                            icon = Icons.Default.Feedback,
                            iconTint = Color(0xFFFFD54F),
                            title = if (isPt) "Sobre os Feedbacks" else "About Feedbacks",
                            subtitle = if (isPt) "Entenda e personalize como o app celebra conquistas, acolhe erros e gera análises"
                            else "Understand and customize celebrations, gentle retry hints and AI feedback"
                        )

                        // Explainer Box 1: How feedback works in DevRoutine
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Favorite, contentDescription = null, tint = Color(0xFFFF4D7D), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isPt) "Como funcionam os feedbacks no DevRoutine?" else "How do feedbacks work in DevRoutine?",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                                Text(
                                    text = if (isPt) {
                                        "• Vitórias: Cada desafio ou quiz concluído exibe uma mensagem comemorativa com XP e preservação das suas 25 vidas diárias.\n" +
                                        "• Erros com carinho: Ao errar, você recebe uma dica gentil para tentar novamente sem desânimo.\n" +
                                        "• Resumo Semanal com IA: Analisa seus últimos 7 dias e cria um plano de estudos personalizado."
                                    } else {
                                        "• Victories: Completing challenges or quizzes triggers celebratory XP feedback alongside your 25 daily lives.\n" +
                                        "• Gentle retries: Mistakes show encouraging hints so you can try again smoothly.\n" +
                                        "• Weekly AI Feedback: Reviews your 7-day history with personalized next-week recommendations."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 19.sp
                                )
                            }
                        }

                        // Feedback Customization Toggles
                        SettingsToggleRow(
                            icon = Icons.Default.AutoAwesome,
                            iconTint = Color(0xFFFFD54F),
                            title = if (isPt) "Mensagens Comemorativas de Vitória" else "Victory Celebration Dialogs",
                            description = if (isPt) "Exibe painel festivo com estrelas ao vencer desafios e quizzes"
                            else "Shows decorated celebration dialog when solving challenges or quizzes",
                            checked = appGeneralSettings.showCelebrationDialogs,
                            onCheckedChange = { checked ->
                                onUpdateAppGeneralSettings(appGeneralSettings.copy(showCelebrationDialogs = checked))
                            },
                            testTag = "switch_celebration_dialogs"
                        )

                        SettingsToggleRow(
                            icon = Icons.Default.Favorite,
                            iconTint = Color(0xFFFF4D7D),
                            title = if (isPt) "Incentivo Gentil ao Errar" else "Gentle Retry Encouragement",
                            description = if (isPt) "Mostra dicas acolhedoras de \"tente novamente\" a cada erro"
                            else "Displays supportive try-again hints whenever an answer misses",
                            checked = appGeneralSettings.showGentleRetryTips,
                            onCheckedChange = { checked ->
                                onUpdateAppGeneralSettings(appGeneralSettings.copy(showGentleRetryTips = checked))
                            },
                            testTag = "switch_gentle_retry_tips"
                        )

                        SettingsToggleRow(
                            icon = Icons.Default.Star,
                            iconTint = primaryColor,
                            title = if (isPt) "Banners Rápidos de XP e Selos" else "XP & Badge Toast Banners",
                            description = if (isPt) "Mostra avisos flutuantes quando você ganha XP ou marcos de foco"
                            else "Shows floating banners whenever XP or focus milestones are earned",
                            checked = appGeneralSettings.showXpToastBanners,
                            onCheckedChange = { checked ->
                                onUpdateAppGeneralSettings(appGeneralSettings.copy(showXpToastBanners = checked))
                            },
                            testTag = "switch_xp_banners"
                        )

                        // Quick launch to AI Weekly Study Summary Feedback
                        OutlinedButton(
                            onClick = onOpenWeeklyAiSummary,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.6f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_settings_open_weekly_ai_feedback")
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = primaryColor, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isPt) "✨ Ver Meu Feedback Semanal com IA" else "✨ Open My AI Weekly Feedback",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = primaryColor
                            )
                        }

                        // Send User Feedback about the App
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = if (isPt) "Enviar Sugestão ou Feedback do App" else "Send App Feedback or Suggestion",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )

                                // Category chips
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(feedbackCategories) { cat ->
                                        val selected = selectedFeedbackCategory == cat
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (selected) accentColor.copy(alpha = 0.2f)
                                            else MaterialTheme.colorScheme.surfaceVariant,
                                            border = BorderStroke(
                                                1.dp,
                                                if (selected) accentColor else Color.Transparent
                                            ),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .clickable { selectedFeedbackCategory = cat }
                                        ) {
                                            Text(
                                                text = cat,
                                                fontSize = 11.sp,
                                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (selected) accentColor else MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }

                                // 1-5 Star Rating
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = if (isPt) "Avaliação:" else "Rating:",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    (1..5).forEach { star ->
                                        IconButton(
                                            onClick = { feedbackRating = star },
                                            modifier = Modifier.size(48.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (star <= feedbackRating) Icons.Default.Star else Icons.Default.StarBorder,
                                                contentDescription = "$star",
                                                tint = Color(0xFFFFD54F),
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = feedbackMessage,
                                    onValueChange = {
                                        feedbackMessage = it
                                        feedbackSubmittedBanner = false
                                    },
                                    label = {
                                        Text(
                                            text = if (isPt) "Conte sua experiência ou sugestão..." else "Share your experience or suggestion...",
                                            fontSize = 12.sp
                                        )
                                    },
                                    maxLines = 3,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_app_feedback")
                                )

                                Button(
                                    onClick = {
                                        if (feedbackMessage.isNotBlank()) {
                                            onSubmitFeedback(selectedFeedbackCategory, feedbackRating, feedbackMessage)
                                            feedbackMessage = ""
                                            feedbackSubmittedBanner = true
                                        }
                                    },
                                    enabled = feedbackMessage.isNotBlank(),
                                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("btn_submit_app_feedback")
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isPt) "Registrar Feedback" else "Submit Feedback",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }

                                if (feedbackSubmittedBanner) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFF2E7D32).copy(alpha = 0.2f),
                                        border = BorderStroke(1.dp, Color(0xFF4CAF50).copy(alpha = 0.5f))
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = if (isPt) "Feedback salvo com carinho! Obrigado pela contribuição 💜"
                                                else "Feedback saved! Thank you for helping shape DevRoutine 💜",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }

                                if (userFeedbackNotes.isNotEmpty()) {
                                    Text(
                                        text = if (isPt) "Seus feedbacks recentes (${userFeedbackNotes.size})"
                                        else "Your recent feedbacks (${userFeedbackNotes.size})",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    userFeedbackNotes.take(3).forEach { note ->
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = "${note.category} • ${"★".repeat(note.rating)} • ${note.createdAtIso}",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = primaryColor
                                                    )
                                                    Text(
                                                        text = note.message,
                                                        fontSize = 12.sp,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                                IconButton(
                                                    onClick = { onDeleteFeedback(note.id) },
                                                    modifier = Modifier.size(48.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.DeleteOutline,
                                                        contentDescription = if (isPt) "Remover" else "Remove",
                                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showFullColorStudio) {
        FullColorPaletteDialog(
            initialCustomization = themeCustomization,
            language = currentLanguage,
            isLoggedIn = userAccount?.isLoggedIn == true,
            onApply = { updated -> onUpdateThemeCustomization(updated) },
            onResetDefault = { onResetThemeDefault() },
            onDismiss = { showFullColorStudio = false }
        )
    }
}

@Composable
private fun SettingsSectionHeader(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconTint.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onCheckedChange(!checked) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.padding(end = 8.dp)) {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = description,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.testTag(testTag)
            )
        }
    }
}

@Composable
fun LanguageOptionCard(
    language: Language,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick)
            .testTag("lang_option_${language.code}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
            else MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = if (isSelected) androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary)
            else androidx.compose.ui.graphics.SolidColor(Color.Transparent),
            width = if (isSelected) 2.dp else 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = language.flag, fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = language.label,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 13.sp
                )
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun ThemeOptionCard(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = MaterialTheme.colorScheme.secondary
    Card(
        modifier = modifier
            .clickable(onClick = onClick)
            .testTag("theme_option_${title.lowercase()}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) accentColor.copy(alpha = 0.22f)
            else MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = if (isSelected) androidx.compose.ui.graphics.SolidColor(accentColor)
            else androidx.compose.ui.graphics.SolidColor(Color.Transparent),
            width = if (isSelected) 2.dp else 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
