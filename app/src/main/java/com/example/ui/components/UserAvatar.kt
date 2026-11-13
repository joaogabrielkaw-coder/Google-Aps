package com.example.ui.components

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.theme.LocalDevRoutineColors
import com.example.util.AvatarImageCodec
import java.io.File

private val PurplePrimary: Color
    @Composable get() = LocalDevRoutineColors.current.primary

private val PurplePrimaryLight: Color
    @Composable get() = LocalDevRoutineColors.current.primaryLight

private val RedAccent: Color
    @Composable get() = LocalDevRoutineColors.current.accent

/**
 * Avatar inteligente do desenvolvedor. Suporta:
 * 1. Imagens sincronizadas globalmente via Base64 Data URI (`data:image/jpeg;base64,...`)
 * 2. Fotos remotas (Google account com `https://`)
 * 3. Presets temáticos de dev (`preset:python`, `preset:cyber`, `preset:database`, etc.)
 * 4. Fotos locais legadas (`content://` ou `file://` existente localmente)
 * 5. Iniciais estilizadas com gradiente como fallback automático quando indisponível
 */
@Composable
fun UserAvatar(
    photoUrl: String?,
    displayName: String,
    size: Dp = 48.dp,
    modifier: Modifier = Modifier,
    borderWidth: Dp = 2.dp,
    borderColor: Color? = null,
    showEditBadge: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val effectiveBorder = borderColor ?: PurplePrimaryLight
    val clickableModifier = if (onClick != null) {
        modifier.clickable { onClick() }
    } else {
        modifier
    }

    Box(
        modifier = clickableModifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        val containerModifier = Modifier
            .size(size)
            .clip(CircleShape)
            .border(borderWidth, effectiveBorder, CircleShape)

        val cleanUrl = photoUrl?.trim().orEmpty()

        val decodedDataBitmap: ImageBitmap? = remember(cleanUrl) {
            if (cleanUrl.startsWith("data:image/", ignoreCase = true)) {
                val bytes = AvatarImageCodec.decodeDataUrlToBytes(cleanUrl)
                if (bytes != null && bytes.isNotEmpty()) {
                    try {
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
                    } catch (_: Exception) {
                        null
                    }
                } else {
                    null
                }
            } else {
                null
            }
        }

        val isBrokenLocalFile = remember(cleanUrl) {
            if (cleanUrl.startsWith("file://", ignoreCase = true)) {
                val path = Uri.parse(cleanUrl).path.orEmpty()
                path.isBlank() || !File(path).exists()
            } else {
                false
            }
        }

        var remoteLoadFailed by remember(cleanUrl) { mutableStateOf(false) }

        when {
            // Preset de Avatar Temático
            cleanUrl.startsWith("preset:") -> {
                val presetKey = cleanUrl.removePrefix("preset:")
                val (icon, bgColors) = getPresetAvatarInfo(presetKey, listOf(PurplePrimary, RedAccent))
                Box(
                    modifier = containerModifier.background(Brush.linearGradient(bgColors)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = "Avatar $presetKey",
                        tint = Color.White,
                        modifier = Modifier.size(size * 0.55f)
                    )
                }
            }

            // Data URI Base64 compartilhada globalmente no Firestore
            decodedDataBitmap != null -> {
                Image(
                    bitmap = decodedDataBitmap,
                    contentDescription = "Foto de perfil de $displayName",
                    contentScale = ContentScale.Crop,
                    modifier = containerModifier
                )
            }

            // URL remota (Google Photo) ou URI local acessível
            cleanUrl.isNotBlank() &&
                !cleanUrl.startsWith("data:image/", ignoreCase = true) &&
                !isBrokenLocalFile &&
                !remoteLoadFailed -> {
                val context = LocalContext.current
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(cleanUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Foto de perfil de $displayName",
                    contentScale = ContentScale.Crop,
                    modifier = containerModifier,
                    onError = { remoteLoadFailed = true },
                    onSuccess = { remoteLoadFailed = false }
                )
            }

            // Fallback: Iniciais estilizadas
            else -> {
                val initials = displayName.trim().take(2).uppercase().ifBlank { "DV" }
                Box(
                    modifier = containerModifier.background(
                        Brush.linearGradient(listOf(PurplePrimary, RedAccent))
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = (size.value * 0.38f).sp
                    )
                }
            }
        }

        // Selo flutuante de edição (Câmera)
        if (showEditBadge) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 2.dp, y = 2.dp)
                    .size(size * 0.34f)
                    .clip(CircleShape)
                    .background(RedAccent)
                    .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Alterar Foto de Perfil",
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.20f)
                )
            }
        }
    }
}

/**
 * Retorna o ícone e gradiente de fundo para os avatares temáticos
 */
fun getPresetAvatarInfo(
    key: String,
    defaultGradientColors: List<Color> = listOf(com.example.ui.theme.PurplePrimary, com.example.ui.theme.RedAccent)
): Pair<ImageVector, List<Color>> {
    return when (key.lowercase()) {
        "python" -> Icons.Default.Terminal to listOf(Color(0xFF1B5E20), Color(0xFF4CAF50))
        "cyber" -> Icons.Default.Security to listOf(Color(0xFF0D47A1), Color(0xFF00E5FF))
        "database" -> Icons.Default.Storage to listOf(Color(0xFF4A148C), Color(0xFFBA68C8))
        "astronaut" -> Icons.Default.RocketLaunch to listOf(Color(0xFFBF360C), Color(0xFFFF9800))
        "wizard" -> Icons.Default.AutoAwesome to listOf(Color(0xFF880E4F), Color(0xFFFF4081))
        "hero" -> Icons.Default.Devices to listOf(Color(0xFF263238), Color(0xFF607D8B))
        else -> Icons.Default.AutoAwesome to defaultGradientColors
    }
}
