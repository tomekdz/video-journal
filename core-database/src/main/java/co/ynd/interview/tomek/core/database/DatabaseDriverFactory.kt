package co.ynd.interview.tomek.core.database

import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import app.cash.sqldelight.db.SqlDriver

fun createDriver(context: Context): SqlDriver {
    return AndroidSqliteDriver(
        schema = VideoJournalDatabase.Schema,
        context = context,
        name = "video_journal.db"
    )
}
