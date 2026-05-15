package co.ynd.interview.tomek.core.data

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import co.ynd.interview.tomek.core.database.VideoJournalDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class DefaultVideoEntryRepositoryTest {

    private lateinit var repository: DefaultVideoEntryRepository

    @Before
    fun setup() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        VideoJournalDatabase.Schema.create(driver)
        val queries = VideoJournalDatabase(driver).videoEntryQueries
        repository = DefaultVideoEntryRepository(queries)
    }

    @Test
    fun `add entry and retrieve from list`() = runTest {
        repository.add("/videos/test.mp4", "Test video", 5000L, null)

        val entries = repository.getVideoEntries().first()
        assertEquals(1, entries.size)
        assertEquals("Test video", entries[0].description)
        assertEquals("/videos/test.mp4", entries[0].filePath)
    }

    @Test
    fun `delete entry removes it from list`() = runTest {
        repository.add("/videos/test.mp4", "Test", 5000L, null)
        val entry = repository.getVideoEntries().first().first()

        repository.delete(entry.id)

        val entries = repository.getVideoEntries().first()
        assertEquals(0, entries.size)
    }

    @Test
    fun `getById returns correct entry`() = runTest {
        repository.add("/videos/test.mp4", "Test", 5000L, null)
        val entry = repository.getVideoEntries().first().first()

        val found = repository.getById(entry.id)
        assertNotNull(found)
        assertEquals("Test", found!!.description)
    }

    @Test
    fun `getById returns null for non-existent id`() = runTest {
        val found = repository.getById(999L)
        assertNull(found)
    }

    @Test
    fun `entries are returned latest first`() = runTest {
        repository.add("/videos/first.mp4", "First", 1000L, null)
        Thread.sleep(10) // ensure distinct createdAt timestamps for deterministic ordering
        repository.add("/videos/second.mp4", "Second", 2000L, null)

        val entries = repository.getVideoEntries().first()
        assertEquals(2, entries.size)
        assertEquals("Second", entries[0].description)
        assertEquals("First", entries[1].description)
    }
}
