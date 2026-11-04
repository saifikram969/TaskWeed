package com.example.taskweed

import androidx.compose.runtime.Composable
import com.saif.scheduleapp.core.designsystem.ScheduleTheme
import com.saif.scheduleapp.core.di.appModule
import com.saif.scheduleapp.core.di.platformModule
import com.saif.scheduleapp.presentation.navigation.AppNavHost
import org.koin.compose.KoinApplication
import org.koin.compose.KoinContext
import org.koin.mp.KoinPlatform

@Composable
fun App() {
    if (KoinPlatform.getKoinOrNull() == null) {
        KoinApplication(application = {
            modules(appModule, platformModule)
        }) {
            ScheduleTheme {
                AppNavHost()
            }
        }
    } else {
        KoinContext {
            ScheduleTheme {
                AppNavHost()
            }
        }
    }
}


