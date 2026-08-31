package mobile.posts

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * The offline-first repository (from Lab 6). Its logic does not change in Lab 8; only its wiring does.
 *
 * Exercise 3: mark the constructor `@Inject constructor(...)` so Hilt can build it, supplying
 * the PostsDao and PostsApi from the module you wrote. (Add the import javax.inject.Inject.)
 */
class PostsRepository(
    private val dao: PostsDao,
    private val api: PostsApi,
) {
    fun observePosts(): Flow<List<Post>> =
        dao.observePosts().map { entities -> entities.map(PostEntity::toDomain) }

    fun observeBookmarkIds(): Flow<Set<Int>> =
        dao.observeBookmarkIds().map { ids -> ids.toSet() }

    suspend fun refresh() {
        dao.upsertPosts(api.getPosts().map(PostDto::toEntity))
    }

    suspend fun setBookmarked(id: Int, bookmarked: Boolean) {
        if (bookmarked) dao.addBookmark(BookmarkEntity(id)) else dao.removeBookmark(id)
    }
}
