package mobile.posts

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Fast JVM tests for the ViewModel. They run on your computer in milliseconds with no device,
 * because the logic has no Android or UI in it. That is the payoff of moving logic out of
 * composables. Run: `./gradlew test`. Do not edit the tests.
 */
class PostsViewModelTest {

    @Test
    fun `initial state shows the seed posts with no bookmarks and no filter`() {
        val vm = PostsViewModel()
        val state = vm.uiState.value
        assertEquals(seedPosts, state.posts)
        assertEquals(0, state.bookmarkCount)
        assertFalse(state.showBookmarkedOnly)
        assertEquals(seedPosts, state.visiblePosts)
    }

    @Test
    fun `search filters the visible posts by title`() {
        val vm = PostsViewModel()
        vm.setQuery("room")
        assertEquals(listOf(2), vm.uiState.value.visiblePosts.map { it.id })
    }

    @Test
    fun `bookmarking is tracked and the count updates`() {
        val vm = PostsViewModel()
        vm.toggleBookmark(1)
        vm.toggleBookmark(3)
        assertEquals(2, vm.uiState.value.bookmarkCount)
        assertTrue(1 in vm.uiState.value.bookmarkedIds)
        vm.toggleBookmark(1) // toggle off
        assertEquals(1, vm.uiState.value.bookmarkCount)
        assertFalse(1 in vm.uiState.value.bookmarkedIds)
    }

    @Test
    fun `bookmarked-only filter shows only saved posts`() {
        val vm = PostsViewModel()
        vm.toggleBookmark(2)
        vm.setShowBookmarkedOnly(true)
        assertEquals(listOf(2), vm.uiState.value.visiblePosts.map { it.id })
    }

    @Test
    fun `search and bookmark filter combine`() {
        val vm = PostsViewModel()
        vm.toggleBookmark(1)
        vm.toggleBookmark(2)
        vm.setShowBookmarkedOnly(true)
        vm.setQuery("kotlin")
        // only bookmarked posts whose title contains "kotlin" -> post 1
        assertEquals(listOf(1), vm.uiState.value.visiblePosts.map { it.id })
    }

    @Test
    fun `state transitions do not mutate the previous state`() {
        val vm = PostsViewModel()
        val first = vm.uiState.value
        vm.toggleBookmark(1)
        assertEquals(0, first.bookmarkCount)               // old snapshot untouched
        assertEquals(1, vm.uiState.value.bookmarkCount)
    }
}
