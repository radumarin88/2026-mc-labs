package mobile.posts

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Fast JVM tests for the ViewModel, with a fake API and a fake DAO and virtual time (no device, no
 * network, no real database). Do not edit the tests.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PostsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sample = listOf(
        PostDto(1, "Kotlin coroutines", "structured concurrency"),
        PostDto(2, "Room database", "on-device source of truth"),
    )

    private fun viewModel(
        api: FakePostsApi = FakePostsApi().apply { result = Result.success(sample) },
        dao: FakePostsDao = FakePostsDao(),
    ) = PostsViewModel(PostsRepository(dao, api))

    @Test
    fun `refresh loads posts from the network into the database and the state observes them`() = runTest {
        val vm = viewModel()
        advanceUntilIdle()
        assertEquals(listOf(1, 2), vm.uiState.value.posts.map { it.id })
        assertEquals(false, vm.uiState.value.isLoading)
    }

    @Test
    fun `a failing refresh sets an error message`() = runTest {
        val vm = viewModel(api = FakePostsApi().apply { result = Result.failure(RuntimeException("no network")) })
        advanceUntilIdle()
        assertEquals("no network", vm.uiState.value.errorMessage)
    }

    @Test
    fun `cached posts are shown even when the refresh fails`() = runTest {
        // the database already has a cached post; the network is down
        val dao = FakePostsDao().apply { upsertPosts(listOf(PostEntity(7, "Cached", "offline"))) }
        val vm = viewModel(api = FakePostsApi().apply { result = Result.failure(RuntimeException("down")) }, dao = dao)
        advanceUntilIdle()
        assertEquals(listOf(7), vm.uiState.value.posts.map { it.id })  // offline-first: cache still shows
        assertEquals("down", vm.uiState.value.errorMessage)
    }

    @Test
    fun `toggleBookmark persists through the database and back into the state`() = runTest {
        val vm = viewModel()
        advanceUntilIdle()
        vm.toggleBookmark(1)
        advanceUntilIdle()
        assertTrue(1 in vm.uiState.value.bookmarkedIds)
        vm.toggleBookmark(1)
        advanceUntilIdle()
        assertTrue(1 !in vm.uiState.value.bookmarkedIds)
    }

    @Test
    fun `search filters the visible posts by title`() = runTest {
        val vm = viewModel()
        advanceUntilIdle()
        vm.setQuery("room")
        assertEquals(listOf(2), vm.uiState.value.visiblePosts.map { it.id })
    }
}
