package mobile.posts

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/**
 * Exercise 2. The background job: refresh the database from the network.
 *
 * A [CoroutineWorker] is a unit of deferrable, guaranteed background work whose [doWork] is a suspend
 * function (so it can call the repository directly). WorkManager runs it on a background thread, when
 * its constraints are met, and retries it if it fails. It does not talk to the UI at all: it just
 * writes fresh posts into Room, and the screen (which observes Room) updates itself.
 *
 * Implement [doWork]: try the repository's refresh() and return Result.success(); if it throws (for
 * example the network is momentarily down), return Result.retry() so WorkManager tries again later
 * with backoff. The repository comes from the app's Graph, exactly as the ViewModel gets it.
 */
class RefreshWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            Graph.repository.refresh()
            showSyncNotification(applicationContext)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
