package mobile.posts

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Exercise 2. The repository, now OFFLINE-FIRST (Lab 6).
 *
 * The big idea: the local database is the single source of truth. The UI observes the database (not
 * the network), so it shows whatever is cached, instantly and offline. The network's only job is to
 * write fresh data INTO the database; when it does, Room's Flow emits and the UI updates on its own.
 *
 * Implement the four members:
 *   - observePosts(): map the DAO's Flow<List<PostEntity>> to Flow<List<Post>> (domain model).
 *   - observeBookmarkIds(): map the DAO's Flow<List<Int>> to Flow<Set<Int>>.
 *   - refresh(): fetch from the api and write the results into the DAO (map each PostDto to a
 *     PostEntity). Do not touch the UI here; the Flow does that.
 *   - setBookmarked(id, bookmarked): add or remove the bookmark in the DAO.
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
