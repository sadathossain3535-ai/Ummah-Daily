package com.example.alarm

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.sin

/**
 * Generates a calm, reverent multi-phrase Azan-inspired Takbir chime on the ALARM audio stream
 * so prayer alerts ring reliably even when the app is closed, without external MP3 dependencies.
 */
class AzanAudioPlayer {

    @Volatile
    private var audioTrack: AudioTrack? = null

    @Volatile
    private var stoppedManually: Boolean = false

    suspend fun playAzanChimeSequence() = withContext(Dispatchers.Default) {
        stoppedManually = false
        val sampleRate = 22050
        val notes = listOf(
            // Phrase 1: Allahu Akbar (D4 -> F4 -> G4 -> A4)
            293.66 to 420,
            349.23 to 420,
            392.00 to 480,
            440.00 to 900,
            0.0 to 260,
            // Phrase 2: Allahu Akbar (A4 -> G4 -> F4 -> G4 -> F4)
            440.00 to 420,
            392.00 to 420,
            349.23 to 480,
            392.00 to 400,
            349.23 to 950,
            0.0 to 320,
            // Phrase 3: Ashhadu / Hayya 'alas-Salah cadence
            293.66 to 380,
            349.23 to 460,
            440.00 to 620,
            392.00 to 520,
            349.23 to 900,
            0.0 to 300,
            // Closing Takbir
            392.00 to 420,
            440.00 to 700,
            349.23 to 500,
            293.66 to 1100
        )

        val pcmBuffer = synthesizeNotes(sampleRate, notes)
        val minBuf = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(sampleRate)

        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(sampleRate)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(minBuf)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        audioTrack = track
        try {
            track.play()
            // Play 2 calm cycles (~24 seconds total) unless stopped earlier
            repeat(2) {
                if (stoppedManually) return@withContext
                var offset = 0
                val chunkSize = 2048
                while (offset < pcmBuffer.size && !stoppedManually) {
                    currentCoroutineContext().ensureActive()
                    val count = minOf(chunkSize, pcmBuffer.size - offset)
                    val written = track.write(pcmBuffer, offset, count)
                    if (written <= 0) break
                    offset += written
                }
            }
        } catch (ce: CancellationException) {
            throw ce
        } catch (_: Exception) {
            // Audio device unavailable or interrupted
        } finally {
            releaseTrack(track)
        }
    }

    fun stop() {
        stoppedManually = true
        val track = audioTrack
        audioTrack = null
        releaseTrack(track)
    }

    private fun releaseTrack(track: AudioTrack?) {
        if (track == null) return
        runCatching {
            if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                track.pause()
                track.flush()
            }
            track.stop()
        }
        runCatching { track.release() }
    }

    private fun synthesizeNotes(sampleRate: Int, notes: List<Pair<Double, Int>>): ShortArray {
        val totalSamples = notes.sumOf { (sampleRate * it.second) / 1000 }
        val output = ShortArray(totalSamples)
        var cursor = 0
        val fadeSamples = (sampleRate * 0.035).toInt().coerceAtLeast(1)

        notes.forEach { (freqHz, durationMs) ->
            val count = (sampleRate * durationMs) / 1000
            if (freqHz <= 0.0) {
                cursor += count
            } else {
                for (i in 0 until count) {
                    val envIn = if (i < fadeSamples) i.toDouble() / fadeSamples else 1.0
                    val envOut = if (i > count - fadeSamples) (count - i).toDouble() / fadeSamples else 1.0
                    val envelope = minOf(envIn, envOut).coerceIn(0.0, 1.0)
                    val angle = 2.0 * PI * freqHz * i / sampleRate
                    // Warm fundamental + gentle 2nd harmonic for resonant chime timbre
                    val wave = 0.78 * sin(angle) + 0.22 * sin(2.0 * angle)
                    val sample = (wave * envelope * 15000.0).toInt().coerceIn(-32767, 32767).toShort()
                    if (cursor < output.size) {
                        output[cursor++] = sample
                    }
                }
            }
        }
        return output
    }
}
