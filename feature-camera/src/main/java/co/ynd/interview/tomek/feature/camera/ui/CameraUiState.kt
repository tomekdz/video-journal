package co.ynd.interview.tomek.feature.camera.ui

sealed interface CameraUiState {
    data object Ready : CameraUiState
    data object Recording : CameraUiState
    data class Review(
        val filePath: String,
        val durationMs: Long,
        val thumbnailPath: String?
    ) : CameraUiState
    data class Error(val throwable: Throwable) : CameraUiState
    data class SaveError(val review: Review, val throwable: Throwable) : CameraUiState
    data object Saved : CameraUiState
}
