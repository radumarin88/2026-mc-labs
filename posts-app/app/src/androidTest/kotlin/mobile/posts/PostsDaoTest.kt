package mobile.posts

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * An INSTRUMENTED test for the real DAO: it runs the actual SQLite queries on a device or emulator
 * using an in-memory Room database, which is the only way to know your `@Query` strings are correct.
 * The repository and ViewModel are tested faster on the JVM with a fake DAO. Do not edit the test.
 * Run: `./gradlew connectedDebugAndroidTest`.
 */
@RunWith(AndroidJUnit4::class)
class PostsDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: PostsDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        dao = db.postsDao()
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun upserting_posts_makes_them_observable() = runBlocking {
        dao.upsertPosts(listOf(PostEntity(1, "A", "a"), PostEntity(2, "B", "b")))
        assertEquals(listOf(1, 2), dao.observePosts().first().map { it.id })

        // upsert replaces by id
        dao.upsertPosts(listOf(PostEntity(1, "A2", "a2")))
        assertEquals("A2", dao.observePosts().first().first { it.id == 1 }.title)
    }

    @Test
    fun bookmarks_can_be_added_and_removed() = runBlocking {
        dao.addBookmark(BookmarkEntity(5))
        dao.addBookmark(BookmarkEntity(5)) // duplicate ignored
        assertEquals(listOf(5), dao.observeBookmarkIds().first())

        dao.removeBookmark(5)
        assertEquals(emptyList<Int>(), dao.observeBookmarkIds().first())
    }
}
