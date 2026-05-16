package co.ynd.interview.tomek.feature.feed.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import co.ynd.interview.tomek.core.domain.di.domainModule
import co.ynd.interview.tomek.core.testing.di.testDataModule
import co.ynd.interview.tomek.core.ui.theme.VideoJournalTheme
import co.ynd.interview.tomek.feature.feed.di.feedModule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

class FeedScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setup() {
        startKoin {
            modules(testDataModule, domainModule, feedModule)
        }
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun emptyState_showsPromptText() {
        composeTestRule.setContent {
            VideoJournalTheme {
                FeedScreen(onNavigateToCamera = {})
            }
        }
        composeTestRule.onNodeWithText("No videos yet", substring = true).assertIsDisplayed()
    }

    @Test
    fun emptyState_showsFab() {
        composeTestRule.setContent {
            VideoJournalTheme {
                FeedScreen(onNavigateToCamera = {})
            }
        }
        composeTestRule.onNodeWithContentDescription("Record video").assertIsDisplayed()
    }

    @Test
    fun topBar_showsTitle() {
        composeTestRule.setContent {
            VideoJournalTheme {
                FeedScreen(onNavigateToCamera = {})
            }
        }
        composeTestRule.onNodeWithText("Video Journal").assertIsDisplayed()
    }
}
