package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.IdleExecutionResult
import com.example.model.Language
import com.example.model.LanguageIdleCatalog
import com.example.model.Technology
import com.example.ui.components.LanguageIdleWorkspace

@Composable
fun LanguageIdleScreen(
    selectedTech: Technology,
    codeByTech: Map<Technology, String>,
    resultByTech: Map<Technology, IdleExecutionResult>,
    language: Language,
    onSelectTech: (Technology) -> Unit,
    onUpdateCode: (Technology, String) -> Unit,
    onRunCode: (Technology) -> Unit,
    onResetCode: (Technology) -> Unit,
    onClearOutput: (Technology) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val isPt = language == Language.PT
    val activeCode = codeByTech[selectedTech]
        ?: LanguageIdleCatalog.getSpec(selectedTech).templates.first().code
    val activeResult = resultByTech[selectedTech]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("screen_language_idles"),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp)
        ) {
            // Top Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("btn_back_language_idles")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.idle_screen_title),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = stringResource(R.string.idle_screen_subtitle),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = selectedTech.accentColor.copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, selectedTech.accentColor.copy(alpha = 0.45f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Terminal,
                                contentDescription = null,
                                tint = selectedTech.accentColor,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = stringResource(R.string.idle_badge_count),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = selectedTech.accentColor
                            )
                        }
                    }
                }
            }

            // Horizontal 12-Language Switcher
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(Technology.entries, key = { it.id }) { tech ->
                    val isSelected = tech == selectedTech
                    val spec = LanguageIdleCatalog.getSpec(tech)
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) tech.accentColor else MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) tech.accentColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onSelectTech(tech) }
                            .testTag("chip_select_idle_${tech.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = tech.icon,
                                contentDescription = null,
                                tint = if (isSelected) Color.White else tech.accentColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Column {
                                Text(
                                    text = tech.displayName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = spec.defaultFileName,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp,
                                    color = if (isSelected) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Dedicated IDLE Workspace for the selected language
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                LanguageIdleWorkspace(
                    tech = selectedTech,
                    code = activeCode,
                    executionResult = activeResult,
                    language = language,
                    onCodeChange = { newCode -> onUpdateCode(selectedTech, newCode) },
                    onRunCode = { onRunCode(selectedTech) },
                    onResetCode = { onResetCode(selectedTech) },
                    onClearOutput = { onClearOutput(selectedTech) }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
