package co.ynd.interview.tomek.feature.feed.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ynd.interview.tomek.core.domain.usecase.DeleteVideoEntryUseCase
import co.ynd.interview.tomek.core.domain.usecase.GetVideoEntriesUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FeedViewModel(
    getVideoEntriesUseCase: GetVideoEntriesUseCase,
    private val deleteVideoEntryUseCase: DeleteVideoEntryUseCase
) : ViewModel() {

    val uiState: StateFlow<FeedUiState> = getVideoEntriesUseCase()
        .map<_, FeedUiState> { FeedUiState.Success(entries = it) }
        .catch { emit(FeedUiState.Error(it)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FeedUiState.Loading)

    private val _events = MutableSharedFlow<FeedEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<FeedEvent> = _events.asSharedFlow()

    fun deleteEntry(id: Long) {
        viewModelScope.launch {
            try {
                deleteVideoEntryUseCase(id)
            } catch (t: Throwable) {
                _events.tryEmit(FeedEvent.DeleteFailed)
            }
        }
    }
}
