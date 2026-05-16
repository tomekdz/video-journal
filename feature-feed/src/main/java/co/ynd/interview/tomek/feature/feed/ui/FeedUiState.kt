package co.ynd.interview.tomek.feature.feed.ui

import co.ynd.interview.tomek.core.domain.model.VideoEntry

sealed interface FeedUiState {
    data object Loading : FeedUiState
    data class Error(val throwable: Throwable) : FeedUiState
    data class Success(val entries: List<VideoEntry>) : FeedUiState
}
