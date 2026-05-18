package co.ynd.interview.tomek.core.data

import co.ynd.interview.tomek.core.domain.VideoFileCleaner
import java.io.File

class FileSystemVideoFileCleaner : VideoFileCleaner {
    override suspend fun delete(filePath: String, thumbnailPath: String?) {
        File(filePath).delete()
        thumbnailPath?.let { File(it).delete() }
    }
}
