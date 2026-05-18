package co.ynd.interview.tomek.test.app

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import co.ynd.interview.tomek.core.domain.repository.VideoEntryRepository
import co.ynd.interview.tomek.core.testing.di.testDataModule
import co.ynd.interview.tomek.ui.MainActivity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.GlobalContext
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules

@RunWith(AndroidJUnit4::class)
class AppTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        loadKoinModules(testDataModule)
    }

    @After
    fun tearDown() {
        unloadKoinModules(testDataModule)
    }

    @Test
    fun feedScreen_isDisplayed() {
        composeTestRule.onNodeWithText("Video Journal").assertIsDisplayed()
    }

    @Test
    fun fab_isDisplayed() {
        composeTestRule.onNodeWithContentDescription("Record video").assertIsDisplayed()
    }

    @Test
    fun feedScreen_showsSeededEntry() {
        val repo = GlobalContext.get().get<VideoEntryRepository>()
        runBlocking { repo.add("/fake/seeded.mp4", "Seeded Entry", 5000L, null) }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Seeded Entry").assertIsDisplayed()
    }

    @Test
    fun feedScreen_deleteRemovesEntry() {
        val repo = GlobalContext.get().get<VideoEntryRepository>()
        runBlocking { repo.add("/fake/deletable.mp4", "Deletable Entry", 3000L, null) }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Deletable Entry").assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription("Delete").performClick()
        composeTestRule.onNodeWithContentDescription("Confirm delete").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onAllNodesWithText("Deletable Entry").assertCountEquals(0)
    }
}
