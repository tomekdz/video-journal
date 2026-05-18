package co.ynd.interview.tomek.feature.feed.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.ynd.interview.tomek.core.domain.model.VideoEntry
import co.ynd.interview.tomek.core.ui.component.ErrorMessage
import co.ynd.interview.tomek.core.ui.component.LoadingIndicator
import co.ynd.interview.tomek.feature.feed.R
import co.ynd.interview.tomek.feature.feed.ui.component.VideoEntryCard
import co.ynd.interview.tomek.feature.feed.util.shareVideo
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun FeedScreen(
    onNavigateToCamera: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FeedViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val deleteFailedMessage = stringResource(R.string.feed_delete_failed)

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                FeedEvent.DeleteFailed -> snackbarHostState.showSnackbar(deleteFailedMessage)
            }
        }
    }

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
            snackbarHostState = snackbarHostState,
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
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var playingVideoId by remember { mutableStateOf<Long?>(null) }
    var pendingDeleteId by remember { mutableStateOf<Long?>(null) }

    val shareMissingMessage = stringResource(R.string.feed_share_missing)
    val confirmDeleteCd = stringResource(R.string.feed_delete_confirm_cd)

    pendingDeleteId?.let { idToDelete ->
        AlertDialog(
            onDismissRequest = { pendingDeleteId = null },
            title = { Text(stringResource(R.string.feed_delete_confirm_title)) },
            text = { Text(stringResource(R.string.feed_delete_confirm_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteEntry(idToDelete)
                        pendingDeleteId = null
                    },
                    modifier = Modifier.semantics { contentDescription = confirmDeleteCd }
                ) {
                    Text(stringResource(R.string.feed_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteId = null }) {
                    Text(stringResource(R.string.feed_cancel))
                }
            }
        )
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.feed_title)) })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToCamera) {
                Icon(Icons.Filled.Videocam, contentDescription = stringResource(R.string.feed_record_video_cd))
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
                    text = stringResource(R.string.feed_empty),
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
                        onShare = {
                            val shared = shareVideo(context, entry.filePath)
                            if (!shared) {
                                coroutineScope.launch { snackbarHostState.showSnackbar(shareMissingMessage) }
                            }
                        },
                        onDelete = { pendingDeleteId = entry.id },
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .animateItem()
                    )
                }
            }
        }
    }
}
