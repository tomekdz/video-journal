package co.ynd.interview.tomek.feature.camera.ui

import androidx.camera.core.CameraSelector
import androidx.camera.core.SurfaceRequest
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.SavedStateHandle
import co.ynd.interview.tomek.core.data.FakeVideoEntryRepository
import co.ynd.interview.tomek.core.domain.VideoFileCleaner
import co.ynd.interview.tomek.core.domain.usecase.SaveVideoEntryUseCase
import co.ynd.interview.tomek.feature.camera.recorder.RecordingEvent
import co.ynd.interview.tomek.feature.camera.recorder.VideoRecorder
import co.ynd.interview.tomek.feature.camera.util.VideoFileOperations
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class CameraViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val fakeRepo = FakeVideoEntryRepository()
    private val fakeRecorder = FakeVideoRecorder()
    private val fakeFileOps = FakeVideoFileOperations()
    private val fakeCleaner = FakeVideoFileCleaner()
    private lateinit var viewModel: CameraViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = CameraViewModel(
            saveVideoEntryUseCase = SaveVideoEntryUseCase(fakeRepo),
            videoRecorder = fakeRecorder,
            fileOperations = fakeFileOps,
            savedStateHandle = SavedStateHandle(),
            fileCleaner = fakeCleaner,
            ioDispatcher = testDispatcher,
        )
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
    fun `startRecording transitions to Recording on Start event`() {
        viewModel.startRecording()
        fakeRecorder.emit(RecordingEvent.Started)

        assertEquals(CameraUiState.Recording, viewModel.uiState.value)
    }

    @Test
    fun `Finalized event transitions to Review`() = runTest {
        viewModel.startRecording()
        fakeRecorder.emit(RecordingEvent.Started)
        fakeRecorder.emit(RecordingEvent.Finalized(5000L))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is CameraUiState.Review)
        assertEquals(5000L, (state as CameraUiState.Review).durationMs)
    }

    @Test
    fun `Failed event transitions to Error`() {
        viewModel.startRecording()
        fakeRecorder.emit(RecordingEvent.Failed("Encoding failed"))

        val state = viewModel.uiState.value
        assertTrue(state is CameraUiState.Error)
    }

    @Test
    fun `saveEntry changes state to Saved and persists entry`() = runTest {
        driveToReview()
        viewModel.saveEntry("My video")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(CameraUiState.Saved, viewModel.uiState.value)
        val entries = fakeRepo.getVideoEntries().first()
        assertEquals(1, entries.size)
        assertEquals(fakeFileOps.videoFile.absolutePath, entries[0].filePath)
        assertEquals("My video", entries[0].description)
    }

    @Test
    fun `saveEntry transitions to SaveError when repository throws`() = runTest {
        fakeRepo.shouldThrowOnAdd = true
        driveToReview()
        viewModel.saveEntry("My video")
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value is CameraUiState.SaveError)
    }

    @Test
    fun `saveEntry does nothing when not in Review state`() = runTest {
        viewModel.saveEntry("ignored")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(CameraUiState.Ready, viewModel.uiState.value)
    }

    @Test
    fun `retrySave succeeds after transient failure`() = runTest {
        fakeRepo.shouldThrowOnAdd = true
        driveToReview()
        viewModel.saveEntry("My video")
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value is CameraUiState.SaveError)

        fakeRepo.shouldThrowOnAdd = false
        viewModel.retrySave("My video")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(CameraUiState.Saved, viewModel.uiState.value)
    }

    @Test
    fun `discardRecording resets to Ready`() = runTest {
        driveToReview()
        viewModel.discardRecording()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(CameraUiState.Ready, viewModel.uiState.value)
    }

    @Test
    fun `discardRecording invokes fileCleaner with correct paths`() = runTest {
        driveToReview()
        viewModel.discardRecording()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(
            listOf(fakeFileOps.videoFile.absolutePath to null),
            fakeCleaner.deletedPaths
        )
    }

    @Test
    fun `discardRecording from SaveError resets to Ready`() = runTest {
        fakeRepo.shouldThrowOnAdd = true
        driveToReview()
        viewModel.saveEntry("test")
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value is CameraUiState.SaveError)

        viewModel.discardRecording()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(CameraUiState.Ready, viewModel.uiState.value)
    }

    @Test
    fun `resetToReady clears Error state`() {
        viewModel.startRecording()
        fakeRecorder.emit(RecordingEvent.Failed("error"))
        viewModel.resetToReady()

        assertEquals(CameraUiState.Ready, viewModel.uiState.value)
    }

    @Test
    fun `flipCamera does not flip while recording`() {
        viewModel.startRecording()
        fakeRecorder.emit(RecordingEvent.Started)

        viewModel.flipCamera()

        assertEquals(CameraSelector.DEFAULT_BACK_CAMERA, viewModel.lensFacing.value)
    }

    @Test
    fun `flipCamera toggles lens facing when Ready`() {
        viewModel.flipCamera()
        assertEquals(CameraSelector.DEFAULT_FRONT_CAMERA, viewModel.lensFacing.value)

        viewModel.flipCamera()
        assertEquals(CameraSelector.DEFAULT_BACK_CAMERA, viewModel.lensFacing.value)
    }

    private fun driveToReview() {
        viewModel.startRecording()
        fakeRecorder.emit(RecordingEvent.Started)
        fakeRecorder.emit(RecordingEvent.Finalized(5000L))
        testDispatcher.scheduler.advanceUntilIdle()
    }
}

private class FakeVideoRecorder : VideoRecorder {
    override val surfaceRequest: StateFlow<SurfaceRequest?> = MutableStateFlow(null)
    private var callback: ((RecordingEvent) -> Unit)? = null
    override suspend fun bind(lifecycleOwner: LifecycleOwner, lensFacing: CameraSelector) {}
    override fun startRecording(file: File, onEvent: (RecordingEvent) -> Unit) { callback = onEvent }
    override fun stopRecording() { callback = null }
    fun emit(event: RecordingEvent) = checkNotNull(callback) { "startRecording not called" }(event)
}

private class FakeVideoFileOperations(
    val videoFile: File = File("/fake/video.mp4")
) : VideoFileOperations {
    override fun createVideoFile(): File = videoFile
    override fun extractThumbnail(videoFile: File): File? = null
}

private class FakeVideoFileCleaner : VideoFileCleaner {
    val deletedPaths = mutableListOf<Pair<String, String?>>()
    override suspend fun delete(filePath: String, thumbnailPath: String?) {
        deletedPaths.add(filePath to thumbnailPath)
    }
}
