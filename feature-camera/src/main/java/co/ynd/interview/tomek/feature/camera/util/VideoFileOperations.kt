package co.ynd.interview.tomek.feature.camera.util

import java.io.File

interface VideoFileOperations {
    fun createVideoFile(): File
    fun extractThumbnail(videoFile: File): File?
}
