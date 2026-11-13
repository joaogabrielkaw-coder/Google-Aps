package com.example.util

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File

private const val TAG = "AudioRecorderHelper"
private const val MAX_INLINE_AUDIO_BYTES = 18 * 1024 * 1024 // < 20 MB inline limit

/**
 * Helper responsável por capturar áudio do microfone em AAC/M4A no cache do app
 * para transcrição multimodal com Firebase AI Logic (gemini-3.5-transcribe).
 */
class AudioRecorderHelper(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var startTimeMillis: Long = 0L

    val isRecording: Boolean
        get() = recorder != null

    fun startRecording(): Boolean {
        stopAndReleaseInternal()
        return try {
            val file = File(context.cacheDir, "voice_input_${System.currentTimeMillis()}.m4a")
            val mr = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            mr.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(16000)
                setAudioEncodingBitRate(64000)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            recorder = mr
            outputFile = file
            startTimeMillis = System.currentTimeMillis()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start MediaRecorder", e)
            stopAndReleaseInternal()
            false
        }
    }

    /**
     * Normalized microphone level in 0f..1f for live UI waveform animation.
     */
    fun getNormalizedAmplitude(): Float {
        return try {
            val amp = recorder?.maxAmplitude ?: 0
            (amp / 18000f).coerceIn(0.05f, 1f)
        } catch (e: Exception) {
            0.05f
        }
    }

    fun getElapsedSeconds(): Int {
        if (startTimeMillis <= 0L) return 0
        return ((System.currentTimeMillis() - startTimeMillis) / 1000L).toInt().coerceAtLeast(0)
    }

    /**
     * Stops recording and returns the recorded bytes, mimeType, and duration in seconds.
     */
    fun stopRecordingAndReadBytes(): RecordedAudioPayload? {
        val elapsedSec = getElapsedSeconds().coerceAtLeast(1)
        val file = outputFile
        try {
            recorder?.stop()
        } catch (e: Exception) {
            Log.w(TAG, "MediaRecorder stop error (short clip?): ${e.message}")
        } finally {
            stopAndReleaseInternal()
        }

        if (file == null || !file.exists() || file.length() <= 0L) {
            return null
        }

        return try {
            val bytes = file.readBytes()
            file.delete()
            if (bytes.size > MAX_INLINE_AUDIO_BYTES) {
                Log.e(TAG, "Recorded audio exceeded max inline size (${bytes.size} bytes)")
                null
            } else {
                RecordedAudioPayload(
                    bytes = bytes,
                    mimeType = "audio/mp4",
                    durationSeconds = elapsedSec
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read recorded audio bytes", e)
            null
        } finally {
            outputFile = null
        }
    }

    fun cancelRecording() {
        stopAndReleaseInternal()
        outputFile?.delete()
        outputFile = null
    }

    private fun stopAndReleaseInternal() {
        try {
            recorder?.reset()
            recorder?.release()
        } catch (e: Exception) {
            Log.w(TAG, "MediaRecorder release warning: ${e.message}")
        } finally {
            recorder = null
        }
    }
}

data class RecordedAudioPayload(
    val bytes: ByteArray,
    val mimeType: String,
    val durationSeconds: Int
)
