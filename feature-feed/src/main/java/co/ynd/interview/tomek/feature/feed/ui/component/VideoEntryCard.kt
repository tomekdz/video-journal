package co.ynd.interview.tomek.feature.feed.ui.component

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import co.ynd.interview.tomek.core.domain.model.VideoEntry
import co.ynd.interview.tomek.core.ui.component.VideoPlayer
import co.ynd.interview.tomek.core.ui.component.VideoThumbnail
import co.ynd.interview.tomek.core.ui.util.formatDuration
import co.ynd.interview.tomek.core.ui.util.formatRelativeTime
import co.ynd.interview.tomek.feature.feed.R

@Composable
internal fun VideoEntryCard(
    entry: VideoEntry,
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Crossfade(targetState = isPlaying, label = "video_player") { playing ->
            if (playing) {
                VideoPlayer(
                    filePath = entry.filePath,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(MaterialTheme.shapes.medium)
                        .clickable { onTogglePlay() }
                )
            } else {
                VideoThumbnail(
                    thumbnailPath = entry.thumbnailPath,
                    filePath = entry.filePath,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(MaterialTheme.shapes.medium)
                        .clickable { onTogglePlay() }
                )
            }
        }

        Column(modifier = Modifier.padding(12.dp)) {
            if (entry.description.isNotBlank()) {
                Text(
                    text = entry.description,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${formatDuration(entry.durationMs)} · ${formatRelativeTime(entry.createdAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row {
                    IconButton(onClick = onShare) {
                        Icon(Icons.Filled.Share, contentDescription = stringResource(R.string.feed_share_cd))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.feed_delete_cd))
                    }
                }
            }
        }
    }
}
