package co.ynd.interview.tomek.core.domain.model

data class VideoEntry(
    val id: Long,
    val filePath: String,
    val description: String,
    val createdAt: Long,
    val durationMs: Long,
    val thumbnailPath: String?
)
