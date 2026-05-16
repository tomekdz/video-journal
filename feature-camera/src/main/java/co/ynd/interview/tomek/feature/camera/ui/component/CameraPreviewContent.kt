package co.ynd.interview.tomek.feature.camera.ui.component

import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import co.ynd.interview.tomek.core.ui.theme.RecordRed
import co.ynd.interview.tomek.core.ui.util.formatDuration
import co.ynd.interview.tomek.feature.camera.util.VideoFileManager
import kotlinx.coroutines.delay

@Composable
internal fun CameraPreviewContent(
    isRecording: Boolean,
    videoFileManager: VideoFileManager,
    onRecordingStarted: () -> Unit,
    onRecordingStopped: (filePath: String, durationMs: Long, thumbnailPath: String?) -> Unit,
    onRecordingFailed: (Throwable) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var recording by remember { mutableStateOf<Recording?>(null) }
    var recordingDurationMs by remember { mutableLongStateOf(0L) }
    var videoCapture by remember { mutableStateOf<VideoCapture<Recorder>?>(null) }
    var lensFacing by remember { mutableStateOf(CameraSelector.DEFAULT_BACK_CAMERA) }

    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingDurationMs = 0L
            while (true) {
                delay(1000)
                recordingDurationMs += 1000
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        key(lensFacing) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    }
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.surfaceProvider = previewView.surfaceProvider
                        }

                        val recorder = Recorder.Builder()
                            .setQualitySelector(QualitySelector.from(Quality.HD))
                            .build()
                        val capture = VideoCapture.withOutput(recorder)
                        videoCapture = capture

                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            lensFacing,
                            preview,
                            capture
                        )
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        if (isRecording) {
            Text(
                text = formatDuration(recordingDurationMs),
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 32.dp)
            )
        }

        IconButton(
            onClick = {
                lensFacing = if (lensFacing == CameraSelector.DEFAULT_BACK_CAMERA) {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                } else {
                    CameraSelector.DEFAULT_BACK_CAMERA
                }
            },
            enabled = !isRecording,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.FlipCameraAndroid,
                contentDescription = "Flip camera",
                tint = Color.White
            )
        }

        val infiniteTransition = rememberInfiniteTransition(label = "record_pulse")
        val scale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = if (isRecording) 1.2f else 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(600),
                repeatMode = RepeatMode.Reverse
            ),
            label = "record_scale"
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .size(72.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(if (isRecording) Color.DarkGray else RecordRed)
                .clickable {
                    if (isRecording) {
                        recording?.stop()
                        recording = null
                    } else {
                        val capture = videoCapture ?: return@clickable
                        val file = videoFileManager.createVideoFile()
                        val outputOptions = FileOutputOptions.Builder(file).build()

                        @Suppress("MissingPermission")
                        recording = capture.output
                            .prepareRecording(context, outputOptions)
                            .withAudioEnabled()
                            .start(ContextCompat.getMainExecutor(context)) { event ->
                                when (event) {
                                    is VideoRecordEvent.Start -> onRecordingStarted()
                                    is VideoRecordEvent.Finalize -> {
                                        if (event.hasError()) {
                                            onRecordingFailed(
                                                RuntimeException("Recording failed: ${event.error}")
                                            )
                                        } else {
                                            val durationMs =
                                                event.recordingStats.recordedDurationNanos / 1_000_000
                                            val thumbFile = videoFileManager.extractThumbnail(file)
                                            onRecordingStopped(
                                                file.absolutePath,
                                                durationMs,
                                                thumbFile?.absolutePath
                                            )
                                        }
                                    }
                                }
                            }
                    }
                }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            recording?.stop()
        }
    }
}
