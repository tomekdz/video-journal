package co.ynd.interview.tomek.core.data.di

import android.content.Context
import co.ynd.interview.tomek.core.data.DefaultVideoEntryRepository
import co.ynd.interview.tomek.core.data.FileSystemVideoFileCleaner
import co.ynd.interview.tomek.core.data.OrphanFileCleaner
import co.ynd.interview.tomek.core.domain.VideoFileCleaner
import co.ynd.interview.tomek.core.domain.repository.VideoEntryRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import java.io.File

val dataModule = module {
    single<VideoEntryRepository> { DefaultVideoEntryRepository(get()) }
    single<VideoFileCleaner> { FileSystemVideoFileCleaner() }
    single {
        val context: Context = androidContext()
        OrphanFileCleaner(
            repository = get(),
            videosDir = File(context.filesDir, "videos"),
            thumbnailsDir = File(context.filesDir, "thumbnails"),
        )
    }
}
