package co.ynd.interview.tomek.feature.feed.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import co.ynd.interview.tomek.feature.camera.navigation.CameraDestination
import co.ynd.interview.tomek.feature.feed.ui.FeedScreen

@Composable
fun EntryProviderScope<NavKey>.FeedEntryProvider(backStack: NavBackStack<NavKey>) {
    entry<FeedDestination> {
        FeedScreen(
            onNavigateToCamera = { backStack.add(CameraDestination) }
        )
    }
}
