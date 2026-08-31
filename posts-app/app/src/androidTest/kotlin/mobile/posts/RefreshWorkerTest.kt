package mobile.posts

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * An INSTRUMENTED test for the worker, using WorkManager's TestListenableWorkerBuilder to run doWork()
 * directly (no scheduling, no waiting). Now that the worker's repository is a constructor dependency
 * (Lab 8), we build it with a WorkerFactory that supplies a fake-backed repository, no global needed.
 * Do not edit the test. Run: `./gradlew connectedDebugAndroidTest`.
 */
@RunWith(AndroidJUnit4::class)
class RefreshWorkerTest {

    private lateinit var db: AppDatabase
    private val context: Context get() = ApplicationProvider.getApplicationContext()

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
    }

    @After
    fun tearDown() = db.close()

    private class FakeApi(val result: Result<List<PostDto>>) : PostsApi {
        override suspend fun getPosts(): List<PostDto> = result.getOrThrow()
    }

    /** A factory that injects the given repository into the worker (what HiltWorkerFactory does in production). */
    private fun factoryWith(repository: PostsRepository) = object : WorkerFactory() {
        override fun createWorker(
            appContext: Context,
            workerClassName: String,
            workerParameters: WorkerParameters,
        ): ListenableWorker = RefreshWorker(appContext, workerParameters, repository)
    }

    @Test
    fun a_successful_sync_writes_to_the_database_and_returns_success() = runBlocking {
        val repository = PostsRepository(
            dao = db.postsDao(),
            api = FakeApi(Result.success(listOf(PostDto(1, "A", "a"), PostDto(2, "B", "b")))),
        )
        val worker = TestListenableWorkerBuilder<RefreshWorker>(context)
            .setWorkerFactory(factoryWith(repository))
            .build()

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        assertEquals(2, db.postsDao().observePosts().first().size)
    }

    @Test
    fun a_failing_sync_asks_for_retry() = runBlocking {
        val repository = PostsRepository(
            dao = db.postsDao(),
            api = FakeApi(Result.failure(RuntimeException("network down"))),
        )
        val worker = TestListenableWorkerBuilder<RefreshWorker>(context)
            .setWorkerFactory(factoryWith(repository))
            .build()

        assertEquals(ListenableWorker.Result.retry(), worker.doWork())
    }
}
