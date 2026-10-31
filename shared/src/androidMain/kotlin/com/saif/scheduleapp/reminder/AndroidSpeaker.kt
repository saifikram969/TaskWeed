package com.saif.scheduleapp.reminder

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.saif.scheduleapp.domain.platform.Speaker
import java.util.Locale

class AndroidSpeaker(
    private val context: Context,
    private val onSpeechCompleted: (() -> Unit)? = null
) : Speaker {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private val pendingQueue = mutableListOf<String>()

    @Synchronized
    private fun initTtsIfNeeded(onReady: (TextToSpeech) -> Unit) {
        if (isInitialized && tts != null) {
            onReady(tts!!)
            return
        }

        if (tts == null) {
            tts = TextToSpeech(context.applicationContext) { status ->
                mainHandler.post {
                    if (status == TextToSpeech.SUCCESS) {
                        isInitialized = true
                        try {
                            val result = tts?.setLanguage(Locale.getDefault())
                            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                                tts?.setLanguage(Locale.US)
                            }
                        } catch (e: Exception) {
                            Log.w("AndroidSpeaker", "Failed to set TTS language: ${e.message}")
                        }

                        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                            override fun onStart(utteranceId: String?) {
                                Log.d("AndroidSpeaker", "TTS onStart: $utteranceId")
                            }

                            override fun onDone(utteranceId: String?) {
                                Log.d("AndroidSpeaker", "TTS onDone: $utteranceId")
                                mainHandler.post {
                                    onSpeechCompleted?.invoke()
                                }
                            }

                            @Deprecated("Deprecated in Java")
                            override fun onError(utteranceId: String?) {
                                Log.e("AndroidSpeaker", "TTS onError: $utteranceId")
                                mainHandler.post {
                                    onSpeechCompleted?.invoke()
                                }
                            }

                            override fun onError(utteranceId: String?, errorCode: Int) {
                                Log.e("AndroidSpeaker", "TTS onError: $utteranceId, code: $errorCode")
                                mainHandler.post {
                                    onSpeechCompleted?.invoke()
                                }
                            }
                        })

                        synchronized(this@AndroidSpeaker) {
                            pendingQueue.forEach { text ->
                                speakInternal(text)
                            }
                            pendingQueue.clear()
                        }
                    } else {
                        Log.e("AndroidSpeaker", "TextToSpeech init failed with status: $status")
                        onSpeechCompleted?.invoke()
                    }
                }
            }
        }
    }

    override fun speak(text: String) {
        if (text.isBlank()) {
            onSpeechCompleted?.invoke()
            return
        }

        synchronized(this) {
            if (!isInitialized || tts == null) {
                pendingQueue.add(text)
                initTtsIfNeeded { /* Pending queue will be drained */ }
            } else {
                speakInternal(text)
            }
        }
    }

    private fun speakInternal(text: String) {
        val utteranceId = "utterance_${System.currentTimeMillis()}"
        val result = tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        if (result != TextToSpeech.SUCCESS) {
            Log.e("AndroidSpeaker", "TTS speak failed with result code: $result")
            onSpeechCompleted?.invoke()
        }
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            Log.w("AndroidSpeaker", "Error shutting down TTS: ${e.message}")
        } finally {
            tts = null
            isInitialized = false
            pendingQueue.clear()
        }
    }
}

