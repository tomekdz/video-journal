package co.ynd.interview.tomek.feature.camera.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.ynd.interview.tomek.core.ui.component.ErrorMessage
import co.ynd.interview.tomek.feature.camera.R
import co.ynd.interview.tomek.feature.camera.ui.component.CameraPreviewContent
import co.ynd.interview.tomek.feature.camera.ui.component.ReviewContent
import co.ynd.interview.tomek.feature.camera.ui.permission.RequireCameraPermissions
import org.koin.androidx.compose.koinViewModel

@Composable
fun CameraScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CameraViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val surfaceRequest by viewModel.surfaceRequest.collectAsStateWithLifecycle()
    val lensFacing by viewModel.lensFacing.collectAsStateWithLifecycle()
    val recordingDurationMs by viewModel.recordingDurationMs.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    val recordingFailedMessage = stringResource(R.string.camera_recording_failed)
    val saveFailedMessage = stringResource(R.string.camera_save_failed)
    val permissionDeniedMessage = stringResource(R.string.camera_permission_denied)

    LaunchedEffect(lensFacing) {
        viewModel.bindCamera(lifecycleOwner)
    }

    LaunchedEffect(uiState is CameraUiState.Saved) {
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
                        surfaceRequest = surfaceRequest,
                        isRecording = state is CameraUiState.Recording,
                        recordingDurationMs = recordingDurationMs,
                        onToggleRecord = {
                            if (state is CameraUiState.Recording) viewModel.stopRecording()
                            else viewModel.startRecording()
                        },
                        onFlipCamera = viewModel::flipCamera,
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
                        message = state.throwable.message ?: recordingFailedMessage,
                        onRetry = viewModel::resetToReady,
                        modifier = modifier
                    )
                }
                is CameraUiState.SaveError -> {
                    ReviewContent(
                        filePath = state.review.filePath,
                        onSave = { description -> viewModel.retrySave(description) },
                        onDiscard = viewModel::discardRecording,
                        saveError = state.throwable.message ?: saveFailedMessage,
                        modifier = modifier
                    )
                }
                is CameraUiState.Saved -> { /* handled by LaunchedEffect */ }
            }
        },
        onDenied = {
            ErrorMessage(
                message = permissionDeniedMessage,
                modifier = modifier
            )
        }
    )
}
