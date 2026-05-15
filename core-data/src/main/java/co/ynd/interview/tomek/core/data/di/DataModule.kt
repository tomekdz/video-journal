package co.ynd.interview.tomek.core.data.di

import co.ynd.interview.tomek.core.data.DefaultVideoEntryRepository
import co.ynd.interview.tomek.core.domain.repository.VideoEntryRepository
import org.koin.dsl.module

val dataModule = module {
    single<VideoEntryRepository> { DefaultVideoEntryRepository(get()) }
}
