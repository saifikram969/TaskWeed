package com.example.taskweed

import android.app.Application
import android.util.Log
import com.saif.scheduleapp.core.di.initKoin
import com.saif.scheduleapp.domain.usecase.RescheduleAllReminders
import com.saif.scheduleapp.reminder.NotificationChannels
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext

class TaskWeedApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@TaskWeedApplication)
        }

        // Create the notification channel at app startup
        NotificationChannels.createReminderChannel(this)

        // Reschedule all active reminders on app start
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val rescheduleAllReminders: RescheduleAllReminders = get()
                rescheduleAllReminders()
                Log.d("TaskWeedApplication", "Rescheduled all reminders on app start.")
            } catch (e: Exception) {
                Log.e("TaskWeedApplication", "Failed to reschedule reminders on app start: ${e.message}", e)
            }
        }
    }
}
