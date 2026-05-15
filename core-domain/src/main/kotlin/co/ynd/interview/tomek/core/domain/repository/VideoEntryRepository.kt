package co.ynd.interview.tomek.core.domain.repository

import co.ynd.interview.tomek.core.domain.model.VideoEntry
import kotlinx.coroutines.flow.Flow

interface VideoEntryRepository {
    fun getVideoEntries(): Flow<List<VideoEntry>>
    suspend fun getById(id: Long): VideoEntry?
    suspend fun add(filePath: String, description: String, durationMs: Long, thumbnailPath: String?)
    suspend fun delete(id: Long)
}
