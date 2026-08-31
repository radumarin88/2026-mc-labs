package mobile.posts

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Exercise 3. Schedule the background refresh, the right way for a phone.
 *
 * The whole point of WorkManager is CONSTRAINT-AWARE work: you describe WHEN it is acceptable to run
 * (here: only when there is a network) and let the system pick the moment, batching it with other
 * apps' work to save battery. A PeriodicWorkRequest repeats; enqueueUniquePeriodicWork with KEEP
 * makes sure only one schedule exists even if the app starts many times.
 *
 * Implement [schedule]:
 *   - build Constraints requiring NetworkType.CONNECTED (do not sync with no network);
 *   - build a PeriodicWorkRequest for RefreshWorker every 6 hours with those constraints;
 *   - enqueue it uniquely under the name "posts-refresh" with ExistingPeriodicWorkPolicy.KEEP.
 */
object Sync {
    private const val UNIQUE_NAME = "posts-refresh"

    fun schedule(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = PeriodicWorkRequestBuilder<RefreshWorker>(6, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }
}
