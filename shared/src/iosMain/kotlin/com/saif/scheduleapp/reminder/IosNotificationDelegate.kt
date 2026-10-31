package com.saif.scheduleapp.reminder

import com.saif.scheduleapp.domain.platform.Speaker
import platform.UserNotifications.UNNotification
import platform.UserNotifications.UNNotificationPresentationOptionBadge
import platform.UserNotifications.UNNotificationPresentationOptionBanner
import platform.UserNotifications.UNNotificationPresentationOptionSound
import platform.UserNotifications.UNNotificationPresentationOptions
import platform.UserNotifications.UNNotificationResponse
import platform.UserNotifications.UNUserNotificationCenter
import platform.UserNotifications.UNUserNotificationCenterDelegateProtocol
import platform.darwin.NSObject

/**
 * UNUserNotificationCenterDelegate implementation for foreground notifications and voice speech.
 *
 * IMPORTANT iOS LIFECYCLE LIMITATION:
 * iOS does NOT allow background execution of arbitrary code or text-to-speech (AVSpeechSynthesizer)
 * when the application is closed or suspended. Therefore, when the app is closed, reminders will
 * display the system notification banner and play the default notification sound only.
 * Speech synthesis (Speaker.speak) is ONLY supported when the app is open / foregrounded, as handled here.
 */
class IosNotificationDelegate(
    private val speaker: Speaker
) : NSObject(), UNUserNotificationCenterDelegateProtocol {

    override fun userNotificationCenter(
        center: UNUserNotificationCenter,
        willPresentNotification: UNNotification,
        withCompletionHandler: (UNNotificationPresentationOptions) -> Unit
    ) {
        val content = willPresentNotification.request.content
        val title = content.title
        val body = content.body

        // Synthesize and speak the task reminder since the app is open
        val speechText = if (body.isNotBlank()) {
            "Time for $title. $body"
        } else {
            "Time for $title"
        }
        speaker.speak(speechText)

        // Show a notification banner, play sound, and update badge while the app is open
        val options: UNNotificationPresentationOptions =
            UNNotificationPresentationOptionBanner or
            UNNotificationPresentationOptionSound or
            UNNotificationPresentationOptionBadge

        withCompletionHandler(options)
    }

    override fun userNotificationCenter(
        center: UNUserNotificationCenter,
        didReceiveNotificationResponse: UNNotificationResponse,
        withCompletionHandler: () -> Unit
    ) {
        withCompletionHandler()
    }
}

