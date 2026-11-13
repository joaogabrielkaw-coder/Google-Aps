package com.example.util

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

/**
 * Procedural PCM audio synthesizer using Android AudioTrack for tactile 3D game feedback.
 * Supports instant mute toggle and runs asynchronously off the UI thread.
 */
object SoundEffectsEngine {
    private const val SAMPLE_RATE = 22050
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Volatile
    var isMuted: Boolean = false

    fun playPickupChime(comboMultiplier: Int = 1) {
        if (isMuted) return
        val baseFreq = 520.0 + (comboMultiplier.coerceAtMost(8) * 55.0)
        playToneSequence(
            frequencies = listOf(baseFreq, baseFreq * 1.25),
            durationsMs = listOf(55, 85),
            volume = 0.25f
        )
    }

    fun playJumpWoosh() {
        if (isMuted) return
        playSweep(startFreq = 260.0, endFreq = 620.0, durationMs = 110, volume = 0.22f)
    }

    fun playImpactThud() {
        if (isMuted) return
        playSweep(startFreq = 190.0, endFreq = 68.0, durationMs = 160, volume = 0.35f)
    }

    fun playStartFanfare() {
        if (isMuted) return
        playToneSequence(
            frequencies = listOf(440.0, 554.37, 659.25, 880.0),
            durationsMs = listOf(65, 65, 65, 130),
            volume = 0.24f
        )
    }

    private fun playToneSequence(
        frequencies: List<Double>,
        durationsMs: List<Int>,
        volume: Float
    ) {
        scope.launch {
            try {
                val totalSamples = durationsMs.sumOf { (SAMPLE_RATE * it) / 1000 }
                val buffer = ShortArray(totalSamples)
                var offset = 0
                frequencies.forEachIndexed { idx, freq ->
                    val count = (SAMPLE_RATE * durationsMs.getOrElse(idx) { 60 }) / 1000
                    for (i in 0 until count) {
                        val env = 1.0 - (i.toDouble() / count.toDouble())
                        val sample = sin(2.0 * PI * freq * i / SAMPLE_RATE) * env * volume
                        buffer[offset + i] = (sample * Short.MAX_VALUE).toInt().toShort()
                    }
                    offset += count
                }
                emitPcmBuffer(buffer)
            } catch (_: Throwable) {
                // Ignore audio hardware limitations in headless tests
            }
        }
    }

    private fun playSweep(
        startFreq: Double,
        endFreq: Double,
        durationMs: Int,
        volume: Float
    ) {
        scope.launch {
            try {
                val count = (SAMPLE_RATE * durationMs) / 1000
                val buffer = ShortArray(count)
                var phase = 0.0
                for (i in 0 until count) {
                    val t = i.toDouble() / count.toDouble()
                    val freq = startFreq + (endFreq - startFreq) * t
                    val env = 1.0 - t
                    phase += 2.0 * PI * freq / SAMPLE_RATE
                    val sample = sin(phase) * env * volume
                    buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
                }
                emitPcmBuffer(buffer)
            } catch (_: Throwable) {
                // Ignore audio hardware limitations in headless tests
            }
        }
    }

    private fun emitPcmBuffer(buffer: ShortArray) {
        val minBuf = AudioTrack.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(buffer.size * 2)

        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(minBuf)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        track.write(buffer, 0, buffer.size)
        track.play()
        val sleepMs = (buffer.size * 1000L) / SAMPLE_RATE + 40L
        Thread.sleep(sleepMs)
        track.stop()
        track.release()
    }
}
