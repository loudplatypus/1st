package com.example.ui.sound

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

object RomanticSounds {
    private const val SAMPLE_RATE = 22050

    val soundsList = listOf(
        "Celestial Chime",
        "Romantic Flutter",
        "Warm Heartbeat",
        "Sparkling Twinkle",
        "Lovable Purr"
    )

    fun playSound(soundName: String) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                when (soundName) {
                    "Romantic Flutter" -> {
                        // Fast rising warm arpeggio (C5 - E5 - G5 - C6)
                        playToneSequence(listOf(523.25, 659.25, 783.99, 1046.50), durationMs = 120)
                    }
                    "Warm Heartbeat" -> {
                        // Double thump heartbeat tones
                        playTone(110.0, durationMs = 150, amplitude = 0.8)
                        Thread.sleep(150)
                        playTone(110.0, durationMs = 180, amplitude = 0.8)
                    }
                    "Celestial Chime" -> {
                        // Ringing cathedral-like soft bell with decay
                        playToneWithDecay(880.0, durationMs = 600)
                    }
                    "Sparkling Twinkle" -> {
                        // Twinkling bright chime pairing
                        playTone(987.77, durationMs = 80)
                        playTone(1174.66, durationMs = 80)
                        playTone(1318.51, durationMs = 150)
                    }
                    "Lovable Purr" -> {
                        // Sweet playful purring chirp
                        playTone(329.63, durationMs = 90)
                        playTone(392.00, durationMs = 90)
                        playTone(440.00, durationMs = 120)
                    }
                    else -> {
                        playToneWithDecay(660.0, durationMs = 300)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun playToneSequence(frequencies: List<Double>, durationMs: Int) {
        for (freq in frequencies) {
            playTone(freq, durationMs)
        }
    }

    private fun playTone(frequency: Double, durationMs: Int, amplitude: Double = 0.5) {
        val numSamples = durationMs * SAMPLE_RATE / 1000
        if (numSamples <= 0) return
        val sample = DoubleArray(numSamples)
        val generatedSnd = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            sample[i] = sin(2.0 * Math.PI * i / (SAMPLE_RATE / frequency))
        }

        for (i in 0 until numSamples) {
            generatedSnd[i] = (sample[i] * 32767 * amplitude).toInt().toShort()
        }

        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
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
            .setBufferSizeInBytes(generatedSnd.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        audioTrack.write(generatedSnd, 0, generatedSnd.size)
        audioTrack.play()
        Thread.sleep(durationMs.toLong() + 30)
        audioTrack.release()
    }

    private fun playToneWithDecay(frequency: Double, durationMs: Int) {
        val numSamples = durationMs * SAMPLE_RATE / 1000
        if (numSamples <= 0) return
        val sample = DoubleArray(numSamples)
        val generatedSnd = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / numSamples
            val decay = (1.0 - t) * (1.0 - t) // Quadratic decay for smoother tail
            sample[i] = sin(2.0 * Math.PI * i / (SAMPLE_RATE / frequency)) * decay
        }

        for (i in 0 until numSamples) {
            generatedSnd[i] = (sample[i] * 32767 * 0.5).toInt().toShort()
        }

        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
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
            .setBufferSizeInBytes(generatedSnd.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        audioTrack.write(generatedSnd, 0, generatedSnd.size)
        audioTrack.play()
        Thread.sleep(durationMs.toLong() + 30)
        audioTrack.release()
    }
}
