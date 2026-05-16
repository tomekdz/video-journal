package co.ynd.interview.tomek.feature.camera.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ynd.interview.tomek.core.domain.usecase.SaveVideoEntryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class CameraViewModel(
    private val saveVideoEntryUseCase: SaveVideoEntryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<CameraUiState>(CameraUiState.Ready)
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    fun onRecordingStarted() {
        _uiState.value = CameraUiState.Recording
    }

    fun onRecordingStopped(filePath: String, durationMs: Long, thumbnailPath: String?) {
        _uiState.value = CameraUiState.Review(filePath, durationMs, thumbnailPath)
    }

    fun onRecordingFailed(error: Throwable) {
        _uiState.value = CameraUiState.Error(error)
    }

    fun saveEntry(description: String) {
        val reviewState = _uiState.value as? CameraUiState.Review ?: return
        viewModelScope.launch {
            saveVideoEntryUseCase(
                filePath = reviewState.filePath,
                description = description,
                durationMs = reviewState.durationMs,
                thumbnailPath = reviewState.thumbnailPath
            )
            _uiState.value = CameraUiState.Saved
        }
    }

    fun discardRecording() {
        val reviewState = _uiState.value as? CameraUiState.Review ?: return
        File(reviewState.filePath).delete()
        reviewState.thumbnailPath?.let { File(it).delete() }
        _uiState.value = CameraUiState.Ready
    }

    fun resetToReady() {
        _uiState.value = CameraUiState.Ready
    }
}
