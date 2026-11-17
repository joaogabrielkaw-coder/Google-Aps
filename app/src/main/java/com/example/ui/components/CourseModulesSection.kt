package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.OndemandVideo
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.TechCourseModulesRepository
import com.example.model.CourseLesson
import com.example.model.CourseModule
import com.example.model.CourseReference
import com.example.model.CourseResourceType
import com.example.model.Language
import com.example.model.Technology
import com.example.ui.theme.RedAccent

fun openExternalCourseUrl(context: Context, url: String, isPt: Boolean) {
    runCatching {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }.onFailure {
        Toast.makeText(
            context,
            if (isPt) "Não foi possível abrir o link no navegador." else "Could not open the link in browser.",
            Toast.LENGTH_SHORT
        ).show()
    }
}

private fun resourceIcon(type: CourseResourceType): ImageVector = when (type) {
    CourseResourceType.YOUTUBE_PLAYLIST -> Icons.Default.VideoLibrary
    CourseResourceType.YOUTUBE_VIDEO -> Icons.Default.OndemandVideo
    CourseResourceType.FREE_COURSE_SITE -> Icons.Default.Public
    CourseResourceType.OFFICIAL_DOCS -> Icons.Default.MenuBook
}

@Composable
fun CourseModulesAndVideosTab(
    tech: Technology,
    language: Language,
    completedLessonIds: Set<String>,
    onToggleLessonCompleted: (String) -> Unit,
    onJumpToPractice: () -> Unit
) {
    val isPt = language == Language.PT
    val context = LocalContext.current
    val curriculum = remember(tech) { TechCourseModulesRepository.getCurriculum(tech) }
    val progress = remember(tech, completedLessonIds) {
        TechCourseModulesRepository.getCourseProgress(tech, completedLessonIds)
    }

    val expandedModules = remember(tech) {
        mutableStateMapOf<String, Boolean>().apply {
            curriculum.modules.forEachIndexed { index, mod ->
                put(mod.id, index == 0)
            }
        }
    }

    val hours = progress.totalMinutes / 60
    val mins = progress.totalMinutes % 60
    val durationText = if (hours > 0) "${hours}h ${mins}min" else "${mins} min"

    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.testTag("list_course_modules_${tech.id}")
    ) {
        // 1. Course Progress Summary Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                ),
                border = BorderStroke(1.dp, tech.accentColor.copy(alpha = 0.45f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(tech.accentColor.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    tint = tech.accentColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (isPt) "Trilha Modular de ${tech.displayName}"
                                    else "${tech.displayName} Modular Course",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = if (isPt) {
                                        "${curriculum.modules.size} módulos • ${progress.totalLessons} aulas • ~$durationText"
                                    } else {
                                        "${curriculum.modules.size} modules • ${progress.totalLessons} lessons • ~$durationText"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = tech.accentColor.copy(alpha = 0.22f)
                        ) {
                            Text(
                                text = "${progress.progressPercent}%",
                                color = tech.accentColor,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LinearProgressIndicator(
                        progress = { progress.progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = tech.accentColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isPt) {
                            "${progress.completedLessons} de ${progress.totalLessons} aulas marcadas • Apenas auxílio aos estudos"
                        } else {
                            "${progress.completedLessons} of ${progress.totalLessons} lessons checked • Study aid only"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                    ) {
                        Text(
                            text = if (isPt) {
                                "ℹ️ Os cursos em vídeo e aulas são apenas de apoio gratuito (sem ganho de XP). Ganhe pontos nas Atividades Práticas e Quizzes!"
                            } else {
                                "ℹ️ Video courses and lessons are free study aids (no XP earned). Earn XP in Hands-on Practice & Scored Quizzes!"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = {
                            openExternalCourseUrl(
                                context,
                                "https://www.cursoemvideo.com/cursos/",
                                isPt
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("button_open_cursoemvideo_portal")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = null,
                            tint = RedAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isPt) "Abrir Portal Curso em Vídeo (Guanabara)"
                            else "Open Curso em Vídeo Portal (Guanabara)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        // 2. Section Header: Módulos & Aulas
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isPt) "Módulos & Aulas Práticas" else "Modules & Practical Lessons",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = if (isPt) "Toque para expandir" else "Tap to expand",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // 3. Expandable Module Cards
        items(curriculum.modules, key = { it.id }) { module ->
            val isExpanded = expandedModules[module.id] ?: false
            CourseModuleAccordionCard(
                module = module,
                tech = tech,
                language = language,
                isExpanded = isExpanded,
                completedLessonIds = completedLessonIds,
                onToggleExpand = { expandedModules[module.id] = !isExpanded },
                onToggleLessonCompleted = onToggleLessonCompleted,
                onOpenVideo = { url -> openExternalCourseUrl(context, url, isPt) },
                onJumpToPractice = onJumpToPractice
            )
        }

        // 4. Section Header: Free Video & Course References
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Column {
                Text(
                    text = if (isPt) "Vídeos & Cursos Livres Gratuitos"
                    else "Free Video & Online Course References",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isPt) {
                        "Playlists verificadas no YouTube e portais gratuitos oficiais para complementar ${tech.displayName}."
                    } else {
                        "Verified free YouTube playlists and official interactive portals to master ${tech.displayName}."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 5. Reference Cards
        items(curriculum.references, key = { it.id }) { ref ->
            CourseReferenceCard(
                reference = ref,
                tech = tech,
                language = language,
                onOpenUrl = { openExternalCourseUrl(context, ref.url, isPt) }
            )
        }
    }
}

@Composable
private fun CourseModuleAccordionCard(
    module: CourseModule,
    tech: Technology,
    language: Language,
    isExpanded: Boolean,
    completedLessonIds: Set<String>,
    onToggleExpand: () -> Unit,
    onToggleLessonCompleted: (String) -> Unit,
    onOpenVideo: (String) -> Unit,
    onJumpToPractice: () -> Unit
) {
    val isPt = language == Language.PT
    val completedInModule = module.lessons.count { completedLessonIds.contains(it.id) }
    val totalInModule = module.lessons.size
    val allDone = totalInModule > 0 && completedInModule == totalInModule

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(
            width = 1.dp,
            color = if (allDone) Color(0xFF2E7D32).copy(alpha = 0.65f)
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_module_${module.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Module clickable header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onToggleExpand() }
                    .testTag("button_toggle_module_${module.id}")
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(
                            if (allDone) Color(0xFF2E7D32) else tech.accentColor.copy(alpha = 0.22f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${module.moduleNumber}",
                        color = if (allDone) Color.White else tech.accentColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = module.levelLabel(language),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = "$completedInModule/$totalInModule " + if (isPt) "aulas" else "lessons",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (allDone) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = module.title(language),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(
                    onClick = onToggleExpand,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) {
                            if (isPt) "Recolher módulo" else "Collapse module"
                        } else {
                            if (isPt) "Expandir módulo" else "Expand module"
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = module.objective(language),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Module Quick Actions Row (Watch YouTube + Practice Challenge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onOpenVideo(module.primaryVideoUrl) },
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("button_watch_module_${module.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayCircleFilled,
                        contentDescription = null,
                        tint = RedAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = module.primaryVideoTitle(language),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Button(
                    onClick = onJumpToPractice,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("button_practice_module_${module.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isPt) "Praticar" else "Practice",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    module.lessons.forEach { lesson ->
                        CourseLessonItemRow(
                            lesson = lesson,
                            tech = tech,
                            language = language,
                            isCompleted = completedLessonIds.contains(lesson.id),
                            onToggleCompleted = { onToggleLessonCompleted(lesson.id) },
                            onOpenVideo = { onOpenVideo(lesson.videoRefUrl) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CourseLessonItemRow(
    lesson: CourseLesson,
    tech: Technology,
    language: Language,
    isCompleted: Boolean,
    onToggleCompleted: () -> Unit,
    onOpenVideo: () -> Unit
) {
    val isPt = language == Language.PT
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isCompleted) Color(0xFF1B4D2B).copy(alpha = 0.22f)
        else MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
        border = BorderStroke(
            width = 1.dp,
            color = if (isCompleted) Color(0xFF4CAF50).copy(alpha = 0.55f)
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onToggleCompleted,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("checkbox_lesson_${lesson.id}")
                ) {
                    Icon(
                        imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = if (isCompleted) {
                            if (isPt) "Marcar aula como pendente" else "Mark lesson incomplete"
                        } else {
                            if (isPt) "Concluir aula" else "Complete lesson"
                        },
                        tint = if (isCompleted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = (if (isPt) "Aula ${lesson.lessonNumber}" else "Lesson ${lesson.lessonNumber}"),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = tech.accentColor
                        )
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "${lesson.durationMinutes} min",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = lesson.title(language),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Text(
                text = lesson.summary(language),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 12.dp, end = 6.dp, bottom = 8.dp)
            )

            // Topic chips row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(start = 12.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                lesson.topics(language).forEach { topic ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = topic,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Direct Video/Reference Link Button inside Lesson
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = RedAccent.copy(alpha = 0.12f),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .padding(horizontal = 8.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onOpenVideo() }
                    .testTag("button_lesson_video_${lesson.id}")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.OndemandVideo,
                            contentDescription = null,
                            tint = RedAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = lesson.videoRefLabel(language),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = RedAccent,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = if (isPt) "Abrir referência em vídeo" else "Open video reference",
                        tint = RedAccent,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CourseReferenceCard(
    reference: CourseReference,
    tech: Technology,
    language: Language,
    onOpenUrl: () -> Unit
) {
    val isPt = language == Language.PT
    val isYoutube = reference.type == CourseResourceType.YOUTUBE_PLAYLIST ||
        reference.type == CourseResourceType.YOUTUBE_VIDEO
    val badgeColor = if (isYoutube) RedAccent else tech.accentColor

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.35f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_reference_${reference.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = badgeColor.copy(alpha = 0.16f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = resourceIcon(reference.type),
                                contentDescription = null,
                                tint = badgeColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = reference.type.label(language),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = badgeColor
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = if (reference.isPortuguese) "PT-BR" else "EN",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                    }
                }

                Text(
                    text = reference.durationLabel,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = reference.title(language),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = reference.provider,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = reference.description(language),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onOpenUrl,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isYoutube) RedAccent else MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("button_open_ref_${reference.id}")
            ) {
                Icon(
                    imageVector = if (isYoutube) Icons.Default.PlayCircleFilled else Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isYoutube) {
                        if (isPt) "Assistir Curso Gratuito no YouTube" else "Watch Free Course on YouTube"
                    } else {
                        if (isPt) "Abrir Curso Livre / Documentação" else "Open Free Course / Docs"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun CourseOverviewQuickReferencesCard(
    tech: Technology,
    language: Language,
    completedLessonIds: Set<String>,
    onOpenModulesTab: () -> Unit
) {
    val isPt = language == Language.PT
    val context = LocalContext.current
    val curriculum = remember(tech) { TechCourseModulesRepository.getCurriculum(tech) }
    val progress = remember(tech, completedLessonIds) {
        TechCourseModulesRepository.getCourseProgress(tech, completedLessonIds)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, tech.accentColor.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isPt) "🎬 Módulos & Cursos Livres Gratuitos"
                    else "🎬 Modules & Free Video Courses",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = tech.accentColor,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${progress.completedLessons}/${progress.totalLessons}",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isPt) {
                    "${curriculum.modules.size} módulos práticos organizados em aulas + ${curriculum.references.size} referências gratuitas no YouTube e sites oficiais."
                } else {
                    "${curriculum.modules.size} practical lesson modules + ${curriculum.references.size} free YouTube & official web course links."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            curriculum.references.take(2).forEach { ref ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .heightIn(min = 48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { openExternalCourseUrl(context, ref.url, isPt) }
                        .testTag("button_overview_ref_${ref.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = resourceIcon(ref.type),
                            contentDescription = null,
                            tint = RedAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = ref.title(language),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${ref.provider} • ${ref.durationLabel}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = if (isPt) "Abrir curso externo" else "Open external course",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onOpenModulesTab,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("button_open_modules_tab_from_overview")
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isPt) "Ver Todos os Módulos & Vídeos" else "View All Modules & Videos",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
