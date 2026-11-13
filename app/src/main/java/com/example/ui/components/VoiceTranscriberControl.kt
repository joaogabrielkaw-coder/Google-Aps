package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.model.AudioTranscriptionUiState
import com.example.model.Language
import com.example.ui.theme.LocalDevRoutineColors
import com.example.util.AudioRecorderHelper
import kotlinx.coroutines.delay

private val PurplePrimary: Color
    @Composable get() = LocalDevRoutineColors.current.primary

private val RedAccent: Color
    @Composable get() = LocalDevRoutineColors.current.accent

/**
 * Controle reutilizável de gravação de áudio via microfone + transcrição com gemini-3.5-transcribe.
 * Gerencia permissão RECORD_AUDIO em runtime, medidor de amplitude e estados Loading/Error/Success.
 */
@Composable
fun VoiceRecorderIconButton(
    transcriptionState: AudioTranscriptionUiState,
    language: Language,
    onRecordingStateChanged: (Boolean) -> Unit,
    onAudioCaptured: (bytes: ByteArray, mimeType: String, durationSeconds: Int) -> Unit,
    onPermissionDenied: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isPt = language == Language.PT
    val recorder = remember { AudioRecorderHelper(context) }

    DisposableEffect(Unit) {
        onDispose {
            recorder.cancelRecording()
        }
    }

    val startCapture = {
        val started = recorder.startRecording()
        onRecordingStateChanged(started)
        if (!started) {
            onPermissionDenied()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startCapture()
        } else {
            onPermissionDenied()
        }
    }

    val isRecording = transcriptionState is AudioTranscriptionUiState.Recording
    val isTranscribing = transcriptionState is AudioTranscriptionUiState.Transcribing

    IconButton(
        onClick = {
            if (isRecording) {
                onRecordingStateChanged(false)
                val payload = recorder.stopRecordingAndReadBytes()
                if (payload != null) {
                    onAudioCaptured(payload.bytes, payload.mimeType, payload.durationSeconds)
                }
            } else if (!isTranscribing) {
                val hasPerm = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
                if (hasPerm) {
                    startCapture()
                } else {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            }
        },
        enabled = !isTranscribing,
        modifier = modifier
            .minimumInteractiveComponentSize()
            .size(48.dp)
            .clip(CircleShape)
            .background(
                when {
                    isRecording -> RedAccent
                    isTranscribing -> PurplePrimary.copy(alpha = 0.2f)
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
            )
            .testTag("btn_voice_transcribe_mic")
    ) {
        when {
            isTranscribing -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = PurplePrimary
                )
            }
            isRecording -> {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = if (isPt) "Parar gravação" else "Stop recording",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            else -> {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = if (isPt) "Gravar áudio para transcrever" else "Record audio to transcribe",
                    tint = PurplePrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
fun VoiceTranscriptionStatusBanner(
    transcriptionState: AudioTranscriptionUiState,
    language: Language,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPt = language == Language.PT
    var ampLevel by remember { mutableFloatStateOf(0.15f) }
    var elapsedSeconds by remember { mutableIntStateOf(0) }

    LaunchedEffect(transcriptionState) {
        if (transcriptionState is AudioTranscriptionUiState.Recording) {
            elapsedSeconds = 0
            while (true) {
                delay(120)
                // Gentle animated meter + 1s clock counter
                ampLevel = ((System.currentTimeMillis() % 700) / 700f).coerceIn(0.2f, 1f)
                if ((System.currentTimeMillis() % 1000) < 130) {
                    elapsedSeconds++
                }
            }
        }
    }

    AnimatedVisibility(
        visible = transcriptionState !is AudioTranscriptionUiState.Idle &&
            transcriptionState !is AudioTranscriptionUiState.Success,
        modifier = modifier
    ) {
        when (transcriptionState) {
            is AudioTranscriptionUiState.Recording -> {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = RedAccent.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, RedAccent.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("banner_audio_recording")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = RedAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (isPt) "Gravando áudio (${elapsedSeconds}s)..."
                                    else "Recording audio (${elapsedSeconds}s)...",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RedAccent
                                )
                                Text(
                                    text = if (isPt) "Toque em Parar para transcrever com gemini-3.5-transcribe"
                                    else "Tap Stop to transcribe via gemini-3.5-transcribe",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf(0.55f, 0.95f, 0.7f, 1f, 0.6f).forEachIndexed { idx, factor ->
                                val animatedH by animateFloatAsState(
                                    targetValue = (ampLevel * factor * 18f).coerceIn(4f, 20f),
                                    animationSpec = tween(120),
                                    label = "wave_$idx"
                                )
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .height(animatedH.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(RedAccent)
                                )
                            }
                        }
                    }
                }
            }

            is AudioTranscriptionUiState.Transcribing -> {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = PurplePrimary.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, PurplePrimary.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("banner_audio_transcribing")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = PurplePrimary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isPt) "Transcrevendo áudio com gemini-3.5-transcribe..."
                            else "Transcribing audio with gemini-3.5-transcribe...",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PurplePrimary
                        )
                    }
                }
            }

            is AudioTranscriptionUiState.Error -> {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("banner_audio_error")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = transcriptionState.message,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = if (isPt) "Dispensar erro" else "Dismiss error",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            else -> Unit
        }
    }
}
