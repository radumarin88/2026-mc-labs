package mobile.posts

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Fast JVM tests for the offline-first repository, using a fake API and a fake DAO (no network, no
 * real database). Run: `./gradlew test`. Do not edit the tests.
 */
class PostsRepositoryTest {

    private val sample = listOf(PostDto(1, "First", "a"), PostDto(2, "Second", "b"))

    @Test
    fun `refresh writes fetched posts into the database`() = runTest {
        val dao = FakePostsDao()
        val repository = PostsRepository(dao, FakePostsApi().apply { result = Result.success(sample) })

        repository.refresh()

        val posts = repository.observePosts().first()
        assertEquals(listOf(Post(1, "First", "a"), Post(2, "Second", "b")), posts)
    }

    @Test
    fun `observePosts maps entities to the domain model`() = runTest {
        val dao = FakePostsDao().apply { upsertPosts(listOf(PostEntity(9, "T", "x"))) }
        val repository = PostsRepository(dao, FakePostsApi())

        assertEquals(listOf(Post(9, "T", "x")), repository.observePosts().first())
    }

    @Test
    fun `setBookmarked adds and removes bookmarks`() = runTest {
        val dao = FakePostsDao()
        val repository = PostsRepository(dao, FakePostsApi())

        repository.setBookmarked(3, bookmarked = true)
        assertEquals(setOf(3), repository.observeBookmarkIds().first())

        repository.setBookmarked(3, bookmarked = false)
        assertEquals(emptySet<Int>(), repository.observeBookmarkIds().first())
    }
}
