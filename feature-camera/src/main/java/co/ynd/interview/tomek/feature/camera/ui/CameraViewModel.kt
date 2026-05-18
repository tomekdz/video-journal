package co.ynd.interview.tomek.feature.camera.ui

import androidx.camera.core.CameraSelector
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ynd.interview.tomek.core.domain.VideoFileCleaner
import co.ynd.interview.tomek.core.domain.usecase.SaveVideoEntryUseCase
import co.ynd.interview.tomek.feature.camera.recorder.RecordingEvent
import co.ynd.interview.tomek.feature.camera.recorder.VideoRecorder
import co.ynd.interview.tomek.feature.camera.util.VideoFileOperations
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CameraViewModel(
    private val saveVideoEntryUseCase: SaveVideoEntryUseCase,
    private val videoRecorder: VideoRecorder,
    private val fileOperations: VideoFileOperations,
    private val savedStateHandle: SavedStateHandle,
    private val fileCleaner: VideoFileCleaner,
) : ViewModel() {

    val surfaceRequest = videoRecorder.surfaceRequest

    private val _lensFacing = MutableStateFlow(CameraSelector.DEFAULT_BACK_CAMERA)
    val lensFacing: StateFlow<CameraSelector> = _lensFacing.asStateFlow()

    private val _uiState = MutableStateFlow<CameraUiState>(restoreReviewState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    private val _recordingDurationMs = MutableStateFlow(0L)
    val recordingDurationMs: StateFlow<Long> = _recordingDurationMs.asStateFlow()

    private var durationJob: Job? = null

    private fun restoreReviewState(): CameraUiState {
        val filePath = savedStateHandle.get<String>(KEY_FILE_PATH) ?: return CameraUiState.Ready
        val durationMs = savedStateHandle.get<Long>(KEY_DURATION_MS) ?: return CameraUiState.Ready
        val thumbnailPath = savedStateHandle.get<String?>(KEY_THUMBNAIL_PATH)
        return CameraUiState.Review(filePath, durationMs, thumbnailPath)
    }

    private fun setReviewState(review: CameraUiState.Review) {
        savedStateHandle[KEY_FILE_PATH] = review.filePath
        savedStateHandle[KEY_DURATION_MS] = review.durationMs
        savedStateHandle[KEY_THUMBNAIL_PATH] = review.thumbnailPath
        _uiState.value = review
    }

    private fun clearReviewState() {
        savedStateHandle.remove<String>(KEY_FILE_PATH)
        savedStateHandle.remove<Long>(KEY_DURATION_MS)
        savedStateHandle.remove<String?>(KEY_THUMBNAIL_PATH)
    }

    fun bindCamera(lifecycleOwner: LifecycleOwner) {
        viewModelScope.launch {
            try {
                videoRecorder.bind(lifecycleOwner, _lensFacing.value)
            } catch (t: Throwable) {
                _uiState.value = CameraUiState.Error(t)
            }
        }
    }

    fun flipCamera() {
        if (_uiState.value is CameraUiState.Recording) return
        _lensFacing.value = if (_lensFacing.value == CameraSelector.DEFAULT_BACK_CAMERA) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }
    }

    fun startRecording() {
        if (_uiState.value !is CameraUiState.Ready) return
        val file = fileOperations.createVideoFile()
        videoRecorder.startRecording(file) { event ->
            when (event) {
                RecordingEvent.Started -> {
                    _uiState.value = CameraUiState.Recording
                    _recordingDurationMs.value = 0L
                    durationJob = viewModelScope.launch {
                        while (true) {
                            delay(1000)
                            _recordingDurationMs.value += 1000
                        }
                    }
                }
                is RecordingEvent.Finalized -> {
                    durationJob?.cancel()
                    durationJob = null
                    val durationMs = event.durationMs
                    viewModelScope.launch(Dispatchers.IO) {
                        val thumbFile = fileOperations.extractThumbnail(file)
                        withContext(Dispatchers.Main) {
                            setReviewState(CameraUiState.Review(file.absolutePath, durationMs, thumbFile?.absolutePath))
                        }
                    }
                }
                is RecordingEvent.Failed -> {
                    durationJob?.cancel()
                    durationJob = null
                    _uiState.value = CameraUiState.Error(RuntimeException(event.message))
                }
            }
        }
    }

    fun stopRecording() {
        videoRecorder.stopRecording()
    }

    fun saveEntry(description: String) {
        val reviewState = _uiState.value as? CameraUiState.Review ?: return
        viewModelScope.launch {
            try {
                saveVideoEntryUseCase(
                    filePath = reviewState.filePath,
                    description = description,
                    durationMs = reviewState.durationMs,
                    thumbnailPath = reviewState.thumbnailPath
                )
                clearReviewState()
                _uiState.value = CameraUiState.Saved
            } catch (t: Throwable) {
                _uiState.value = CameraUiState.SaveError(reviewState, t)
            }
        }
    }

    fun retrySave(description: String) {
        val saveError = _uiState.value as? CameraUiState.SaveError ?: return
        _uiState.value = saveError.review
        saveEntry(description)
    }

    fun discardRecording() {
        val review = when (val state = _uiState.value) {
            is CameraUiState.Review -> state
            is CameraUiState.SaveError -> state.review
            else -> return
        }
        clearReviewState()
        _uiState.value = CameraUiState.Ready
        viewModelScope.launch {
            fileCleaner.delete(review.filePath, review.thumbnailPath)
        }
    }

    fun resetToReady() {
        _uiState.value = CameraUiState.Ready
    }

    override fun onCleared() {
        super.onCleared()
        durationJob?.cancel()
        videoRecorder.stopRecording()
    }

    private companion object {
        const val KEY_FILE_PATH = "review_filePath"
        const val KEY_DURATION_MS = "review_durationMs"
        const val KEY_THUMBNAIL_PATH = "review_thumbnailPath"
    }
}
