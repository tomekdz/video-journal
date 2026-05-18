package co.ynd.interview.tomek.core.domain

interface VideoFileCleaner {
    suspend fun delete(filePath: String, thumbnailPath: String?)
}
