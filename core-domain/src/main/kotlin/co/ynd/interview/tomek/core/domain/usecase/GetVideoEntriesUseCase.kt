package co.ynd.interview.tomek.core.domain.usecase

import co.ynd.interview.tomek.core.domain.model.VideoEntry
import co.ynd.interview.tomek.core.domain.repository.VideoEntryRepository
import kotlinx.coroutines.flow.Flow

class GetVideoEntriesUseCase(private val repository: VideoEntryRepository) {
    operator fun invoke(): Flow<List<VideoEntry>> = repository.getVideoEntries()
}
