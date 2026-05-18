package co.ynd.interview.tomek.feature.camera.recorder

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class CameraXVideoRecorder(private val context: Context) : VideoRecorder {

    private val _surfaceRequest = MutableStateFlow<SurfaceRequest?>(null)
    override val surfaceRequest: StateFlow<SurfaceRequest?> = _surfaceRequest.asStateFlow()

    private var videoCapture: VideoCapture<Recorder>? = null
    private var currentRecording: Recording? = null

    override suspend fun bind(lifecycleOwner: LifecycleOwner, lensFacing: CameraSelector) {
        val cameraProvider = ProcessCameraProvider.awaitInstance(context)
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider { request -> _surfaceRequest.value = request }
        }
        val recorder = Recorder.Builder()
            .setQualitySelector(QualitySelector.from(Quality.HD))
            .build()
        val capture = VideoCapture.withOutput(recorder)
        videoCapture = capture

        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(lifecycleOwner, lensFacing, preview, capture)
    }

    override fun startRecording(file: File, onEvent: (RecordingEvent) -> Unit) {
        val capture = videoCapture ?: return
        val outputOptions = FileOutputOptions.Builder(file).build()

        @Suppress("MissingPermission")
        currentRecording = capture.output
            .prepareRecording(context, outputOptions)
            .withAudioEnabled()
            .start(ContextCompat.getMainExecutor(context)) { event ->
                when (event) {
                    is VideoRecordEvent.Start ->
                        onEvent(RecordingEvent.Started)
                    is VideoRecordEvent.Finalize ->
                        if (event.hasError()) onEvent(RecordingEvent.Failed(errorMessage(event.error)))
                        else onEvent(RecordingEvent.Finalized(event.recordingStats.recordedDurationNanos / 1_000_000))
                    else -> {}
                }
            }
    }

    override fun stopRecording() {
        currentRecording?.stop()
        currentRecording = null
    }

    private fun errorMessage(code: Int): String = when (code) {
        VideoRecordEvent.Finalize.ERROR_ENCODING_FAILED -> "Encoding failed"
        VideoRecordEvent.Finalize.ERROR_FILE_SIZE_LIMIT_REACHED -> "File size limit reached"
        VideoRecordEvent.Finalize.ERROR_INSUFFICIENT_STORAGE -> "Insufficient storage"
        VideoRecordEvent.Finalize.ERROR_NO_VALID_DATA -> "No valid data recorded"
        VideoRecordEvent.Finalize.ERROR_RECORDER_ERROR -> "Recorder error"
        VideoRecordEvent.Finalize.ERROR_SOURCE_INACTIVE -> "Camera source became inactive"
        VideoRecordEvent.Finalize.ERROR_INVALID_OUTPUT_OPTIONS -> "Invalid output options"
        VideoRecordEvent.Finalize.ERROR_DURATION_LIMIT_REACHED -> "Duration limit reached"
        else -> "Recording failed (code $code)"
    }
}
