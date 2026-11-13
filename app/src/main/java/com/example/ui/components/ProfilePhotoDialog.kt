package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.Language
import com.example.ui.theme.LocalDevRoutineColors
import com.example.util.AvatarImageCodec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val PurplePrimary: Color
    @Composable get() = LocalDevRoutineColors.current.primary

private val RedAccent: Color
    @Composable get() = LocalDevRoutineColors.current.accent

data class PresetAvatarOption(
    val key: String,
    val labelPt: String,
    val labelEn: String
)

val PRESET_AVATARS = listOf(
    PresetAvatarOption("python", "Python Coder", "Python Coder"),
    PresetAvatarOption("cyber", "Cyber Ninja", "Cyber Ninja"),
    PresetAvatarOption("database", "Mestre SQL", "SQL Master"),
    PresetAvatarOption("astronaut", "Dev Cósmico", "Cosmic Dev"),
    PresetAvatarOption("wizard", "Mago da IA", "AI Wizard"),
    PresetAvatarOption("hero", "FullStack", "FullStack")
)

/**
 * Diálogo para escolha da foto de perfil:
 * 1. Seleção direta da galeria via Android Photo Picker (zero permissões, comprimida em Data URI Base64 para sincronizar com todos no Placar)
 * 2. Avatares temáticos de desenvolvedor prontos
 * 3. Opção de restaurar iniciais
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfilePhotoDialog(
    currentPhotoUrl: String?,
    displayName: String,
    language: Language,
    onSavePhoto: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val isPt = language == Language.PT
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedPhoto by remember { mutableStateOf(currentPhotoUrl.orEmpty()) }
    var isProcessingPhoto by remember { mutableStateOf(false) }
    var photoErrorMessage by remember { mutableStateOf<String?>(null) }

    // Android Photo Picker zero-permission launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            photoErrorMessage = null
            isProcessingPhoto = true
            scope.launch {
                val encodedDataUrl = withContext(Dispatchers.IO) {
                    AvatarImageCodec.encodeUriToDataUrl(context, uri)
                }
                isProcessingPhoto = false
                if (!encodedDataUrl.isNullOrBlank()) {
                    selectedPhoto = encodedDataUrl
                } else {
                    photoErrorMessage = if (isPt) {
                        "Não foi possível processar esta imagem. Tente escolher outra foto."
                    } else {
                        "Could not process this image. Please try picking another photo."
                    }
                }
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("dialog_profile_photo"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isPt) "Foto de Perfil do Dev" else "Developer Profile Photo",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = if (isPt) "Fechar" else "Close"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Avatar Preview
                Box(contentAlignment = Alignment.Center) {
                    UserAvatar(
                        photoUrl = selectedPhoto,
                        displayName = displayName,
                        size = 90.dp,
                        borderWidth = 3.dp,
                        borderColor = RedAccent
                    )
                    if (isProcessingPhoto) {
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.55f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(30.dp),
                                strokeWidth = 3.dp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = displayName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = null,
                        tint = PurplePrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isPt) "Sua foto será visível para todos no Placar!"
                        else "Your photo will be visible to everyone on the Leaderboard!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                if (photoErrorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = photoErrorMessage.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action 1: Escolher foto da galeria do celular
                Button(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    enabled = !isProcessingPhoto,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_pick_gallery_photo"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isProcessingPhoto) {
                            if (isPt) "Preparando foto..." else "Preparing photo..."
                        } else {
                            if (isPt) "Escolher Foto da Galeria" else "Choose from Gallery"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                Spacer(modifier = Modifier.height(14.dp))

                // Section 2: Avatares Temáticos de Dev
                Text(
                    text = if (isPt) "Ou escolha um Avatar Dev:" else "Or pick a Developer Avatar:",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    maxItemsInEachRow = 3
                ) {
                    PRESET_AVATARS.forEach { option ->
                        val presetVal = "preset:${option.key}"
                        val isSelected = selectedPhoto == presetVal

                        Column(
                            modifier = Modifier
                                .padding(vertical = 6.dp)
                                .clickable {
                                    photoErrorMessage = null
                                    selectedPhoto = presetVal
                                }
                                .padding(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                UserAvatar(
                                    photoUrl = presetVal,
                                    displayName = option.labelPt,
                                    size = 54.dp,
                                    borderWidth = if (isSelected) 3.dp else 1.dp,
                                    borderColor = if (isSelected) RedAccent else Color.Transparent
                                )
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .background(RedAccent),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = if (isPt) option.labelPt else option.labelEn,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) RedAccent else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Clear / Reset to initials button
                if (selectedPhoto.isNotBlank()) {
                    OutlinedButton(
                        onClick = {
                            photoErrorMessage = null
                            selectedPhoto = ""
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isPt) "Remover Foto (Usar Iniciais)" else "Remove Photo (Use Initials)",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Final action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (isPt) "Cancelar" else "Cancel")
                    }

                    Button(
                        onClick = {
                            onSavePhoto(selectedPhoto)
                            onDismiss()
                        },
                        enabled = !isProcessingPhoto,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_save_profile_photo"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RedAccent)
                    ) {
                        Text(
                            text = if (isPt) "Salvar Foto" else "Save Photo",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
