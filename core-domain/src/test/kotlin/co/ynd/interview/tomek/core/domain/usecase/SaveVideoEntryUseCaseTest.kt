package co.ynd.interview.tomek.core.domain.usecase

import co.ynd.interview.tomek.core.domain.model.VideoEntry
import co.ynd.interview.tomek.core.domain.repository.VideoEntryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SaveVideoEntryUseCaseTest {

    private val recorded = mutableListOf<Map<String, Any?>>()
    private val repo = object : VideoEntryRepository {
        override fun getVideoEntries(): Flow<List<VideoEntry>> = flowOf(emptyList())
        override suspend fun getById(id: Long): VideoEntry? = null
        override suspend fun add(filePath: String, description: String, durationMs: Long, thumbnailPath: String?) {
            recorded += mapOf("filePath" to filePath, "description" to description, "durationMs" to durationMs, "thumbnailPath" to thumbnailPath)
        }
        override suspend fun delete(id: Long) {}
    }
    private val useCase = SaveVideoEntryUseCase(repo)

    @Test
    fun `delegates all fields to repository add`() = runTest {
        useCase("/video.mp4", "My video", 5000L, "/thumb.jpg")

        assertEquals(1, recorded.size)
        assertEquals("/video.mp4", recorded[0]["filePath"])
        assertEquals("My video", recorded[0]["description"])
        assertEquals(5000L, recorded[0]["durationMs"])
        assertEquals("/thumb.jpg", recorded[0]["thumbnailPath"])
    }

    @Test
    fun `passes null thumbnailPath to repository`() = runTest {
        useCase("/video.mp4", "No thumb", 2000L, null)

        assertEquals(null, recorded[0]["thumbnailPath"])
    }
}
