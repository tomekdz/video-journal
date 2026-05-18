package co.ynd.interview.tomek.core.domain.usecase

import co.ynd.interview.tomek.core.domain.VideoFileCleaner
import co.ynd.interview.tomek.core.domain.repository.VideoEntryRepository

class DeleteVideoEntryUseCase(
    private val repository: VideoEntryRepository,
    private val cleaner: VideoFileCleaner,
) {
    suspend operator fun invoke(id: Long) {
        val entry = repository.getById(id) ?: return
        repository.delete(id)
        cleaner.delete(entry.filePath, entry.thumbnailPath)
    }
}
