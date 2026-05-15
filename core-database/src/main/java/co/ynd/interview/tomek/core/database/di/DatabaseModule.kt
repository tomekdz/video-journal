package co.ynd.interview.tomek.core.database.di

import co.ynd.interview.tomek.core.database.VideoJournalDatabase
import co.ynd.interview.tomek.core.database.createDriver
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single { createDriver(androidContext()) }
    single { VideoJournalDatabase(get()) }
    single { get<VideoJournalDatabase>().videoEntryQueries }
}
