package com.saif.scheduleapp.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.saif.scheduleapp.db.ScheduleDatabase

actual class DatabaseDriverFactory {
    actual fun create(): SqlDriver {
        return NativeSqliteDriver(
            schema = ScheduleDatabase.Schema,
            name = "schedule.db"
        )
    }
}
