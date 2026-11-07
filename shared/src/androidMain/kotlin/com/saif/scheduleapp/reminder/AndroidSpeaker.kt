package com.saif.scheduleapp.reminder

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.saif.scheduleapp.domain.platform.Speaker
import java.util.Locale

interface SpeechListener {
    fun onSpeechStarted()
    fun onSpeechFinished()
    fun onSpeechError(errorCode: Int? = null)
}

class AndroidSpeaker(
    private val context: Context,
    private var speechListener: SpeechListener? = null
) : Speaker {

    constructor(context: Context, onSpeechCompleted: (() -> Unit)?) : this(
        context = context,
        speechListener = if (onSpeechCompleted != null) {
            object : SpeechListener {
                override fun onSpeechStarted() {}
                override fun onSpeechFinished() { onSpeechCompleted() }
                override fun onSpeechError(errorCode: Int?) { onSpeechCompleted() }
            }
        } else null
    )

    private val mainHandler = Handler(Looper.getMainLooper())
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private val pendingQueue = mutableListOf<String>()

    val isSpeaking: Boolean
        get() = tts?.isSpeaking == true

    fun setSpeechListener(listener: SpeechListener?) {
        this.speechListener = listener
    }

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
                                mainHandler.post {
                                    speechListener?.onSpeechStarted()
                                }
                            }

                            override fun onDone(utteranceId: String?) {
                                Log.d("AndroidSpeaker", "TTS onDone: $utteranceId")
                                mainHandler.post {
                                    speechListener?.onSpeechFinished()
                                }
                            }

                            @Deprecated("Deprecated in Java")
                            override fun onError(utteranceId: String?) {
                                Log.e("AndroidSpeaker", "TTS onError: $utteranceId")
                                mainHandler.post {
                                    speechListener?.onSpeechError(null)
                                }
                            }

                            override fun onError(utteranceId: String?, errorCode: Int) {
                                Log.e("AndroidSpeaker", "TTS onError: $utteranceId, code: $errorCode")
                                mainHandler.post {
                                    speechListener?.onSpeechError(errorCode)
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
                        mainHandler.post {
                            speechListener?.onSpeechError(status)
                        }
                    }
                }
            }
        }
    }

    override fun speak(text: String) {
        if (text.isBlank()) {
            mainHandler.post {
                speechListener?.onSpeechFinished()
            }
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
            mainHandler.post {
                speechListener?.onSpeechError(result)
            }
        }
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (e: Exception) {
            Log.w("AndroidSpeaker", "Error stopping TTS: ${e.message}")
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
            speechListener = null
        }
    }
}

