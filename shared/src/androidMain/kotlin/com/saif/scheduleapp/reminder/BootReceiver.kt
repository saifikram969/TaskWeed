package com.saif.scheduleapp.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.saif.scheduleapp.domain.usecase.RescheduleAllReminders
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatform

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            Log.d("BootReceiver", "Received $action. Rescheduling all active task reminders.")

            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.Default).launch {
                try {
                    val koin = KoinPlatform.getKoinOrNull()
                    val rescheduleAll = koin?.getOrNull<RescheduleAllReminders>()
                    if (rescheduleAll != null) {
                        rescheduleAll()
                        Log.d("BootReceiver", "Successfully rescheduled all reminders after reboot/update.")
                    } else {
                        Log.w("BootReceiver", "RescheduleAllReminders use case could not be resolved from Koin.")
                    }
                } catch (e: Exception) {
                    Log.e("BootReceiver", "Failed to reschedule reminders on boot: ${e.message}", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}

