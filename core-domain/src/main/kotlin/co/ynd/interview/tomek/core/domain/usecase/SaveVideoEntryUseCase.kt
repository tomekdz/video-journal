package co.ynd.interview.tomek.core.domain.usecase

import co.ynd.interview.tomek.core.domain.repository.VideoEntryRepository

class SaveVideoEntryUseCase(private val repository: VideoEntryRepository) {
    suspend operator fun invoke(
        filePath: String,
        description: String,
        durationMs: Long,
        thumbnailPath: String?
    ) {
        repository.add(filePath, description, durationMs, thumbnailPath)
    }
}
