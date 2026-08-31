package mobile.posts

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/**
 * Exercise 5. Inject the worker's dependency (Lab 8). See docs/lab8.md, Part 2 "Workers", and exercise 5.
 *
 * In Lab 7 this worker reached into the global `Graph` for its repository. `Graph` is gone. The snag:
 * WorkManager is what CONSTRUCTS the worker, so you cannot just add the repository to a normal
 * constructor. Hilt's answer is ASSISTED INJECTION: WorkManager supplies the runtime values (the
 * `Context` and `WorkerParameters`), Hilt supplies the repository.
 *
 * Work out, from Part 2 and the Hilt-WorkManager docs, which annotations go on the class and the
 * constructor, which parameters are "assisted" (WorkManager's) and which Hilt provides. Then use the
 * injected repository in `doWork()`: try `refresh()` then `showSyncNotification(applicationContext)`
 * and return `Result.success()`; on failure return `Result.retry()`. The `HiltWorkerFactory` wiring in
 * `PostsApp` is already provided.
 */
class RefreshWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = TODO()
}
