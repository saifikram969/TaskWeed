package com.saif.scheduleapp.data.local

import android.content.Context
import android.util.Log
import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.saif.scheduleapp.db.ScheduleDatabase

actual class DatabaseDriverFactory(private val context: Context? = null) {
    actual fun create(): SqlDriver {
        val ctx = checkNotNull(context) { "Android Context must be provided to create AndroidSqliteDriver" }
        return AndroidSqliteDriver(
            schema = ScheduleDatabase.Schema,
            context = ctx,
            name = "schedule.db",
            callback = object : AndroidSqliteDriver.Callback(ScheduleDatabase.Schema) {
                override fun onUpgrade(
                    db: SupportSQLiteDatabase,
                    oldVersion: Int,
                    newVersion: Int
                ) {
                    try {
                        super.onUpgrade(db, oldVersion, newVersion)
                    } catch (e: Exception) {
                        Log.w("DatabaseDriverFactory", "Migration from $oldVersion to $newVersion had issue: ${e.message}. Ensuring tables and columns exist safely.")
                        db.execSQL("CREATE TABLE IF NOT EXISTS scheduleTask (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT NOT NULL, details TEXT NOT NULL, startMinutes INTEGER NOT NULL, endMinutes INTEGER NOT NULL, category TEXT NOT NULL, isDone INTEGER NOT NULL DEFAULT 0, repeatDays INTEGER NOT NULL DEFAULT 127, voiceEnabled INTEGER NOT NULL DEFAULT 1)")
                        db.execSQL("CREATE TABLE IF NOT EXISTS taskCompletion (taskId INTEGER NOT NULL, date TEXT NOT NULL, PRIMARY KEY (taskId, date))")
                        try {
                            db.execSQL("ALTER TABLE scheduleTask ADD COLUMN repeatDays INTEGER NOT NULL DEFAULT 127")
                        } catch (_: Exception) {}
                        try {
                            db.execSQL("ALTER TABLE scheduleTask ADD COLUMN voiceEnabled INTEGER NOT NULL DEFAULT 1")
                        } catch (_: Exception) {}
                    }
                }
            }
        )
    }
}
