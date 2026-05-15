package co.ynd.interview.tomek.core.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import co.ynd.interview.tomek.core.data.mapper.toDomain
import co.ynd.interview.tomek.core.database.VideoEntryQueries
import co.ynd.interview.tomek.core.domain.model.VideoEntry
import co.ynd.interview.tomek.core.domain.repository.VideoEntryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class DefaultVideoEntryRepository(
    private val queries: VideoEntryQueries
) : VideoEntryRepository {

    override fun getVideoEntries(): Flow<List<VideoEntry>> =
        queries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: Long): VideoEntry? = withContext(Dispatchers.IO) {
        queries.selectById(id).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun add(
        filePath: String,
        description: String,
        durationMs: Long,
        thumbnailPath: String?
    ) {
        withContext(Dispatchers.IO) {
            queries.insert(
                filePath = filePath,
                description = description,
                createdAt = System.currentTimeMillis(),
                durationMs = durationMs,
                thumbnailPath = thumbnailPath
            )
        }
    }

    override suspend fun delete(id: Long) {
        withContext(Dispatchers.IO) {
            queries.deleteById(id)
        }
    }
}
