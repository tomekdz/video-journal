package co.ynd.interview.tomek.core.domain.usecase

import co.ynd.interview.tomek.core.domain.VideoFileCleaner
import co.ynd.interview.tomek.core.domain.model.VideoEntry
import co.ynd.interview.tomek.core.domain.repository.VideoEntryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeleteVideoEntryUseCaseTest {

    private val entries = mutableMapOf<Long, VideoEntry>()
    private val deletedIds = mutableListOf<Long>()
    private val cleanedPaths = mutableListOf<Pair<String, String?>>()

    private val repo = object : VideoEntryRepository {
        override fun getVideoEntries(): Flow<List<VideoEntry>> = flowOf(entries.values.toList())
        override suspend fun getById(id: Long): VideoEntry? = entries[id]
        override suspend fun add(filePath: String, description: String, durationMs: Long, thumbnailPath: String?) {}
        override suspend fun delete(id: Long) { deletedIds += id; entries.remove(id) }
    }
    private val cleaner = object : VideoFileCleaner {
        override suspend fun delete(filePath: String, thumbnailPath: String?) {
            cleanedPaths += filePath to thumbnailPath
        }
    }
    private val useCase = DeleteVideoEntryUseCase(repo, cleaner)

    @Test
    fun `deletes entry and cleans files when entry exists`() = runTest {
        entries[1L] = VideoEntry(1L, "/video.mp4", "Test", 0L, 5000L, "/thumb.jpg")

        useCase(1L)

        assertEquals(listOf(1L), deletedIds)
        assertEquals(listOf("/video.mp4" to "/thumb.jpg"), cleanedPaths)
    }

    @Test
    fun `deletes entry with null thumbnailPath`() = runTest {
        entries[2L] = VideoEntry(2L, "/video2.mp4", "Test", 0L, 5000L, null)

        useCase(2L)

        assertEquals(listOf("/video2.mp4" to null), cleanedPaths)
    }

    @Test
    fun `does nothing when entry does not exist`() = runTest {
        useCase(999L)

        assertTrue(deletedIds.isEmpty())
        assertTrue(cleanedPaths.isEmpty())
    }
}
