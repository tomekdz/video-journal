package co.ynd.interview.tomek.feature.camera.recorder

sealed interface RecordingEvent {
    data object Started : RecordingEvent
    data class Finalized(val durationMs: Long) : RecordingEvent
    data class Failed(val message: String) : RecordingEvent
}
