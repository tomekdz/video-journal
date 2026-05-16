package co.ynd.interview.tomek.test.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import co.ynd.interview.tomek.core.testing.di.testDataModule
import co.ynd.interview.tomek.ui.MainActivity
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
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
}
