package co.ynd.interview.tomek.feature.camera.di

import co.ynd.interview.tomek.feature.camera.recorder.CameraXVideoRecorder
import co.ynd.interview.tomek.feature.camera.recorder.VideoRecorder
import co.ynd.interview.tomek.feature.camera.ui.CameraViewModel
import co.ynd.interview.tomek.feature.camera.util.VideoFileManager
import co.ynd.interview.tomek.feature.camera.util.VideoFileOperations
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val cameraModule = module {
    single<VideoRecorder> { CameraXVideoRecorder(androidContext()) }
    factory<VideoFileOperations> { VideoFileManager(androidContext()) }
    viewModel { CameraViewModel(get(), get(), get(), get(), get()) }
}
