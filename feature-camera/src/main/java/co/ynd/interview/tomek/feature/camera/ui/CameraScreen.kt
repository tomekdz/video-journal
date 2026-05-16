package co.ynd.interview.tomek.feature.camera.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.ynd.interview.tomek.core.ui.component.ErrorMessage
import co.ynd.interview.tomek.feature.camera.ui.component.CameraPreviewContent
import co.ynd.interview.tomek.feature.camera.ui.component.ReviewContent
import co.ynd.interview.tomek.feature.camera.ui.permission.RequireCameraPermissions
import co.ynd.interview.tomek.feature.camera.util.VideoFileManager
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun CameraScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CameraViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val videoFileManager: VideoFileManager = koinInject()

    LaunchedEffect(uiState) {
        if (uiState is CameraUiState.Saved) {
            onNavigateBack()
        }
    }

    RequireCameraPermissions(
        onGranted = {
            when (val state = uiState) {
                is CameraUiState.Ready,
                is CameraUiState.Recording -> {
                    CameraPreviewContent(
                        isRecording = state is CameraUiState.Recording,
                        videoFileManager = videoFileManager,
                        onRecordingStarted = viewModel::onRecordingStarted,
                        onRecordingStopped = viewModel::onRecordingStopped,
                        onRecordingFailed = viewModel::onRecordingFailed,
                        modifier = modifier
                    )
                }
                is CameraUiState.Review -> {
                    ReviewContent(
                        filePath = state.filePath,
                        onSave = viewModel::saveEntry,
                        onDiscard = viewModel::discardRecording,
                        modifier = modifier
                    )
                }
                is CameraUiState.Error -> {
                    ErrorMessage(
                        message = state.throwable.message ?: "Recording failed",
                        onRetry = viewModel::resetToReady,
                        modifier = modifier
                    )
                }
                is CameraUiState.Saved -> { /* handled by LaunchedEffect */ }
            }
        },
        onDenied = {
            ErrorMessage(
                message = "Camera and microphone permissions are required to record video.",
                modifier = modifier
            )
        }
    )
}
