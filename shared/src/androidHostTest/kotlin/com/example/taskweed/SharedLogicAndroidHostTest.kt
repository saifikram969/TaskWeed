package com.example.taskweed

import android.content.Context
import com.saif.scheduleapp.core.di.platformModule
import com.saif.scheduleapp.data.local.DatabaseDriverFactory
import com.saif.scheduleapp.domain.repository.ScheduleRepository
import org.koin.core.error.InstanceCreationException
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SharedLogicAndroidHostTest {

    @BeforeTest
    fun setUp() {
        org.koin.core.context.stopKoin()
    }

    @AfterTest
    fun tearDown() {
        org.koin.core.context.stopKoin()
    }

    @Test
    fun testPlatformModuleWithoutContextFailsWithClearError() {
        val koinApp = koinApplication {
            modules(platformModule)
        }
        try {
            val exception = assertFailsWith<InstanceCreationException> {
                koinApp.koin.get<DatabaseDriverFactory>()
            }

            // Check root cause message
            var cause: Throwable? = exception
            var foundMessage = false
            while (cause != null) {
                if (cause.message?.contains("Android Context must be provided in Koin") == true) {
                    foundMessage = true
                    break
                }
                cause = cause.cause
            }
            assertTrue(foundMessage, "Expected clear error message about missing Android Context")
        } finally {
            koinApp.close()
        }
    }

    @Test
    fun testPlatformModuleWithContextRegistersDatabaseDriverFactory() {
        val dummyContext = android.content.ContextWrapper(null)

        val koinApp = koinApplication {
            modules(
                platformModule,
                module {
                    single<Context> { dummyContext }
                }
            )
        }
        try {
            val factory = koinApp.koin.get<DatabaseDriverFactory>()
            assertNotNull(factory)
        } finally {
            koinApp.close()
        }
    }
}