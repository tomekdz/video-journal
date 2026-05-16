package co.ynd.interview.tomek.feature.feed.di

import co.ynd.interview.tomek.feature.feed.ui.FeedViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val feedModule = module {
    viewModel { FeedViewModel(get(), get()) }
}
