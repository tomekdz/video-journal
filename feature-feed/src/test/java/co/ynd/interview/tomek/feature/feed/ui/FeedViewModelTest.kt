package co.ynd.interview.tomek.feature.feed.ui

import co.ynd.interview.tomek.core.data.FakeVideoEntryRepository
import co.ynd.interview.tomek.core.domain.VideoFileCleaner
import co.ynd.interview.tomek.core.domain.model.VideoEntry
import co.ynd.interview.tomek.core.domain.usecase.DeleteVideoEntryUseCase
import co.ynd.interview.tomek.core.domain.usecase.GetVideoEntriesUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FeedViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val fakeRepository = FakeVideoEntryRepository()
    private lateinit var viewModel: FeedViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = FeedViewModel(
            getVideoEntriesUseCase = GetVideoEntriesUseCase(fakeRepository),
            deleteVideoEntryUseCase = DeleteVideoEntryUseCase(
                fakeRepository,
                object : VideoFileCleaner { override suspend fun delete(filePath: String, thumbnailPath: String?) {} }
            )
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // WhileSubscribed only starts the upstream when there is an active collector.
    // Launching with UnconfinedTestDispatcher subscribes eagerly and triggers the upstream.
    private fun TestScope.startCollecting() = launch(UnconfinedTestDispatcher(testScheduler)) {
        viewModel.uiState.collect {}
    }

    @Test
    fun `initial state is Loading`() {
        assertEquals(FeedUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `emits Success when repository emits entries`() = runTest {
        val job = startCollecting()
        fakeRepository.emit(listOf(VideoEntry(1, "/test.mp4", "Test", 0L, 5000L, null)))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is FeedUiState.Success)
        assertEquals(1, (state as FeedUiState.Success).entries.size)
        job.cancel()
    }

    @Test
    fun `deleteEntry removes entry from state`() = runTest {
        val job = startCollecting()
        fakeRepository.emit(listOf(VideoEntry(1, "/test.mp4", "Test", 0L, 5000L, null)))

        viewModel.deleteEntry(1)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is FeedUiState.Success)
        assertEquals(0, (state as FeedUiState.Success).entries.size)
        job.cancel()
    }

    @Test
    fun `state reflects all entries from repository`() = runTest {
        val job = startCollecting()
        fakeRepository.emit(
            listOf(
                VideoEntry(1, "/a.mp4", "A", 0L, 1000L, null),
                VideoEntry(2, "/b.mp4", "B", 0L, 2000L, null),
            )
        )
        advanceUntilIdle()

        assertEquals(2, (viewModel.uiState.value as FeedUiState.Success).entries.size)
        job.cancel()
    }
}
