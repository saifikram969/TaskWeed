package com.saif.scheduleapp.reminder

import com.saif.scheduleapp.domain.platform.Speaker
import platform.AVFAudio.AVSpeechSynthesizer
import platform.AVFAudio.AVSpeechSynthesisVoice
import platform.AVFAudio.AVSpeechUtterance
import platform.AVFAudio.AVSpeechUtteranceDefaultSpeechRate

/**
 * iOS implementation of [Speaker] using Apple's [AVSpeechSynthesizer].
 *
 * NOTE ON iOS BACKGROUND AUDIO / SPEECH:
 * Speech synthesis is supported when the application is running in the foreground (open).
 * iOS strictly forbids background speech synthesis or arbitrary code execution for standard
 * user notification triggers when the application is suspended or terminated.
 */
class IosSpeaker : Speaker {

    private val synthesizer by lazy { AVSpeechSynthesizer() }

    override fun speak(text: String) {
        if (text.isBlank()) return
        try {
            val utterance = AVSpeechUtterance.speechUtteranceWithString(text).apply {
                voice = AVSpeechSynthesisVoice.voiceWithLanguage("en-US")
                rate = AVSpeechUtteranceDefaultSpeechRate
            }
            synthesizer.speakUtterance(utterance)
        } catch (e: Exception) {
            println("IosSpeaker error: ${e.message}")
        }
    }
}
