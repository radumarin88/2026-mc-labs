package mobile.posts

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * The complete, immutable screen state. It stores the source of truth (the posts, which the user has
 * bookmarked, the search query, and whether to show only bookmarks); what the screen shows
 * ([visiblePosts]) and the [bookmarkCount] are DERIVED, so they can never drift out of sync.
 *
 * Lab 5 adds loading and error fields here when the posts start coming from the network.
 */
data class PostsUiState(
    val posts: List<Post> = emptyList(),
    val bookmarkedIds: Set<Int> = emptySet(),
    val query: String = "",
    val showBookmarkedOnly: Boolean = false,
) {
    /**
     * Exercise 1a. The posts to display: keep a post when we are not filtering to bookmarks or it is
     * bookmarked, AND its title contains the query (case-insensitive).
     */
    val visiblePosts: List<Post>
        get() = TODO()

    /** Exercise 1b. How many posts are bookmarked. */
    val bookmarkCount: Int
        get() = TODO()
}

/**
 * The state holder. It owns the state and is the only thing that changes it; the UI reads the state
 * and reports events. For now the posts come from an in-memory list; in Lab 5 they come from the
 * network and this ViewModel gains a repository and a refresh coroutine.
 */
class PostsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PostsUiState(posts = seedPosts))

    /** Exercise 2. Expose [_uiState] to the UI as a read-only StateFlow. */
    val uiState: StateFlow<PostsUiState> = TODO()

    /** Exercise 3. Produce a new state with the search query changed. */
    fun setQuery(query: String) {
        TODO()
    }

    /** Exercise 4. Produce a new state with the "bookmarked only" flag changed. */
    fun setShowBookmarkedOnly(only: Boolean) {
        TODO()
    }

    /**
     * Exercise 5. Toggle a bookmark: if [id] is already in bookmarkedIds, remove it; otherwise add
     * it. Produce a new state (do not mutate the set).
     */
    fun toggleBookmark(id: Int) {
        TODO()
    }
}
