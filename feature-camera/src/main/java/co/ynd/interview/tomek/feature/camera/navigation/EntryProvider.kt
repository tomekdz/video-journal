package co.ynd.interview.tomek.feature.camera.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import co.ynd.interview.tomek.core.navigation.CameraDestination
import co.ynd.interview.tomek.feature.camera.ui.CameraScreen

@Composable
fun EntryProviderScope<NavKey>.CameraEntryProvider(backStack: NavBackStack<NavKey>) {
    entry<CameraDestination> {
        CameraScreen(
            onNavigateBack = { backStack.removeLastOrNull() }
        )
    }
}
