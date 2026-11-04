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

class TimeChangedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_TIMEZONE_CHANGED ||
            action == Intent.ACTION_TIME_CHANGED ||
            action == Intent.ACTION_DATE_CHANGED ||
            action == "android.intent.action.TIME_SET"
        ) {
            Log.d("TimeChangedReceiver", "Received action $action. Rescheduling all active task reminders.")

            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.Default).launch {
                try {
                    val koin = KoinPlatform.getKoinOrNull()
                    val rescheduleAll = koin?.getOrNull<RescheduleAllReminders>()
                    if (rescheduleAll != null) {
                        rescheduleAll()
                        Log.d("TimeChangedReceiver", "Successfully rescheduled all reminders after time change.")
                    } else {
                        Log.w("TimeChangedReceiver", "RescheduleAllReminders use case could not be resolved from Koin.")
                    }
                } catch (e: Exception) {
                    Log.e("TimeChangedReceiver", "Failed to reschedule reminders on time change: ${e.message}", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}

