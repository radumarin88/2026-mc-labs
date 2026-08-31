# Lab 5: Networking (Retrofit and coroutines)

So far your Posts app has shown a hardcoded list. Now it talks to a real server. You fetch the posts from a REST API with **Retrofit**, run the call as a **coroutine** so it never blocks the screen, and give the state two new facts: is a load in flight, and did it fail? This is also where you meet coroutines properly, in the place they matter most.

You are extending the **same app** from Lab 4. The architecture (ViewModel, immutable `PostsUiState`, the stateless screen) stays; you add a network layer beneath it and teach the ViewModel to fetch. The tests run on the JVM with a fake server, so `./gradlew test` needs no device.

Time: about two hours.

---

## Part 1: Before you start

### What is new since Lab 4
The project now depends on **Retrofit** (added to `app/build.gradle.kts` and the version catalog) and declares the **INTERNET** permission. `PostsUiState` has gained two fields, `isLoading` and `errorMessage`, and there is a new network layer for you to complete: `PostsApi`, `Network.kt` (provided), and `PostsRepository`.

### Open the project and run the tests
Open `posts-app`, let Gradle sync (it downloads Retrofit and OkHttp the first time), and run:

```bash
./gradlew test        # fast JVM tests: a fake API stands in for the network
```

They start red. To see the real thing, run the app on a device or emulator with internet; it fetches live posts.

Pinned tooling (verified Aug 2026): **Retrofit 3.0.0** (with the kotlinx-serialization converter, OkHttp 4.12 transitive), plus the Lab 4 stack.

### The one production concern: A NETWORK CALL IS SLOW, OFF-THREAD, AND FALLIBLE

On a phone the network is the least reliable thing you touch: slow, dropping, unavailable exactly when the user is on a train. Three consequences shape every networked screen:

- **It must not run on the main thread.** A blocking call there freezes the UI. You run it as a **coroutine**, which suspends without blocking.
- **It can fail.** "No network" and "the server errored" are normal, not exceptional. Your state carries `isLoading` and `errorMessage` so the screen can always show where things stand, and give the user a retry.
- **It should not outlive its screen.** A `viewModelScope` coroutine is cancelled when the ViewModel is cleared, so a screen you left cannot keep a request alive.

Getting an app to show data is easy. Getting it to behave when the data is late or absent is the job, and it is the thread that runs through Labs 6 (cache it) and 8 (work offline).

---

## Part 2: What you need to know

### What Retrofit is, and the alternatives

Talking to a server means HTTP: open a connection, send a GET, read the response stream, and parse the JSON into objects. Done by hand (with the low-level `HttpURLConnection`) that is a lot of repetitive, error-prone code on every screen.

**Retrofit** removes it. It is a **type-safe HTTP client** for Android and the JVM, from Square. You *describe* your API as a Kotlin interface whose methods are annotated with the HTTP method and path, and Retrofit generates the code that makes the calls. It leans on two things underneath: **OkHttp** does the real networking (connections, timeouts, retries), and a **converter** turns JSON into your objects (here, kotlinx.serialization). You call a normal `suspend` function and get back a parsed object; the URLs, threads, and streams are handled for you. That is what "type-safe" means here: the compiler checks your call, instead of you hand-building URL strings and parsing raw responses.

Retrofit is not the only option:
- **OkHttp on its own** is the layer Retrofit sits on. You can use it directly, but then you build each request and parse each response by hand. Retrofit is the ergonomic wrapper over it.
- **Ktor Client** (from JetBrains) is the modern alternative: coroutine-native from the ground up, with JSON handling built in, and crucially **Kotlin Multiplatform**, so the same networking code runs on Android, iOS, desktop, and the server. If you were sharing a codebase across platforms, Ktor would be the natural pick, and teams targeting KMP increasingly choose it.

We use **Retrofit** here because it is the most established Android choice: mature, well-documented, and it pairs cleanly with the repository pattern you are building (a recent survey still put it at roughly three-quarters of Android projects). The ideas transfer, though: an interface of `suspend` functions, a DTO, a converter, and a repository that maps to the domain model is the same shape whether the client underneath is Retrofit or Ktor.

**Docs:** [Retrofit](https://square.github.io/retrofit/) · [OkHttp](https://square.github.io/okhttp/) · [Ktor client](https://ktor.io/docs/client-create-new-application.html)

### Coroutines, in one screen

A **coroutine** is work that can pause ("suspend") and resume later without blocking a thread.

- A function marked **`suspend`** can call other suspend functions and pause at those points. Retrofit generates a suspend function per endpoint, so `api.getPosts()` suspends while the request is in flight and resumes with the parsed result. You write no threading code.
- `suspend` is contagious upward: a function that calls a suspend function must itself be `suspend`. So the repository's `loadPosts()` is `suspend` too.
- To *start* a coroutine from ordinary code you need a **scope**. A ViewModel has **`viewModelScope`**; `viewModelScope.launch { ... }` starts a coroutine tied to the ViewModel's life.
- Errors are just exceptions. A failed request throws, and you catch it with `try/catch`.

```kotlin
viewModelScope.launch {
    _state.update { it.copy(isLoading = true, errorMessage = null) }
    try {
        val posts = repository.loadPosts()          // suspends here, off the main thread
        _state.update { it.copy(posts = posts, isLoading = false) }
    } catch (e: Exception) {
        _state.update { it.copy(isLoading = false, errorMessage = e.message) }
    }
}
```

### The pieces, and why they are separate
- **`PostsApi`** (a Retrofit interface) describes the HTTP request; Retrofit implements it.
- **`PostDto`** is the network model (the JSON shape); **`Post`** is the domain model. The **repository** maps one to the other at the boundary, so the rest of the app never depends on the server's JSON.
- **`PostsRepository`** takes the API as a constructor parameter, which lets a test pass a **fake** API instead of the real network. That is why the logic is JVM-testable in milliseconds.
- Note the state design: `isLoading` and `errorMessage` are **fields on the same state** as the posts, not a separate Loading/Error type. That means the screen can show cached posts *and* a refresh spinner or error at once, which is exactly what offline-first needs in Lab 7.

The Retrofit client (base URL, JSON converter) is set up for you in `Network.kt`, and `PostsRoute` is already updated to build the ViewModel with its repository; you will move both into dependency injection in Lab 8.

---

## Part 3: The exercises

### 1 · Describe the request: `PostsApi.kt`

Add the Retrofit annotation so `getPosts` performs an HTTP GET on the `posts` endpoint. The `suspend` and the `List<PostDto>` return type are there already.

```kotlin
interface ExampleApi {
    @GET("things")
    suspend fun getThings(): List<ThingDto>
}
```

**Docs:** [Retrofit](https://square.github.io/retrofit/) · [Coroutines with Retrofit](https://developer.android.com/topic/libraries/architecture/coroutines)

### 2 · Fetch and map: `PostsRepository.kt`

Implement `loadPosts()`: call `api.getPosts()` and map each `PostDto` to a `Post`. Do not catch errors here; let them propagate so the ViewModel decides what the user sees.

**Make pass:** `loadPosts maps network DTOs to domain models`.

**Docs:** [App architecture: data layer](https://developer.android.com/topic/architecture/data-layer)

### 3 · Run the call: `PostsViewModel.kt` (`refresh`)

Implement `refresh()`: in `viewModelScope.launch`, set `isLoading = true` and clear any error, then `try` the repository and publish the posts with `isLoading = false`, `catch` any exception and publish the message with `isLoading = false`. Leave the bookmarks and query untouched (copy the current state). This is the coroutine doing the real work.

**Make pass:** `starts loading, then ends in success with the fetched posts`, `a failing load ends with an error message`, `retry after a failure can succeed`, `bookmarks survive a refresh`.

**Docs:** [Coroutines overview](https://developer.android.com/kotlin/coroutines) · [Coroutines with lifecycle](https://developer.android.com/topic/libraries/architecture/coroutines)

### 4 · Render the states: `PostsScreen.kt`

Below the search box and filter chip, branch on the state:
- when `isLoading` and there are no posts yet: a centered `CircularProgressIndicator`.
- when there is an `errorMessage` and no posts: the message and a `Retry` button that calls `onRetry`.
- otherwise: the `LazyColumn` of `visiblePosts` (which you already had in Lab 4), each with its Save toggle, and **each row clickable** to open the detail screen: put a `.clickable { onOpenPost(post.id) }` on the row.

**The app is now two screens.** `MainActivity` hosts a provided `PostsNavHost` (a Navigation Compose graph with type-safe routes `ListRoute` and `DetailRoute(postId)`, the same idiom as Lab 2), and a provided `PostDetailScreen` renders the full post (hero image, body, bookmark) when a row is tapped. The one ViewModel is hoisted in `PostsNavHost` and shared by both screens, so the detail reads the selected post from the same state. Making rows call `onOpenPost` is all the wiring the list owes. The systems point: the nav library restores the back stack after **process death**, not just rotation, so a user who is killed by the OS mid-app returns where they left off.

**Also load a network image per post.** Fetching JSON is one kind of network call; fetching an **image** is another, and doing it wrong (decoding a bitmap on the main thread) freezes the UI. Use **Coil** (`io.coil-kt.coil3:coil-compose` + `coil-network-okhttp`, already on the classpath) to load each post's image off-thread with caching:
```kotlin
import coil3.compose.AsyncImage
// inside the post row, above the title:
AsyncImage(
    model = "https://picsum.photos/seed/${post.id}/600/300",
    contentDescription = null,
    modifier = Modifier.fillMaxWidth().height(160.dp),
)
```
`AsyncImage` runs the fetch and decode off the main thread and caches the result; it needs only the `INTERNET` permission you already have. (Here the URL is derived from the post id via a placeholder image service, since the posts API carries no image field; in your own app the URL comes from your data.)

**Do it:** run the app (spinner, then the list with images). Turn on **airplane mode** and relaunch, or press Retry: you get the error state, not a crash. That is the whole point.

**Docs:** [Progress indicators](https://developer.android.com/develop/ui/compose/components/progress) · [Lists](https://developer.android.com/develop/ui/compose/lists) · [Coil for Compose](https://coil-kt.github.io/coil/compose/)

---

## Part 4: Extend it

Verify by running; add a JVM test where you can. Do at least one.

1. **Empty state.** If the load succeeds but returns no posts, show "No posts yet". Is that a new field, or a branch inside the list case?
2. **A single-post endpoint.** Add `@GET("posts/{id}") suspend fun getPost(@Path("id") id: Int): PostDto` and a repository method. A test is enough.
3. **Name the failure.** Distinguish "no connection" from other failures in the message. (`retrofit2.HttpException` is thrown for non-2xx; an `IOException` usually means the network is down.)

For each, write one line: *what does the user see when this goes wrong, and why is that better than a crash or a blank screen?*

---

## Part 5: You are done when

```bash
./gradlew test        # repository and ViewModel tests pass
```

And the app runs: a spinner, then the fetched posts; airplane mode shows the error with a working Retry; and search and bookmarks still work.

Notice what the tests did **not** need: a device, a network, or waiting. Because the logic is a plain repository and ViewModel with the API injected, a fake stands in for the server and virtual time skips the waiting. That testability is the payoff of Lab 4's architecture, and it is why these tests belong in the fast CI gate (Lab 9).

Commit your work:
```bash
git add -A && git commit -m "Lab 5 complete"
```

---

## Apply it to your app

Your milestone: give your app a real remote source, a Retrofit API and a repository that maps to your domain model, driving one screen with loading and error state. Then answer the systems question you will defend: *when the network is down, what does your app show, and what does the user lose?* Labs 6 and 7 turn that answer into a cache and offline sync.

## What's next

Lab 6 gives this app a local database with Room, so it keeps its own copy of the posts and reads them reactively with Flow. Lab 7 then combines this network layer with that database into an offline-first app, the systems heart of the course.

*This lab adapts ideas from [Android Basics with Compose](https://developer.android.com/courses/android-basics-compose/course) (Google) and the [Retrofit](https://square.github.io/retrofit/) documentation.*
