package com.example.camera

import android.content.Context
import android.media.ToneGenerator
import android.media.AudioManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HandsFreeShutterManager(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val _isCountdownActive = MutableStateFlow(false)
    val isCountdownActive: StateFlow<Boolean> = _isCountdownActive.asStateFlow()

    private val _countdownSecRemaining = MutableStateFlow(3)
    val countdownSecRemaining: StateFlow<Int> = _countdownSecRemaining.asStateFlow()

    private val _triggerStatusMessage = MutableStateFlow<String?>(null)
    val triggerStatusMessage: StateFlow<String?> = _triggerStatusMessage.asStateFlow()

    private var countdownJob: Job? = null
    private var toneGenerator: ToneGenerator? = try {
        ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80)
    } catch (_: Exception) {
        null
    }

    /**
     * Triggers the 3-second hands-free countdown with audible beeps and executes onComplete.
     */
    fun startHandsFreeCountdown(triggerReason: String = "✋ Palm Detected", onComplete: () -> Unit) {
        if (_isCountdownActive.value) return

        countdownJob?.cancel()
        countdownJob = scope.launch(Dispatchers.Main) {
            _isCountdownActive.value = true
            _triggerStatusMessage.value = triggerReason

            for (sec in 3 downTo 1) {
                _countdownSecRemaining.value = sec
                playBeep(isFinal = false)
                delay(1000)
            }

            playBeep(isFinal = true)
            _isCountdownActive.value = false
            _triggerStatusMessage.value = null
            onComplete()
        }
    }

    fun cancelCountdown() {
        countdownJob?.cancel()
        _isCountdownActive.value = false
        _triggerStatusMessage.value = null
    }

    private fun playBeep(isFinal: Boolean) {
        try {
            if (isFinal) {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 180)
            } else {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 80)
            }
        } catch (_: Exception) {}
    }

    fun release() {
        countdownJob?.cancel()
        try {
            toneGenerator?.release()
        } catch (_: Exception) {}
    }
}
