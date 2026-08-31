package mobile.posts

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Exercise 4. The worker, now with its dependency INJECTED (Lab 8).
 *
 * In Lab 7 the worker reached into the global Graph for its repository, because WorkManager, not you,
 * constructs the worker and you could not pass anything in. Hilt fixes that: @HiltWorker plus an
 * @AssistedInject constructor lets Hilt provide the repository while WorkManager still provides the
 * Context and WorkerParameters (the "assisted" arguments). The app's HiltWorkerFactory, wired in
 * PostsApp, is what connects the two. No more global; the worker declares what it needs.
 */
@HiltWorker
class RefreshWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: PostsRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            repository.refresh()
            showSyncNotification(applicationContext)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
