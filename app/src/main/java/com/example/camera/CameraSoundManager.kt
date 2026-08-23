package com.example.camera

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.data.model.ShutterSoundProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

class CameraSoundManager(private val context: Context, private val scope: CoroutineScope) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    /**
     * Plays authentic shutter sound & synchronized tactile mechanical vibration based on selected profile.
     */
    fun playShutter(profile: ShutterSoundProfile, isSoundEnabled: Boolean = true, isHapticsEnabled: Boolean = true) {
        if (profile == ShutterSoundProfile.SILENT) {
            if (isHapticsEnabled) triggerHapticClick()
            return
        }

        if (isHapticsEnabled) {
            triggerMechanicalHaptics(profile)
        }

        if (isSoundEnabled) {
            scope.launch(Dispatchers.Default) {
                when (profile) {
                    ShutterSoundProfile.LEICA_M -> playLeicaM3Sound()
                    ShutterSoundProfile.VINTAGE_SLR -> playVintageSlrCrankSound()
                    ShutterSoundProfile.POLAROID_MOTOR -> playPolaroidMotorSound()
                    ShutterSoundProfile.SILENT -> {}
                }
            }
        }
    }

    private fun triggerHapticClick() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(25, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(25)
            }
        } catch (_: Exception) {}
    }

    private fun triggerMechanicalHaptics(profile: ShutterSoundProfile) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                when (profile) {
                    ShutterSoundProfile.LEICA_M -> {
                        val timings = longArrayOf(0, 18, 40, 12)
                        val amplitudes = intArrayOf(0, 220, 0, 140)
                        vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
                    }
                    ShutterSoundProfile.VINTAGE_SLR -> {
                        val timings = longArrayOf(0, 35, 45, 25, 20, 20, 20, 20)
                        val amplitudes = intArrayOf(0, 255, 0, 180, 0, 120, 0, 90)
                        vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
                    }
                    ShutterSoundProfile.POLAROID_MOTOR -> {
                        val timings = longArrayOf(0, 20, 10, 80)
                        val amplitudes = intArrayOf(0, 200, 0, 90)
                        vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
                    }
                    ShutterSoundProfile.SILENT -> triggerHapticClick()
                }
            } else {
                triggerHapticClick()
            }
        } catch (_: Exception) {}
    }

    private fun playLeicaM3Sound() {
        val sampleRate = 44100
        val durationMs = 140
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val envelope = when {
                t < 0.015 -> (t / 0.015)
                t < 0.050 -> 1.0 - (t - 0.015) / 0.035
                t < 0.065 -> 0.0
                t < 0.080 -> (t - 0.065) / 0.015 * 0.6
                else -> 0.6 * (1.0 - (t - 0.080) / 0.060)
            }
            val clickFreq = 1850.0 + (if (t < 0.05) -800.0 * (t / 0.05) else 300.0)
            val wave = sin(2.0 * Math.PI * clickFreq * t) * envelope * 24000.0
            buffer[i] = wave.toInt().toShort()
        }
        playAudioBuffer(buffer, sampleRate)
    }

    private fun playVintageSlrCrankSound() {
        val sampleRate = 44100
        val durationMs = 280
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            var wave = 0.0
            if (t < 0.045) {
                // Mirror slap & shutter click
                val env = 1.0 - (t / 0.045)
                wave = (sin(2.0 * Math.PI * 920.0 * t) * 0.7 + sin(2.0 * Math.PI * 280.0 * t) * 0.8) * env * 28000.0
            } else if (t > 0.080 && t < 0.240) {
                // Film ratchet crank sound (chhk-chhk)
                val ratchetT = (t - 0.080)
                val tooth = ((ratchetT * 45.0) % 1.0)
                val env = if (tooth < 0.3) 1.0 else 0.05
                wave = (sin(2.0 * Math.PI * 3200.0 * t) * 0.5 + sin(2.0 * Math.PI * 1400.0 * t) * 0.5) * env * 14000.0
            }
            buffer[i] = wave.toInt().coerceIn(-32767, 32767).toShort()
        }
        playAudioBuffer(buffer, sampleRate)
    }

    private fun playPolaroidMotorSound() {
        val sampleRate = 44100
        val durationMs = 320
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val motorEnvelope = if (t < 0.03) t / 0.03 else (1.0 - (t - 0.03) / 0.29).coerceIn(0.0, 1.0)
            val motorWhine = sin(2.0 * Math.PI * (640.0 + sin(2.0 * Math.PI * 30.0 * t) * 80.0) * t) * 0.6
            val gearGrind = sin(2.0 * Math.PI * 2200.0 * t) * 0.3
            val wave = (motorWhine + gearGrind) * motorEnvelope * 22000.0
            buffer[i] = wave.toInt().coerceIn(-32767, 32767).toShort()
        }
        playAudioBuffer(buffer, sampleRate)
    }

    private fun playAudioBuffer(buffer: ShortArray, sampleRate: Int) {
        try {
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(buffer, 0, buffer.size)
            track.play()
            track.setNotificationMarkerPosition(buffer.size)
            track.setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
                override fun onMarkerReached(t: AudioTrack?) {
                    try {
                        t?.stop()
                        t?.release()
                    } catch (_: Exception) {}
                }
                override fun onPeriodicNotification(t: AudioTrack?) {}
            })
        } catch (_: Exception) {}
    }
}
