package co.ynd.interview.tomek.core.testing.di

import co.ynd.interview.tomek.core.data.FakeVideoEntryRepository
import co.ynd.interview.tomek.core.domain.repository.VideoEntryRepository
import co.ynd.interview.tomek.core.domain.usecase.DeleteVideoEntryUseCase
import co.ynd.interview.tomek.core.domain.usecase.GetVideoEntriesUseCase
import co.ynd.interview.tomek.core.domain.usecase.GetVideoEntryByIdUseCase
import co.ynd.interview.tomek.core.domain.usecase.SaveVideoEntryUseCase
import org.koin.dsl.module

val testDataModule = module {
    single<VideoEntryRepository> { FakeVideoEntryRepository() }
    factory { GetVideoEntriesUseCase(get()) }
    factory { SaveVideoEntryUseCase(get()) }
    factory { DeleteVideoEntryUseCase(get()) }
    factory { GetVideoEntryByIdUseCase(get()) }
}
