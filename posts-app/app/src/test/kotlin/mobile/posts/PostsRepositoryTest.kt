package mobile.posts

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

/** Fast JVM tests for the repository, using the fake API (no network). Do not edit the tests. */
class PostsRepositoryTest {

    @Test
    fun `loadPosts maps network DTOs to domain models`() = runTest {
        val api = FakePostsApi().apply {
            result = Result.success(
                listOf(
                    PostDto(id = 1, title = "First", body = "Body one"),
                    PostDto(id = 2, title = "Second", body = "Body two"),
                ),
            )
        }
        val posts = PostsRepository(api).loadPosts()
        assertEquals(listOf(Post(1, "First", "Body one"), Post(2, "Second", "Body two")), posts)
    }
}
