package co.ynd.interview.tomek.core.testing.di

import co.ynd.interview.tomek.core.data.FakeVideoEntryRepository
import co.ynd.interview.tomek.core.domain.VideoFileCleaner
import co.ynd.interview.tomek.core.domain.repository.VideoEntryRepository
import org.koin.dsl.module

val testDataModule = module {
    single<VideoEntryRepository> { FakeVideoEntryRepository() }
    single<VideoFileCleaner> { object : VideoFileCleaner {
        override suspend fun delete(filePath: String, thumbnailPath: String?) {}
    } }
}
