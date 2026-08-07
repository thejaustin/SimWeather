package com.thejaustin.simweather

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

/**
 * Synthesizes retro 8-bit SimCity 3000 style sound effects using AudioTrack.
 * Operates without external asset dependencies and respects sound settings.
 */
class SoundManager private constructor(context: Context) {
    private val settings = SettingsPreferences.getInstance(context)
    private val scope = CoroutineScope(Dispatchers.Default)

    fun playClick() {
        if (!settings.soundEnabled) return
        scope.launch {
            generateTone(freq = 880.0, durationMs = 35, volume = 0.4f)
        }
    }

    fun playSplineReticulate() {
        if (!settings.soundEnabled) return
        scope.launch {
            generateSweep(startFreq = 400.0, endFreq = 1200.0, durationMs = 120, volume = 0.3f)
        }
    }

    fun playAlert() {
        if (!settings.soundEnabled) return
        scope.launch {
            generateTone(freq = 750.0, durationMs = 80, volume = 0.5f)
            generateTone(freq = 550.0, durationMs = 80, volume = 0.5f)
        }
    }

    fun playSpeedChange() {
        if (!settings.soundEnabled) return
        scope.launch {
            generateSweep(startFreq = 600.0, endFreq = 1400.0, durationMs = 60, volume = 0.35f)
        }
    }

    fun playCashRegister() {
        if (!settings.soundEnabled) return
        scope.launch {
            generateTone(freq = 1200.0, durationMs = 40, volume = 0.5f)
            generateTone(freq = 1800.0, durationMs = 90, volume = 0.6f)
        }
    }

    fun playSiren() {
        if (!settings.soundEnabled) return
        scope.launch {
            repeat(3) {
                generateSweep(startFreq = 500.0, endFreq = 950.0, durationMs = 150, volume = 0.5f)
                generateSweep(startFreq = 950.0, endFreq = 500.0, durationMs = 150, volume = 0.5f)
            }
        }
    }

    private fun generateTone(
        freq: Double,
        durationMs: Int,
        volume: Float,
    ) {
        try {
            val sampleRate = 22050
            val numSamples = (durationMs * sampleRate / 1000)
            val buffer = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val angle = 2.0 * Math.PI * i / (sampleRate / freq)
                val sine = sin(angle)
                val sampleValue = (sine * Short.MAX_VALUE * volume).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                buffer[i] = sampleValue.toShort()
            }

            val track =
                AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build(),
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build(),
                    )
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

            track.write(buffer, 0, buffer.size)
            track.play()
            Thread.sleep(durationMs.toLong())
            track.release()
        } catch (_: Exception) {
        }
    }

    private fun generateSweep(
        startFreq: Double,
        endFreq: Double,
        durationMs: Int,
        volume: Float,
    ) {
        try {
            val sampleRate = 22050
            val numSamples = (durationMs * sampleRate / 1000)
            val buffer = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val progress = i.toDouble() / numSamples
                val currentFreq = startFreq + (endFreq - startFreq) * progress
                val angle = 2.0 * Math.PI * i / (sampleRate / currentFreq)
                val sampleValue = (sin(angle) * Short.MAX_VALUE * volume).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                buffer[i] = sampleValue.toShort()
            }

            val track =
                AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build(),
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build(),
                    )
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

            track.write(buffer, 0, buffer.size)
            track.play()
            Thread.sleep(durationMs.toLong())
            track.release()
        } catch (_: Exception) {
        }
    }

    companion object {
        @Volatile
        private var instance: SoundManager? = null

        fun getInstance(context: Context): SoundManager {
            return instance ?: synchronized(this) {
                instance ?: SoundManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
