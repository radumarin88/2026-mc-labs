package mobile.posts

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/** A fake API that never touches the network, so tests are fast and deterministic. Provided. */
class FakePostsApi : PostsApi {
    var result: Result<List<PostDto>> = Result.success(emptyList())
    override suspend fun getPosts(): List<PostDto> = result.getOrThrow()
}

/**
 * An in-memory fake of the DAO, so the repository and ViewModel can be tested on the JVM without a
 * real database. It behaves like Room: writes update the backing state and the observe-flows emit.
 * The real DAO's SQL is checked separately by the instrumented PostsDaoTest. Provided.
 */
class FakePostsDao : PostsDao {
    private val posts = MutableStateFlow<List<PostEntity>>(emptyList())
    private val bookmarks = MutableStateFlow<Set<Int>>(emptySet())

    override fun observePosts(): Flow<List<PostEntity>> = posts

    override suspend fun upsertPosts(posts: List<PostEntity>) {
        val byId = this.posts.value.associateBy { it.id }.toMutableMap()
        posts.forEach { byId[it.id] = it }
        this.posts.value = byId.values.toList()
    }

    override fun observeBookmarkIds(): Flow<List<Int>> = bookmarks.map { it.toList() }

    override suspend fun addBookmark(bookmark: BookmarkEntity) {
        bookmarks.update { it + bookmark.id }
    }

    override suspend fun removeBookmark(id: Int) {
        bookmarks.update { it - id }
    }
}

/** Swaps Dispatchers.Main for a test dispatcher so viewModelScope coroutines run under the test. */
class MainDispatcherRule : TestWatcher() {
    val dispatcher = StandardTestDispatcher()
    override fun starting(description: Description) = Dispatchers.setMain(dispatcher)
    override fun finished(description: Description) = Dispatchers.resetMain()
}
