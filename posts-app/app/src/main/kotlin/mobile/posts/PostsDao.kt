package mobile.posts

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/**
 * Exercise 1. The Data Access Object: the queries Room turns into real SQLite calls.
 *
 * Note two things. The read queries return a [Flow]: Room re-runs them and emits a new list every
 * time the underlying table changes, so the UI updates itself when data is written. The writes are
 * `suspend`, because touching the disk must happen off the main thread. Add the annotations and query
 * strings for each function.
 */
@Dao
interface PostsDao {

    /** Observe every cached post. Emits again whenever the posts table changes. */
    @Query("SELECT * FROM posts")
    fun observePosts(): Flow<List<PostEntity>>

    /** Insert or update the given posts (upsert = insert new rows, replace existing ones by id). */
    @Upsert
    suspend fun upsertPosts(posts: List<PostEntity>)

    /** Observe the ids of the bookmarked posts. Emits again whenever the bookmarks table changes. */
    @Query("SELECT id FROM bookmarks")
    fun observeBookmarkIds(): Flow<List<Int>>

    /** Add a bookmark; do nothing if it is already there. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addBookmark(bookmark: BookmarkEntity)

    /** Remove the bookmark for the given post id. */
    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun removeBookmark(id: Int)
}
