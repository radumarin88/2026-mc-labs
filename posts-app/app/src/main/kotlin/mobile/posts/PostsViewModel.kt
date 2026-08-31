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
    val visiblePosts: List<Post>
        get() = posts.filter { post ->
            (!showBookmarkedOnly || post.id in bookmarkedIds) &&
                post.title.contains(query.trim(), ignoreCase = true)
        }

    val bookmarkCount: Int
        get() = bookmarkedIds.size
}

/**
 * The state holder. It owns the state and is the only thing that changes it; the UI reads the state
 * and reports events. For now the posts come from an in-memory list; in Lab 5 they come from the
 * network and this ViewModel gains a repository and a refresh coroutine.
 */
class PostsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PostsUiState(posts = seedPosts))
    val uiState: StateFlow<PostsUiState> = _uiState.asStateFlow()

    fun setQuery(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    fun setShowBookmarkedOnly(only: Boolean) {
        _uiState.update { it.copy(showBookmarkedOnly = only) }
    }

    fun toggleBookmark(id: Int) {
        _uiState.update { state ->
            val ids = if (id in state.bookmarkedIds) state.bookmarkedIds - id else state.bookmarkedIds + id
            state.copy(bookmarkedIds = ids)
        }
    }
}
