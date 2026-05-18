package co.ynd.interview.tomek.feature.camera.util

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import java.io.File
import java.io.FileOutputStream

class VideoFileManager(private val context: Context) : VideoFileOperations {

    private val videosDir: File = File(context.filesDir, "videos").also { it.mkdirs() }
    private val thumbnailsDir: File = File(context.filesDir, "thumbnails").also { it.mkdirs() }

    override fun createVideoFile(): File {
        return File(videosDir, "video_${System.currentTimeMillis()}.mp4")
    }

    override fun extractThumbnail(videoFile: File): File? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(videoFile.absolutePath)
            val bitmap = retriever.getFrameAtTime(500_000L, MediaMetadataRetriever.OPTION_CLOSEST)
                ?: return null
            val thumbFile = File(thumbnailsDir, "thumb_${System.currentTimeMillis()}.jpg")
            FileOutputStream(thumbFile).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 80, it) }
            thumbFile
        } catch (_: RuntimeException) {
            null
        } finally {
            retriever.release()
        }
    }
}
