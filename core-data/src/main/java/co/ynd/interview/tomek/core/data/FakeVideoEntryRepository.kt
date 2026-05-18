package co.ynd.interview.tomek.core.data

import co.ynd.interview.tomek.core.domain.model.VideoEntry
import co.ynd.interview.tomek.core.domain.repository.VideoEntryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeVideoEntryRepository : VideoEntryRepository {

    private val _entries = MutableStateFlow<List<VideoEntry>>(emptyList())
    private var nextId = 1L
    var shouldThrowOnAdd = false

    fun emit(entries: List<VideoEntry>) {
        _entries.value = entries
    }

    override fun getVideoEntries(): Flow<List<VideoEntry>> = _entries

    override suspend fun getById(id: Long): VideoEntry? =
        _entries.value.find { it.id == id }

    override suspend fun add(
        filePath: String,
        description: String,
        durationMs: Long,
        thumbnailPath: String?
    ) {
        if (shouldThrowOnAdd) throw RuntimeException("DB error")
        val entry = VideoEntry(
            id = nextId++,
            filePath = filePath,
            description = description,
            createdAt = System.currentTimeMillis(),
            durationMs = durationMs,
            thumbnailPath = thumbnailPath
        )
        _entries.value = listOf(entry) + _entries.value
    }

    override suspend fun delete(id: Long) {
        _entries.value = _entries.value.filter { it.id != id }
    }
}
