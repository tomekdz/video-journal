package co.ynd.interview.tomek.core.domain.usecase

import co.ynd.interview.tomek.core.domain.repository.VideoEntryRepository

class DeleteVideoEntryUseCase(private val repository: VideoEntryRepository) {
    suspend operator fun invoke(id: Long) = repository.delete(id)
}
