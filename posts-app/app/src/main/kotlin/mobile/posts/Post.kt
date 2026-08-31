package mobile.posts

/** The domain model the app uses. In Lab 5 the posts start coming from the network. */
data class Post(
    val id: Int,
    val title: String,
    val body: String,
)

/** In-memory sample posts, so the screen has data before there is a network (Lab 5 replaces this). */
val seedPosts: List<Post> = listOf(
    Post(1, "Kotlin coroutines", "Structured concurrency for asynchronous work."),
    Post(2, "Room database", "An on-device source of truth your app reads first."),
    Post(3, "WorkManager", "Constraint-aware background work that respects the battery."),
    Post(4, "Jetpack Compose", "Describe the UI as a function of state."),
    Post(5, "Offline first", "Keep working when the network does not."),
)
