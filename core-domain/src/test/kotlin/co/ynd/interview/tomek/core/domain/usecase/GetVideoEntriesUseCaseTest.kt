package co.ynd.interview.tomek.core.domain.usecase

import co.ynd.interview.tomek.core.domain.model.VideoEntry
import co.ynd.interview.tomek.core.domain.repository.VideoEntryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetVideoEntriesUseCaseTest {

    private val entries = listOf(
        VideoEntry(1L, "/a.mp4", "A", 0L, 1000L, null),
        VideoEntry(2L, "/b.mp4", "B", 0L, 2000L, null),
    )
    private val repo = object : VideoEntryRepository {
        override fun getVideoEntries(): Flow<List<VideoEntry>> = flowOf(entries)
        override suspend fun getById(id: Long): VideoEntry? = null
        override suspend fun add(filePath: String, description: String, durationMs: Long, thumbnailPath: String?) {}
        override suspend fun delete(id: Long) {}
    }
    private val useCase = GetVideoEntriesUseCase(repo)

    @Test
    fun `returns entries from repository`() = runTest {
        val result = useCase().first()
        assertEquals(entries, result)
    }
}
