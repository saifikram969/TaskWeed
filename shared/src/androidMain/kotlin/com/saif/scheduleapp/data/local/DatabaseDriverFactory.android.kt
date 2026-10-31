package com.saif.scheduleapp.data.local

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.saif.scheduleapp.db.ScheduleDatabase

actual class DatabaseDriverFactory(private val context: Context? = null) {
    actual fun create(): SqlDriver {
        val ctx = checkNotNull(context) { "Android Context must be provided to create AndroidSqliteDriver" }
        return AndroidSqliteDriver(
            schema = ScheduleDatabase.Schema,
            context = ctx,
            name = "schedule.db"
        )
    }
}
