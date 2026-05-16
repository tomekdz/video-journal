package co.ynd.interview.tomek.feature.feed.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.ynd.interview.tomek.core.domain.model.VideoEntry
import co.ynd.interview.tomek.core.ui.component.ErrorMessage
import co.ynd.interview.tomek.core.ui.component.LoadingIndicator
import co.ynd.interview.tomek.feature.feed.ui.component.VideoEntryCard
import org.koin.androidx.compose.koinViewModel

@Composable
fun FeedScreen(
    onNavigateToCamera: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FeedViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is FeedUiState.Loading -> LoadingIndicator(modifier)
        is FeedUiState.Error -> ErrorMessage(
            message = state.throwable.message ?: "Unknown error",
            modifier = modifier
        )
        is FeedUiState.Success -> FeedContent(
            entries = state.entries,
            onNavigateToCamera = onNavigateToCamera,
            onDeleteEntry = viewModel::deleteEntry,
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FeedContent(
    entries: List<VideoEntry>,
    onNavigateToCamera: () -> Unit,
    onDeleteEntry: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var playingVideoId by remember { mutableStateOf<Long?>(null) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(title = { Text("Video Journal") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToCamera) {
                Icon(Icons.Filled.Videocam, contentDescription = "Record video")
            }
        }
    ) { paddingValues ->
        if (entries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No videos yet. Tap the camera button to record one!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                modifier = Modifier.padding(paddingValues)
            ) {
                items(entries, key = { it.id }) { entry ->
                    VideoEntryCard(
                        entry = entry,
                        isPlaying = playingVideoId == entry.id,
                        onTogglePlay = {
                            playingVideoId = if (playingVideoId == entry.id) null else entry.id
                        },
                        onDelete = { onDeleteEntry(entry.id) },
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .animateItem()
                    )
                }
            }
        }
    }
}
