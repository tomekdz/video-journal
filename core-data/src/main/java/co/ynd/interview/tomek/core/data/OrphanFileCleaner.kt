package co.ynd.interview.tomek.core.data

import co.ynd.interview.tomek.core.domain.repository.VideoEntryRepository
import kotlinx.coroutines.flow.first
import java.io.File

class OrphanFileCleaner(
    private val repository: VideoEntryRepository,
    private val videosDir: File,
    private val thumbnailsDir: File,
) {
    suspend fun clean() {
        val entries = repository.getVideoEntries().first()
        val knownPaths = buildSet {
            entries.forEach { entry ->
                add(entry.filePath)
                entry.thumbnailPath?.let { add(it) }
            }
        }

        listOf(videosDir, thumbnailsDir).forEach { dir ->
            dir.listFiles()?.forEach { file ->
                if (file.absolutePath !in knownPaths) {
                    file.delete()
                }
            }
        }
    }
}
