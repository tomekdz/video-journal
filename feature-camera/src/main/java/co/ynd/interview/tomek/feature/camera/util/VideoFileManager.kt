package co.ynd.interview.tomek.feature.camera.util

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import java.io.File
import java.io.FileOutputStream

class VideoFileManager(private val context: Context) {

    private val videosDir: File
        get() = File(context.filesDir, "videos").also { it.mkdirs() }

    private val thumbnailsDir: File
        get() = File(context.filesDir, "thumbnails").also { it.mkdirs() }

    fun createVideoFile(): File {
        return File(videosDir, "video_${System.currentTimeMillis()}.mp4")
    }

    fun extractThumbnail(videoFile: File): File? {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(videoFile.absolutePath)
            val bitmap = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            retriever.release()

            bitmap?.let {
                val thumbFile = File(thumbnailsDir, "thumb_${System.currentTimeMillis()}.jpg")
                FileOutputStream(thumbFile).use { out ->
                    it.compress(Bitmap.CompressFormat.JPEG, 80, out)
                }
                thumbFile
            }
        } catch (e: Exception) {
            null
        }
    }

    fun deleteFile(path: String) {
        File(path).delete()
    }
}
