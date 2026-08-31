# Lab 7: Offline-first with WorkManager

Your app now reads from a local database and refreshes it whenever a screen opens. But it only refreshes while someone is looking at it. A truly offline-first app keeps its data fresh **in the background**, on its own, so that whenever the user opens it, even offline, what they see is recent. This lab adds that with **WorkManager**: a scheduled, constraint-aware background sync that tops up the database when conditions are right.

This is the systems heart of the course. Everything you have built leads here: Room is the source of truth (Lab 6), the network fills it (Lab 5), the UI observes it (Lab 4), and now a background worker keeps it current without anyone asking. You extend the same `posts-app`; the screen does not change at all, which is the point.

Time: about two and a half hours.

---

## Part 1: Before you start

### What is new since Lab 6
Nothing yet: there is no background work and no WorkManager dependency. You add them. When you are done, the app will schedule a periodic refresh that runs only on a network, writes fresh posts into Room, and lets the UI update itself, with no code in the UI aware that a sync happened.

### Open the project and run the tests
As in Lab 6, the project does **not compile** at first: the provided `PostsApp` and the worker test refer to a `RefreshWorker` and a `Sync` scheduler you have not written yet. You make it compile and go green by doing the three exercises. Then:

```bash
./gradlew test                       # the Room repository/ViewModel tests, on the JVM
./gradlew connectedDebugAndroidTest  # the DAO test and the new worker test, on a device/emulator
```

Pinned tooling (verified November 2026): you will add **WorkManager 2.12.0** (`androidx.work:work-runtime`), plus `androidx.work:work-testing` for the test.

### The one production concern: DEFER WORK TO WHEN CONDITIONS ARE RIGHT

A phone is not a server. It is battery-powered, often on a metered or absent network, and asleep most of the time. So background work on a phone must be a negotiation with the system, not a demand:

- You do not say "sync now, on this thread." You say "sync sometime soon, **but only when there is a network**, and feel free to batch it with other apps' work." The system picks the moment.
- The work must **survive**: if the process is killed or the device reboots before it runs, it should still run later. A plain background thread cannot promise that; WorkManager can.
- And it must **back off** on failure, not hammer a dead network.

Getting data on screen was Labs 4 to 6. Deciding *when* it is acceptable to spend the user's battery and data to refresh it is this lab, and it is exactly the kind of resource-aware judgement that separates a mobile app from a desktop one.

---

## Part 2: What you need to know

### What WorkManager is, and why not just a thread

You might reach for a background `Thread`, a coroutine on an app scope, or an old `Service`, or `AlarmManager`. They all fail the phone's constraints in some way: a thread or app-scope coroutine dies when the process does (and Android kills backgrounded processes freely); a `Service` fights the background-execution limits added in recent Android; `AlarmManager` wakes the device but knows nothing about network or battery. None of them survive a reboot.

**WorkManager** is the official answer for **deferrable, guaranteed background work**. "Deferrable" means it does not need to run this instant (a sync, an upload, a cleanup, as opposed to something the user is waiting on). "Guaranteed" means WorkManager persists the request in its own small database and will run it eventually even across process death and reboots, honouring the constraints you set. Under the hood it uses the right OS mechanism for the device and Android version (JobScheduler and friends); you just describe the work and the conditions.

### A worker: the unit of work

You describe the work as a **Worker**. The coroutine-friendly kind is a **`CoroutineWorker`**, whose one method, `doWork()`, is a `suspend` function, so it can call your repository directly and run on a background dispatcher. It returns a **`Result`**:

- **`Result.success()`**: done.
- **`Result.retry()`**: it failed transiently (network blip); WorkManager should try again later, with **backoff** (increasing delays, so you do not hammer a dead server).
- **`Result.failure()`**: it failed permanently (bad data); do not retry.

A worker must not touch the UI. Ours does the offline-first thing: it asks the repository to refresh, which writes to Room, and that is all. The screen, observing Room, updates itself.

```kotlin
class ExampleWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = try {
        repository.refresh()   // writes to the database
        Result.success()
    } catch (e: Exception) {
        Result.retry()
    }
}
```

### `retry` versus `failure`: which to return

`success` is obvious; the real judgement is between the two failures, and it comes down to one question: **could running this again, unchanged, plausibly succeed?**

- Return **`retry`** when the failure is **transient**, caused by the environment rather than the request: a network timeout or "no route to host", a `5xx` server error, a `429` (rate limited), a momentarily busy disk. Trying again later is exactly the right move, and WorkManager obliges with **backoff**, an increasing delay between attempts (default exponential, starting at 30 seconds) so you do not hammer a struggling server.
- Return **`failure`** when the failure is **permanent**, where a retry would waste battery for nothing: a `400`/`404` (the request is malformed or the resource is gone), corrupt or unparseable input, a bug in your own code. Give up cleanly. And, as you will see next, in a *chain* a `failure` stops everything downstream, which is usually what you want when a prerequisite step cannot be done.

There is a middle case: something that is transient but might never recover (a server that stays down for days). Retrying forever is not free. WorkManager gives each worker a `runAttemptCount`, so you can cap it:

```kotlin
} catch (e: Exception) {
    if (runAttemptCount >= 3) Result.failure() else Result.retry()
}
```

Our `RefreshWorker` keeps it simple and retries on any exception, which is reasonable for a *periodic* refresh (if this run gives up, the next period tries again anyway). A stricter version would distinguish an `IOException` (retry) from an HTTP `4xx` (fail); that is a good extension.

**Docs:** [Result reference](https://developer.android.com/reference/androidx/work/ListenableWorker.Result) · [Retries and backoff](https://developer.android.com/develop/background-work/background-tasks/persistent/how-to/manage-work#retries_backoff)

### Constraints: the "when"

A **`Constraints`** object describes the conditions under which the work may run. WorkManager holds the work until they are all met. The common ones:

- `setRequiredNetworkType(NetworkType.CONNECTED)` (or `UNMETERED` to avoid mobile data),
- `setRequiresBatteryNotLow(true)`,
- `setRequiresCharging(true)`,
- `setRequiresDeviceIdle(true)`.

For a periodic post refresh, "only when there is a network" is the essential one: syncing with no network is pointless, and you never want to try. This is the whole spirit of the lab, so notice how little you have to say to get correct battery-and-data behaviour: you declare the condition, the system enforces it.

### One-time vs periodic, and unique work

A **`OneTimeWorkRequest`** runs once (an upload after a tap). A **`PeriodicWorkRequest`** repeats on an interval (minimum 15 minutes; the exact time is the system's choice, batched with other work). For a background refresh you want periodic.

If your app starts many times, you do not want many overlapping schedules. **`enqueueUniquePeriodicWork(name, policy, request)`** keeps a single schedule under a name; the policy `KEEP` means "if one already exists, leave it alone," which is what you want when you schedule from `Application.onCreate()`.

### Chaining workers

Real background jobs often have ordered steps: compress an image, then upload it, then delete the temp file; or download a file, then parse it, then save it. You do not cram those into one giant worker; you make each a small worker and **chain** them, so each is independently retryable and the sequence is explicit:

```kotlin
WorkManager.getInstance(context)
    .beginWith(compressRequest)   // step 1
    .then(uploadRequest)          // step 2, runs only if step 1 succeeded
    .then(cleanupRequest)         // step 3
    .enqueue()
```

Two rules make chains powerful:

- **Order follows success.** A step runs only after its predecessor returns `success`. This is where `retry` versus `failure` really bites: a `retry` pauses the chain and re-attempts that step; a `failure` **stops the chain** and the downstream work never runs. So "should the rest of the job proceed?" is another way to decide which Result to return.
- **Data flows down the chain.** A worker can return `Result.success(outputData)`, and that becomes the next worker's `inputData`. You can also run steps in parallel and join them: `beginWith(listOf(a, b)).then(c)` runs `a` and `b` together and `c` after both (an `InputMerger` combines their outputs). Unique chains exist too, via `beginUniqueWork(name, policy, work)`.

This app has a single job (refresh), so it needs no chain. But the moment a background task has more than one step, chaining is how you keep each step small, retryable, and observable instead of one opaque worker that either wholly succeeds or wholly fails.

**Docs:** [Chain work](https://developer.android.com/develop/background-work/background-tasks/persistent/how-to/chain-work)

### How this closes the offline-first loop

Trace the whole thing once, because this is the payoff of the last four labs:

```
              (periodic, network-constrained)
WorkManager --------> RefreshWorker --> repository.refresh() --> [ Room ] --Flow--> UI
   ^                                                               ^
   |__ persists the schedule across reboots           the UI observes Room, so it
                                                       updates with no idea a sync ran
```

The UI has no code about syncing. The worker has no code about the UI. They meet only at the database, the single source of truth. That decoupling is why you can add, change, or remove background sync without touching a single composable, and it is why the app already works offline: it was never reading the network in the first place.

### A note on Doze and App Standby

Android aggressively saves battery, and two mechanisms shape when your background work actually runs:

- **Doze mode.** When the device is unplugged and stationary with the screen off for a while, it enters Doze: the system suspends most background activity and only opens periodic **maintenance windows** (which grow further apart the longer the phone stays idle) in which deferred work, including your WorkManager jobs, is allowed to run in a batch. So a phone left on a desk overnight does not run your sync every 6 hours on the dot; it runs it in the next maintenance window.
- **App Standby buckets.** The system sorts apps into buckets (active, working set, frequent, rare, restricted) based on how the user uses them, and the rarer the bucket, the more it throttles that app's background work and jobs. An app the user rarely opens gets its syncs deferred much harder.

The point is not to fight this. WorkManager is built to **cooperate** with Doze and Standby, which is exactly why you use it instead of `AlarmManager` wake-ups or a foreground service: it lets the system batch your work with everyone else's at a good moment for the battery. Your periodic sync therefore runs at a *reasonable* time the system chooses, not a precise one. Designing for "recent enough", not "this instant", is the mobile mindset, and it is why the UI reads the cache rather than waiting on a sync.

### Where the worker gets its repository

WorkManager constructs the worker for you, so you cannot pass it a repository in a constructor (not until Lab 8's dependency injection, which has a Hilt worker factory). For now the worker reads `Graph.repository`, the same hand-wired container the ViewModel uses, set up in `PostsApp`. The provided test swaps `Graph.repository` for a fake-backed one to run the worker without a real network.

---

## Part 3: The exercises

Do them in order. The app does not compile until all three are done, because `PostsApp` schedules the sync and the test drives the worker.

### 1 · Add the WorkManager dependency

- In `gradle/libs.versions.toml`: add a version `work = "2.12.0"`; a library `androidx-work-runtime` (`androidx.work:work-runtime`, `version.ref = "work"`); and a test library `androidx-work-testing` (`androidx.work:work-testing`, same version).
- In `app/build.gradle.kts`: add `implementation(libs.androidx.work.runtime)` and `androidTestImplementation(libs.androidx.work.testing)`.

Sync Gradle. (WorkManager initialises itself; you do not need any manifest setup for the basic case.)

**Docs:** [Getting started with WorkManager](https://developer.android.com/develop/background-work/background-tasks/persistent/getting-started)

### 2 · Write the worker: `RefreshWorker.kt`

Write a `class RefreshWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params)` and implement `doWork()`: `try` `Graph.repository.refresh()`, then **`showSyncNotification(applicationContext)`**, and return `Result.success()`; on an exception return `Result.retry()`. It writes to the database and tells the user, but never touches the UI directly.

**Make pass (instrumented):** `a_successful_sync_writes_to_the_database_and_returns_success`, `a_failing_sync_asks_for_retry`.

**Docs:** [CoroutineWorker](https://developer.android.com/develop/background-work/background-tasks/persistent/threading/coroutineworker) · [Worker Result](https://developer.android.com/develop/background-work/background-tasks/persistent/how-to/define-work)

#### Telling the user: a notification, and a runtime permission

A background sync is invisible unless you surface it, so on success the worker posts a **notification** (a "Posts synced" message). That is a good excuse to meet the other kind of Android permission. `INTERNET` (Lab 5) is a **normal, install-time** permission, granted automatically. Posting a notification on **Android 13 (API 33)+** needs `POST_NOTIFICATIONS`, a **runtime (dangerous) permission** the user must grant in a dialog. The pieces are provided in `Notifications.kt`, and you only call them:

- the manifest declares `<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />` (already added);
- `RequestNotificationPermission()` (called from `PostsRoute`) asks for it once from the UI, with `rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission())` guarded on API 33+ (a worker has no Activity to prompt from, so the *UI* must ask first);
- `showSyncNotification(context)` builds a channel (required since API 26) and posts via `NotificationManagerCompat`, and **no-ops until the permission is granted**, which is exactly why the request matters.

Run it, grant the permission, then force a sync (or wait): you get a notification. Deny it: the app keeps working, no crash. That request-rationale-degrade flow is what "handle a runtime permission" means, and it is a floor requirement for your own project.

**Docs:** [Notification runtime permission](https://developer.android.com/develop/ui/views/notifications/notification-permission) · [Request permissions](https://developer.android.com/training/permissions/requesting)

### 3 · Schedule it: `Sync.kt`

Write `object Sync` with `fun schedule(context: Context)` that:
- builds `Constraints` requiring `NetworkType.CONNECTED`;
- builds a `PeriodicWorkRequestBuilder<RefreshWorker>(6, TimeUnit.HOURS)` with those constraints;
- enqueues it with `WorkManager.getInstance(context).enqueueUniquePeriodicWork("posts-refresh", ExistingPeriodicWorkPolicy.KEEP, request)`.

`PostsApp` already calls `Sync.schedule(this)` on startup, so once this compiles the schedule is live.

**Docs:** [Define work constraints](https://developer.android.com/develop/background-work/background-tasks/persistent/getting-started/define-work#work-constraints) · [Recurring work](https://developer.android.com/develop/background-work/background-tasks/persistent/getting-started/recurring-work)

### Then: see it run

You cannot easily watch a 6-hour periodic job in a lab, so use the tools: in Android Studio open **App Inspection → Background Task Inspector** to see your `posts-refresh` work enqueued with its constraints. To force it, you can temporarily lower the interval or enqueue a one-time request; and you can toggle the emulator's network to watch the constraint hold the work back.

---

## Part 4: Extend it

Verify by running / with the Background Task Inspector. Do at least one.

1. **Add a constraint.** Also require `setRequiresBatteryNotLow(true)`, and in the inspector confirm the work waits when you set the emulator to low battery. Argue whether an app like this should also require `UNMETERED` (no mobile data), and what the user would trade.
2. **Backoff on failure.** Set a custom backoff policy on the request (`setBackoffCriteria(BackoffPolicy.EXPONENTIAL, ...)`). What does `Result.retry()` do differently from `Result.failure()`?
3. **A "sync now" action.** Add a menu action that enqueues a one-time `OneTimeWorkRequest<RefreshWorker>` (still network-constrained). Notice you did not add a code path to the UI to load data; the same Room-observing loop delivers it.

For each, write one line: *what condition did you impose, and who decides when the work actually runs?*

---

## Gotchas: WorkManager remembers everything

WorkManager's greatest strength is also its sharpest edge: **it persists your work in its own database and runs it even across process death and reboots.** That is what makes it reliable. It also means the work you schedule is durable state that outlives your app's process, and you have to treat it that way. A few things bite people, and one of them is genuinely hard to debug.

- **A reboot does not clear your schedule.** Enqueue a periodic job once and it keeps coming back after the phone restarts, after the app is force-stopped, after days of not being opened. During development this surprises people: "I removed that code, why is it still syncing?" The code that *enqueued* the work is gone, but the *work* is still in WorkManager's database.

- **`KEEP` ignores your changes (the classic time-sink).** You schedule unique work with `ExistingPeriodicWorkPolicy.KEEP`, then later change the interval or a constraint to test something, and nothing happens. `KEEP` means "if a schedule with this name already exists, leave it alone", so your new request is discarded and the old one keeps running. Nothing errors; your change simply has no effect, which is the most confusing kind of bug. The fix is to say what you mean:

  ```kotlin
  // KEEP: don't touch an existing schedule (fine for first-time setup).
  // UPDATE: apply the new request to the existing unique work (use this when the spec changed).
  WorkManager.getInstance(context).enqueueUniquePeriodicWork(
      "posts-refresh",
      ExistingPeriodicWorkPolicy.UPDATE,   // was KEEP
      request,
  )
  ```

  or cancel it first: `WorkManager.getInstance(context).cancelUniqueWork("posts-refresh")`. (During development, "Clear app data" also wipes WorkManager's database and gives you a clean slate.)

- **Delete or rename a Worker while work still references it, and it breaks later, far from the cause.** The persisted request stores the worker's fully-qualified class name as a string. If a scheduled job outlives the class, when its time comes WorkManager cannot construct the worker; the work fails (you will see `Could not instantiate mobile.posts.RefreshWorker` in Logcat) and whatever depended on it silently stops. Because it fires long after, on some future trigger or on a user's device after an app update, it is decoupled from the change that caused it, and hard to trace. **Clean up after yourself:** cancel work you no longer need before you remove its worker.

**The discipline:** treat scheduled work as owned, persistent state. Cancel what you no longer need (`cancelUniqueWork`, `cancelAllWorkByTag`, `cancelWorkById`), prefer `UPDATE` over `KEEP` when a request changes, and let WorkManager tidy finished work (it auto-prunes; `pruneWork()` forces it). When something seems stuck, the **Background Task Inspector** in Android Studio shows exactly what is enqueued, with which constraints and worker, which turns "why is this happening?" into a two-minute look.

### A worked example of the gotcha

Say that, to see your sync fire during the lab, you drop the interval to the 15-minute minimum:

```kotlin
val request = PeriodicWorkRequestBuilder<RefreshWorker>(15, TimeUnit.MINUTES)
    .setConstraints(constraints)
    .build()
WorkManager.getInstance(context).enqueueUniquePeriodicWork(
    "posts-refresh",
    ExistingPeriodicWorkPolicy.KEEP,   // <-- the trap
    request,
)
```

You reinstall, wait, and it still runs on the original 6-hour cadence. Nothing is wrong with your new request; `KEEP` found the old `"posts-refresh"` schedule already in WorkManager's database (it survived the reinstall's data, or a previous run) and kept it. Switch the policy to `ExistingPeriodicWorkPolicy.UPDATE` (or `cancelUniqueWork("posts-refresh")` once, then re-enqueue), and confirm in the Background Task Inspector that the schedule now shows your 15-minute period. Same lesson from the other side: the database remembered, so you have to be explicit about replacing what it remembered.

---

## Part 5: You are done when

```bash
./gradlew test                       # repository + ViewModel tests pass
./gradlew connectedDebugAndroidTest  # the DAO and worker tests pass (device/emulator running)
```

And, conceptually, you can explain the loop: a network-constrained periodic worker refreshes Room in the background, and the UI, observing Room, shows recent data whenever it opens, online or off. That is offline-first.

Commit your work:
```bash
git add -A && git commit -m "Lab 7 complete"
```

---

## Apply it to your app

Your project milestone: give your app a WorkManager sync that keeps its Room database fresh under a real constraint (at least a network requirement). Then answer the systems question you will defend: *how fresh does your data need to be, what does it cost the user (battery, mobile data) to keep it that fresh, and what constraints express that trade-off?* Work it through the **five questions** you use for every design decision this course - the same frame you meet in Course 6, on the written exam, and in the project defence: *What binds? · What will you do? · What are you giving up? · What breaks? · What would change your mind?* This is the mobile-systems reasoning the whole project is assessed on.

## Using AI on this lab

Good uses: the WorkManager builder API, a constraint name, the `TestListenableWorkerBuilder` signature. Reason unaided about the real decisions: *why is this work deferrable rather than immediate? why must it survive a reboot? which constraints protect the user's battery and data, and why not just sync every minute?* If you cannot justify your constraints for your own app, you have not finished the lab.

## Further reading

- [Background Task Inspector](https://developer.android.com/studio/inspect/task): see, and debug, exactly what work is enqueued.
- [Manage and cancel work](https://developer.android.com/develop/background-work/background-tasks/persistent/how-to/manage-work): unique work policies, cancelling, pruning.
- [Observe work with WorkInfo](https://developer.android.com/develop/background-work/background-tasks/persistent/how-to/observe): read a job's state and result.
- [Doze and App Standby](https://developer.android.com/training/monitoring-device-state/doze-standby): how the system throttles background work, and how to test with it.

## What's next

Lab 8 introduces **dependency injection** with Hilt. The hand-wired `Graph` you have been using (and the worker reaching into it) is exactly the wiring Hilt takes over, including a proper way to give the worker its repository instead of a global. The app behaves identically; the plumbing gets honest.

*This lab adapts ideas from [Android Basics with Compose](https://developer.android.com/courses/android-basics-compose/course) (Google) and the [WorkManager](https://developer.android.com/develop/background-work/background-tasks/persistent) documentation.*
