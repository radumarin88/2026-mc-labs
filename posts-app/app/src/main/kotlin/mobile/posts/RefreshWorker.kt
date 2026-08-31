package mobile.posts

/*
 * Exercise 2. The background job: refresh the database from the network. See docs/lab7.md, Part 2
 * "A worker" and exercise 2.
 *
 * Write:
 *   class RefreshWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params)
 * and implement doWork():
 *   - try Graph.repository.refresh(), then return Result.success();
 *   - on an exception, return Result.retry() (WorkManager will try again later, with backoff).
 * The worker writes to Room and returns; it never touches the UI.
 *
 * (Add the imports: android.content.Context, androidx.work.CoroutineWorker, androidx.work.WorkerParameters.)
 */
