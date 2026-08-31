package mobile.posts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * The complete, immutable screen state. The posts and the bookmarked ids now come from the database
 * (Lab 6); the search query and filter are transient UI state. [visiblePosts] and [bookmarkCount] are
 * DERIVED. [isLoading] / [errorMessage] describe the network refresh.
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

/**
 * Exercise 3. The ViewModel now OBSERVES the database (Lab 6).
 *
 * Instead of holding the posts itself, it collects the repository's Flows and copies each new value
 * into the state. So a write anywhere (a network refresh, a bookmark toggle) flows through Room and
 * back into the UI automatically. The database is the single source of truth; the ViewModel is just a
 * lens onto it.
 *
 * Implement the four TODOs:
 *   - in init, launch a coroutine that collects repository.observePosts() and copies each list into
 *     state.posts; and another that collects observeBookmarkIds() into state.bookmarkedIds.
 *   - refresh(): set isLoading, try repository.refresh(), catch into errorMessage, clear isLoading.
 *   - toggleBookmark(id): ask the repository to flip the bookmark (add it if it is not currently in
 *     bookmarkedIds, remove it otherwise). The change comes back through the Flow.
 */
class PostsViewModel(
    private val repository: PostsRepository = Graph.repository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PostsUiState(isLoading = true))
    val uiState: StateFlow<PostsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observePosts().collect { posts ->
                _uiState.update { it.copy(posts = posts) }
            }
        }
        viewModelScope.launch {
            repository.observeBookmarkIds().collect { ids ->
                _uiState.update { it.copy(bookmarkedIds = ids) }
            }
        }
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                repository.refresh()
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Something went wrong") }
            }
        }
    }

    fun toggleBookmark(id: Int) {
        viewModelScope.launch {
            repository.setBookmarked(id, bookmarked = id !in _uiState.value.bookmarkedIds)
        }
    }

    fun setQuery(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    fun setShowBookmarkedOnly(only: Boolean) {
        _uiState.update { it.copy(showBookmarkedOnly = only) }
    }
}
