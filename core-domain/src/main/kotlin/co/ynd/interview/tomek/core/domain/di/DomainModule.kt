package co.ynd.interview.tomek.core.domain.di

import co.ynd.interview.tomek.core.domain.usecase.DeleteVideoEntryUseCase
import co.ynd.interview.tomek.core.domain.usecase.GetVideoEntriesUseCase
import co.ynd.interview.tomek.core.domain.usecase.GetVideoEntryByIdUseCase
import co.ynd.interview.tomek.core.domain.usecase.SaveVideoEntryUseCase
import org.koin.dsl.module

val domainModule = module {
    factory { GetVideoEntriesUseCase(get()) }
    factory { SaveVideoEntryUseCase(get()) }
    factory { DeleteVideoEntryUseCase(get()) }
    factory { GetVideoEntryByIdUseCase(get()) }
}
