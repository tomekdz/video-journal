package co.ynd.interview.tomek.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import co.ynd.interview.tomek.core.navigation.FeedDestination
import co.ynd.interview.tomek.feature.camera.navigation.CameraEntryProvider
import co.ynd.interview.tomek.feature.feed.navigation.FeedEntryProvider

@Composable
fun MainNavigation() {
    val backStack = rememberNavBackStack(FeedDestination)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {
            FeedEntryProvider(backStack = backStack)
            CameraEntryProvider(backStack = backStack)
        }
    )
}
