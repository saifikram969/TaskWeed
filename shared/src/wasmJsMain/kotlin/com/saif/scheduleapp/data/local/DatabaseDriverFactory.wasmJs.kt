package com.saif.scheduleapp.data.local

import app.cash.sqldelight.db.SqlDriver

actual class DatabaseDriverFactory {
    actual fun create(): SqlDriver {
        throw UnsupportedOperationException("Web platform uses InMemoryScheduleRepository")
    }
}
