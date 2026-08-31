package mobile.catalog

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * INSTRUMENTED tests: they run on a device or emulator (the ▶ next to the class, or
 * `./gradlew connectedDebugAndroidTest`) using the real Android framework, unlike the JVM-only
 * unit tests from Lab 1. Do not edit the tests: edit Exercises.kt until they pass.
 */
@RunWith(AndroidJUnit4::class)
class CatalogNavigationTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private fun setApp() = rule.setContent { AppTheme { CatalogNavHost() } }

    @Test
    fun list_shows_items_and_tapping_one_opens_its_detail() {
        setApp()
        val first = SampleItems.all.first()
        rule.onNodeWithText(first.name).assertIsDisplayed()
        rule.onNodeWithText(first.name).performClick()
        // detail shows the full description
        rule.onNodeWithText(first.description).assertIsDisplayed()
    }

    @Test
    fun typing_in_search_filters_the_list() {
        setApp()
        // "Mesh Relay" is present, "Edge Node" is present, before filtering.
        rule.onNodeWithText("Edge Node").assertIsDisplayed()
        rule.onNode(hasSetTextAction()).performTextInput("Relay")
        // now only the matching item remains
        rule.onNodeWithText("Mesh Relay").assertIsDisplayed()
        rule.onNodeWithText("Edge Node").assertDoesNotExist()
    }

    @Test
    fun a_search_with_no_matches_shows_the_empty_state() {
        setApp()
        rule.onNode(hasSetTextAction()).performTextInput("zzzzz")
        rule.onNodeWithText("No matching items").assertIsDisplayed()
    }
}
