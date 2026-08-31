package mobile.posts

/*
 * Exercise 3. Schedule the background refresh, constraint-aware. See docs/lab7.md, Part 2
 * "Constraints" and "One-time vs periodic", and exercise 3.
 *
 * Write:
 *   object Sync {
 *       fun schedule(context: Context) { ... }
 *   }
 * where schedule():
 *   - builds Constraints requiring NetworkType.CONNECTED (do not sync with no network);
 *   - builds a PeriodicWorkRequestBuilder<RefreshWorker>(6, TimeUnit.HOURS) with those constraints;
 *   - enqueues it with WorkManager.getInstance(context).enqueueUniquePeriodicWork(
 *         "posts-refresh", ExistingPeriodicWorkPolicy.KEEP, request).
 *
 * PostsApp already calls Sync.schedule(this) on startup, so once this compiles the schedule is live.
 * (Add the imports from androidx.work.* and java.util.concurrent.TimeUnit as you type.)
 */
