package mobile.posts

/*
 * Exercise 3. The DAO: your queries as annotated methods. See docs/lab6.md, Part 2 "The DAO" and
 * exercise 3.
 *
 * Write a `@Dao interface PostsDao` with these methods (match the signatures; the repository and the
 * fake DAO in the tests rely on them):
 *
 *   - observePosts(): Flow<List<PostEntity>>
 *       @Query selecting every row from the posts table. A Flow, so it re-emits on every change.
 *   - upsertPosts(posts: List<PostEntity>)   // suspend
 *       @Upsert: insert new rows, replace existing ones by primary key.
 *   - observeBookmarkIds(): Flow<List<Int>>
 *       @Query selecting the id column from the bookmarks table.
 *   - addBookmark(bookmark: BookmarkEntity)  // suspend
 *       @Insert with onConflict = OnConflictStrategy.IGNORE.
 *   - removeBookmark(id: Int)                // suspend
 *       @Query deleting the bookmarks row whose id equals the bound :id.
 *
 * Reads are Flow (reactive); writes are suspend (off the main thread). Add the imports as you type
 * (androidx.room.*, kotlinx.coroutines.flow.Flow).
 */
