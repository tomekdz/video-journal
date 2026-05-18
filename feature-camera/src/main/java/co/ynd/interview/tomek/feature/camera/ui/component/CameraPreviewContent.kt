package co.ynd.interview.tomek.feature.camera.ui.component

import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.SurfaceRequest
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import co.ynd.interview.tomek.core.ui.theme.RecordRed
import co.ynd.interview.tomek.core.ui.util.formatDuration
import co.ynd.interview.tomek.feature.camera.R

@Composable
internal fun CameraPreviewContent(
    surfaceRequest: SurfaceRequest?,
    isRecording: Boolean,
    recordingDurationMs: Long,
    onToggleRecord: () -> Unit,
    onFlipCamera: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        surfaceRequest?.let { request ->
            CameraXViewfinder(
                surfaceRequest = request,
                modifier = Modifier.fillMaxSize()
            )
        }

        if (isRecording) {
            Text(
                text = formatDuration(recordingDurationMs),
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 32.dp)
            )
        }

        IconButton(
            onClick = onFlipCamera,
            enabled = !isRecording,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 32.dp, bottom = 60.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.4f))
        ) {
            Icon(
                imageVector = Icons.Filled.FlipCameraAndroid,
                contentDescription = stringResource(R.string.camera_flip_cd),
                tint = Color.White
            )
        }

        val scale = remember { Animatable(1f) }
        LaunchedEffect(isRecording) {
            if (isRecording) {
                while (true) {
                    scale.animateTo(1.2f, animationSpec = tween(600))
                    scale.animateTo(1f, animationSpec = tween(600))
                }
            } else {
                scale.animateTo(1f, animationSpec = tween(300))
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .size(72.dp)
                .scale(scale.value)
                .clip(CircleShape)
                .background(if (isRecording) Color.DarkGray else RecordRed)
                .clickable { onToggleRecord() }
        )
    }
}
