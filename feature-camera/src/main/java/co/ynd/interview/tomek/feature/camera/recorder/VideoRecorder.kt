package co.ynd.interview.tomek.feature.camera.recorder

import androidx.camera.core.CameraSelector
import androidx.camera.core.SurfaceRequest
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.StateFlow
import java.io.File

interface VideoRecorder {
    val surfaceRequest: StateFlow<SurfaceRequest?>
    suspend fun bind(lifecycleOwner: LifecycleOwner, lensFacing: CameraSelector)
    fun startRecording(file: File, onEvent: (RecordingEvent) -> Unit)
    fun stopRecording()
}
