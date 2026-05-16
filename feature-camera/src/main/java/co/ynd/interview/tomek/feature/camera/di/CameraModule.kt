package co.ynd.interview.tomek.feature.camera.di

import co.ynd.interview.tomek.feature.camera.ui.CameraViewModel
import co.ynd.interview.tomek.feature.camera.util.VideoFileManager
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val cameraModule = module {
    viewModel { CameraViewModel(get()) }
    factory { VideoFileManager(androidContext()) }
}
