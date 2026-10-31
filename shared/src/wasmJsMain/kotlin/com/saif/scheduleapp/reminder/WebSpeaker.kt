package com.saif.scheduleapp.reminder

import com.saif.scheduleapp.domain.platform.Speaker

/**
 * Web no-op implementation of [Speaker].
 * Logs when speech is requested.
 */
class WebSpeaker : Speaker {

    override fun speak(text: String) {
        println("[WebSpeaker] Speech requested: '$text'")
    }
}

