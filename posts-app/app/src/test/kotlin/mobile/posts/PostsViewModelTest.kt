package mobile.posts

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Fast JVM tests for the ViewModel, with a fake API and virtual time (no device, no real network).
 * Do not edit the tests.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PostsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sample = listOf(
        PostDto(1, "Kotlin coroutines", "structured concurrency"),
        PostDto(2, "Room database", "on-device source of truth"),
        PostDto(3, "WorkManager", "constraint-aware background work"),
    )

    private fun viewModel(api: FakePostsApi) = PostsViewModel(PostsRepository(api))

    @Test
    fun `starts loading, then ends in success with the fetched posts`() = runTest {
        val api = FakePostsApi().apply { result = Result.success(sample) }
        val vm = viewModel(api)
        assertTrue(vm.uiState.value.isLoading)          // init launched refresh, not yet run
        advanceUntilIdle()
        assertFalse(vm.uiState.value.isLoading)
        assertEquals(3, vm.uiState.value.posts.size)
    }

    @Test
    fun `a failing load ends with an error message`() = runTest {
        val api = FakePostsApi().apply { result = Result.failure(RuntimeException("no network")) }
        val vm = viewModel(api)
        advanceUntilIdle()
        assertEquals("no network", vm.uiState.value.errorMessage)
    }

    @Test
    fun `retry after a failure can succeed`() = runTest {
        val api = FakePostsApi().apply { result = Result.failure(RuntimeException("down")) }
        val vm = viewModel(api)
        advanceUntilIdle()
        assertEquals("down", vm.uiState.value.errorMessage)

        api.result = Result.success(sample)
        vm.refresh()
        advanceUntilIdle()
        assertEquals(3, vm.uiState.value.posts.size)
        assertEquals(null, vm.uiState.value.errorMessage)
    }

    @Test
    fun `search filters the visible posts by title`() = runTest {
        val vm = viewModel(FakePostsApi().apply { result = Result.success(sample) })
        advanceUntilIdle()
        vm.setQuery("room")
        assertEquals(listOf(2), vm.uiState.value.visiblePosts.map { it.id })
    }

    @Test
    fun `bookmarking is tracked and the count updates`() = runTest {
        val vm = viewModel(FakePostsApi().apply { result = Result.success(sample) })
        advanceUntilIdle()
        vm.toggleBookmark(1)
        vm.toggleBookmark(3)
        assertEquals(2, vm.uiState.value.bookmarkCount)
        vm.toggleBookmark(1) // toggle off
        assertEquals(1, vm.uiState.value.bookmarkCount)
    }

    @Test
    fun `bookmarked-only filter shows only saved posts`() = runTest {
        val vm = viewModel(FakePostsApi().apply { result = Result.success(sample) })
        advanceUntilIdle()
        vm.toggleBookmark(2)
        vm.setShowBookmarkedOnly(true)
        assertEquals(listOf(2), vm.uiState.value.visiblePosts.map { it.id })
    }

    @Test
    fun `bookmarks survive a refresh`() = runTest {
        val api = FakePostsApi().apply { result = Result.success(sample) }
        val vm = viewModel(api)
        advanceUntilIdle()
        vm.toggleBookmark(2)
        vm.refresh()
        advanceUntilIdle()
        assertTrue(2 in vm.uiState.value.bookmarkedIds)
    }
}
