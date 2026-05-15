package co.ynd.interview.tomek.core.domain.usecase

import co.ynd.interview.tomek.core.domain.model.VideoEntry
import co.ynd.interview.tomek.core.domain.repository.VideoEntryRepository

class GetVideoEntryByIdUseCase(private val repository: VideoEntryRepository) {
    suspend operator fun invoke(id: Long): VideoEntry? = repository.getById(id)
}
