package mobile.posts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * The complete, immutable screen state. It stores the source of truth (the posts, which bookmarks
 * the user set, the search query, the filter, and whether a load is in flight or failed); what the
 * screen actually shows ([visiblePosts]) and the [bookmarkCount] are DERIVED so they cannot drift.
 *
 * The bookmark and search fields arrive in Lab 4; the loading/error fields arrive in Lab 5 when the
 * posts start coming from the network. Note the design: keeping [isLoading]/[errorMessage] as fields
 * on the same state (rather than a separate Loading/Error type) means the screen can show cached
 * posts AND a refresh spinner or error at the same time, which is exactly what offline-first needs
 * in Lab 7.
 */
data class PostsUiState(
    val posts: List<Post> = emptyList(),
    val bookmarkedIds: Set<Int> = emptySet(),
    val query: String = "",
    val showBookmarkedOnly: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
) {
    val visiblePosts: List<Post>
        get() = posts.filter { post ->
            (!showBookmarkedOnly || post.id in bookmarkedIds) &&
                post.title.contains(query.trim(), ignoreCase = true)
        }

    val bookmarkCount: Int
        get() = bookmarkedIds.size
}

class PostsViewModel(
    private val repository: PostsRepository = PostsRepository(RetrofitInstance.api),
) : ViewModel() {

    // Starts in the loading state, because init immediately refreshes from the network.
    private val _uiState = MutableStateFlow(PostsUiState(isLoading = true))
    val uiState: StateFlow<PostsUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    /**
     * Fetch the posts as a coroutine (Lab 5). Sets loading, tries the repository, and turns the
     * outcome into state: the posts on success, an error message on failure. Runs off the main
     * thread and is cancelled with the ViewModel, so a screen you left cannot keep a request alive.
     */
    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val posts = repository.loadPosts()
                _uiState.update { it.copy(posts = posts, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Something went wrong") }
            }
        }
    }

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
