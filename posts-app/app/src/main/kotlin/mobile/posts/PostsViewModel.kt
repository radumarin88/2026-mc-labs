package mobile.posts

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * The complete, immutable screen state. It stores the source of truth (the posts, which bookmarks
 * the user set, the search query, the filter, and whether a load is in flight or failed); what the
 * screen actually shows ([visiblePosts]) and the [bookmarkCount] are DERIVED so they cannot drift.
 *
 * The [isLoading] and [errorMessage] fields are new in Lab 5, for when the posts come from the
 * network. Keeping them as fields on the same state (rather than a separate Loading/Error type) lets
 * the screen show cached posts AND a refresh spinner or error at the same time, which is exactly what
 * offline-first needs in Lab 7.
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
     * Exercise 3 (Lab 5). Fetch the posts as a coroutine. In viewModelScope.launch: set isLoading =
     * true and clear any error, then try the repository and publish the posts with isLoading = false;
     * catch any exception and publish the message with isLoading = false. Leave the bookmarks and
     * query untouched (copy the current state). Runs off the main thread; cancelled with the ViewModel.
     * (Let Android Studio add the imports as you type.)
     */
    fun refresh() {
        TODO()
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
