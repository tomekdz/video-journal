package co.ynd.interview.tomek.feature.camera.ui

import co.ynd.interview.tomek.core.domain.model.VideoEntry
import co.ynd.interview.tomek.core.domain.repository.VideoEntryRepository
import co.ynd.interview.tomek.core.domain.usecase.SaveVideoEntryUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CameraViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val fakeRepo = FakeRepo()
    private lateinit var viewModel: CameraViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = CameraViewModel(SaveVideoEntryUseCase(fakeRepo))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Ready`() {
        assertEquals(CameraUiState.Ready, viewModel.uiState.value)
    }

    @Test
    fun `onRecordingStarted changes state to Recording`() {
        viewModel.onRecordingStarted()
        assertEquals(CameraUiState.Recording, viewModel.uiState.value)
    }

    @Test
    fun `onRecordingStopped changes state to Review`() {
        viewModel.onRecordingStopped("/test.mp4", 5000L, "/thumb.jpg")

        val state = viewModel.uiState.value
        assertTrue(state is CameraUiState.Review)
        assertEquals("/test.mp4", (state as CameraUiState.Review).filePath)
        assertEquals(5000L, state.durationMs)
        assertEquals("/thumb.jpg", state.thumbnailPath)
    }

    @Test
    fun `saveEntry changes state to Saved`() = runTest {
        viewModel.onRecordingStopped("/test.mp4", 5000L, null)
        viewModel.saveEntry("My video")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(CameraUiState.Saved, viewModel.uiState.value)
    }

    @Test
    fun `saveEntry does nothing when not in Review state`() = runTest {
        viewModel.saveEntry("ignored")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(CameraUiState.Ready, viewModel.uiState.value)
    }

    @Test
    fun `discardRecording resets to Ready`() {
        viewModel.onRecordingStopped("/nonexistent.mp4", 5000L, null)
        viewModel.discardRecording()

        assertEquals(CameraUiState.Ready, viewModel.uiState.value)
    }

    @Test
    fun `onRecordingFailed changes state to Error`() {
        val error = RuntimeException("Camera error")
        viewModel.onRecordingFailed(error)

        val state = viewModel.uiState.value
        assertTrue(state is CameraUiState.Error)
        assertEquals(error, (state as CameraUiState.Error).throwable)
    }

    @Test
    fun `resetToReady clears Error state`() {
        viewModel.onRecordingFailed(RuntimeException("error"))
        viewModel.resetToReady()

        assertEquals(CameraUiState.Ready, viewModel.uiState.value)
    }
}

private class FakeRepo : VideoEntryRepository {
    override fun getVideoEntries(): Flow<List<VideoEntry>> = flowOf(emptyList())
    override suspend fun getById(id: Long): VideoEntry? = null
    override suspend fun add(filePath: String, description: String, durationMs: Long, thumbnailPath: String?) {}
    override suspend fun delete(id: Long) {}
}
