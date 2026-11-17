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
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.Translate
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.AccessibilitySettings
import com.example.model.AppGeneralSettings
import com.example.model.FontScalePreset
import com.example.model.Language
import com.example.model.ProSubscriptionState
import com.example.model.ThemeCustomization
import com.example.model.ThemePreference
import com.example.model.ThemePreset
import com.example.model.UserAccount
import com.example.model.UserFeedbackNote
import com.example.ui.components.FullColorPaletteDialog
import com.example.ui.components.PresetPaletteChip
import com.example.ui.components.ProSubscriptionBannerCard
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
    onSendTestNotification: () -> Unit = {},
    targetSubjectsCount: Int = 0,
    onOpenTargetSubjects: () -> Unit = {},
    proSubscriptionState: ProSubscriptionState = ProSubscriptionState(),
    onOpenProSubscription: () -> Unit = {}
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
                            contentDescription = stringResource(R.string.common_back),
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
                            text = stringResource(R.string.settings_title),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
                        )
                        Text(
                            text = stringResource(R.string.settings_subtitle),
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

        // 2b. DevRoutine Pro Monthly Subscription Card (R$ 13/mês • Sem anúncios +15 vidas/dia)
        item {
            ProSubscriptionBannerCard(
                subscriptionState = proSubscriptionState,
                language = currentLanguage,
                onOpenSubscriptionDialog = onOpenProSubscription
            )
        }

        // 2c. Dedicated Dynamic App Language Localization Card (Portuguese / English)
        if (selectedCategory == SettingsCategory.ALL || selectedCategory == SettingsCategory.GENERAL) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = BorderStroke(
                        1.2.dp,
                        Brush.linearGradient(listOf(primaryColor.copy(alpha = 0.65f), Color(0xFF00ACC1).copy(alpha = 0.65f)))
                    ),
                    modifier = Modifier.testTag("card_settings_language_toggle")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SettingsSectionHeader(
                            icon = Icons.Default.Translate,
                            iconTint = Color(0xFF00ACC1),
                            title = stringResource(R.string.settings_lang_card_title),
                            subtitle = stringResource(R.string.settings_lang_card_subtitle)
                        )

                        SettingsToggleRow(
                            icon = Icons.Default.Language,
                            iconTint = primaryColor,
                            title = stringResource(R.string.settings_lang_toggle_label),
                            description = stringResource(R.string.settings_lang_toggle_desc),
                            checked = currentLanguage == Language.EN,
                            onCheckedChange = { useEnglish ->
                                onSetLanguage(if (useEnglish) Language.EN else Language.PT)
                            },
                            testTag = "switch_toggle_language"
                        )

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

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = primaryColor.copy(alpha = 0.14f),
                            border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(R.string.settings_lang_active_res_badge),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor,
                                modifier = Modifier
                                    .padding(horizontal = 10.dp, vertical = 7.dp)
                                    .testTag("badge_active_strings_resource")
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(R.string.settings_lang_live_preview),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
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
                            title = stringResource(R.string.settings_accessibility_title),
                            subtitle = stringResource(R.string.settings_accessibility_subtitle)
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
                                    text = stringResource(R.string.settings_text_size),
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
                                    text = stringResource(R.string.settings_text_size_preview),
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        // High Contrast Borders Toggle
                        SettingsToggleRow(
                            icon = Icons.Default.Contrast,
                            iconTint = primaryColor,
                            title = stringResource(R.string.settings_high_contrast_title),
                            description = stringResource(R.string.settings_high_contrast_desc),
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
                            title = stringResource(R.string.settings_reduce_motion_title),
                            description = stringResource(R.string.settings_reduce_motion_desc),
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
                            title = stringResource(R.string.settings_haptic_title),
                            description = stringResource(R.string.settings_haptic_desc),
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
                                    text = stringResource(R.string.settings_reset_accessibility),
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
                                        text = stringResource(R.string.settings_theme_title),
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
                                            text = stringResource(
                                                if (userAccount?.isLoggedIn == true) R.string.settings_theme_synced
                                                else R.string.settings_theme_local
                                            ),
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
                                title = stringResource(R.string.settings_theme_dark),
                                icon = Icons.Default.DarkMode,
                                isSelected = currentTheme == ThemePreference.DARK,
                                onClick = { onSetTheme(ThemePreference.DARK) },
                                modifier = Modifier.weight(1f)
                            )

                            ThemeOptionCard(
                                title = stringResource(R.string.settings_theme_light),
                                icon = Icons.Default.LightMode,
                                isSelected = currentTheme == ThemePreference.LIGHT,
                                onClick = { onSetTheme(ThemePreference.LIGHT) },
                                modifier = Modifier.weight(1f)
                            )

                            ThemeOptionCard(
                                title = stringResource(R.string.settings_theme_system),
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
                                        text = stringResource(R.string.settings_active_palette),
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    val activePreset = ThemePreset.fromId(themeCustomization.presetId)
                                    val label = when {
                                        themeCustomization.isDefaultPalette -> stringResource(R.string.settings_default_palette)
                                        activePreset != null -> if (isPt) activePreset.namePt else activePreset.nameEn
                                        else -> stringResource(
                                            R.string.settings_custom_palette,
                                            ThemeCustomization.formatHex(themeCustomization.primaryColorHex)
                                        )
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
                            text = stringResource(R.string.settings_preset_palettes),
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
                                    text = stringResource(R.string.settings_full_palette_btn),
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
                                    Text(stringResource(R.string.settings_reset_btn), fontSize = 12.sp)
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
                            title = stringResource(R.string.settings_general_title),
                            subtitle = stringResource(R.string.settings_general_subtitle)
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
                                        text = userAccount?.displayName ?: stringResource(R.string.settings_default_dev_name),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = (if (isPt) userAccount?.levelTitlePt else userAccount?.levelTitleEn)
                                            ?: stringResource(R.string.settings_default_dev_level),
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
                                Text(text = stringResource(R.string.settings_photo_btn), fontSize = 12.sp)
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
                                text = stringResource(R.string.settings_redo_diagnostic_btn),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        OutlinedButton(
                            onClick = onOpenTargetSubjects,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.6f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_settings_target_subjects")
                        ) {
                            Icon(Icons.Default.TrackChanges, contentDescription = null, tint = primaryColor, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (targetSubjectsCount > 0) {
                                    stringResource(R.string.settings_target_subjects_count, targetSubjectsCount)
                                } else {
                                    stringResource(R.string.settings_target_subjects_choose)
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = primaryColor
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
                                    text = stringResource(R.string.settings_idle_pause_title),
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            Text(
                                text = stringResource(R.string.settings_idle_pause_desc),
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
                                                    stringResource(R.string.settings_idle_min_default, mins)
                                                } else {
                                                    stringResource(R.string.settings_idle_min, mins)
                                                },
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
                            title = stringResource(R.string.settings_daily_reminders_title),
                            description = stringResource(R.string.settings_daily_reminders_desc),
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
                                text = stringResource(R.string.settings_test_notification_btn),
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
                                text = stringResource(R.string.settings_covered_techs_title),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = stringResource(R.string.settings_covered_techs_body),
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
                            title = stringResource(R.string.settings_feedbacks_title),
                            subtitle = stringResource(R.string.settings_feedbacks_subtitle)
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
                                        text = stringResource(R.string.settings_feedbacks_how_title),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                                Text(
                                    text = stringResource(R.string.settings_feedbacks_how_body),
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
                            title = stringResource(R.string.settings_celebration_title),
                            description = stringResource(R.string.settings_celebration_desc),
                            checked = appGeneralSettings.showCelebrationDialogs,
                            onCheckedChange = { checked ->
                                onUpdateAppGeneralSettings(appGeneralSettings.copy(showCelebrationDialogs = checked))
                            },
                            testTag = "switch_celebration_dialogs"
                        )

                        SettingsToggleRow(
                            icon = Icons.Default.Favorite,
                            iconTint = Color(0xFFFF4D7D),
                            title = stringResource(R.string.settings_gentle_retry_title),
                            description = stringResource(R.string.settings_gentle_retry_desc),
                            checked = appGeneralSettings.showGentleRetryTips,
                            onCheckedChange = { checked ->
                                onUpdateAppGeneralSettings(appGeneralSettings.copy(showGentleRetryTips = checked))
                            },
                            testTag = "switch_gentle_retry_tips"
                        )

                        SettingsToggleRow(
                            icon = Icons.Default.Star,
                            iconTint = primaryColor,
                            title = stringResource(R.string.settings_xp_banners_title),
                            description = stringResource(R.string.settings_xp_banners_desc),
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
                                text = stringResource(R.string.settings_open_weekly_ai_btn),
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
                                    text = stringResource(R.string.settings_send_feedback_title),
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
                                        text = stringResource(R.string.settings_rating_label),
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
                                            text = stringResource(R.string.settings_feedback_input_label),
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
                                        text = stringResource(R.string.settings_submit_feedback_btn),
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
                                                text = stringResource(R.string.settings_feedback_saved_banner),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }

                                if (userFeedbackNotes.isNotEmpty()) {
                                    Text(
                                        text = stringResource(R.string.settings_recent_feedbacks, userFeedbackNotes.size),
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
                                                        contentDescription = stringResource(R.string.common_remove),
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
