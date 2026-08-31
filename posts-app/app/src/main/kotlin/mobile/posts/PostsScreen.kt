package mobile.posts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * A fully STATELESS screen. The search box and filter chip are provided. Your job (exercise 4) is to
 * render the three network states below them with an exhaustive branch over [state]:
 *   - when isLoading and there are no posts yet: a centered CircularProgressIndicator.
 *   - when there is an errorMessage and no posts: the message and a "Retry" Button that calls [onRetry].
 *   - otherwise: a LazyColumn of state.visiblePosts (image + title + body), keyed by id, each row
 *     clickable to `onOpenPost(post.id)` (opens the detail screen) with a Save/Saved TextButton that
 *     calls onToggleBookmark(post.id).
 * (Let Android Studio add the imports as you type.) The ViewModel is now hoisted in PostsNavHost.
 */
@Composable
fun PostsScreen(
    state: PostsUiState,
    onQueryChange: (String) -> Unit,
    onShowBookmarkedOnlyChange: (Boolean) -> Unit,
    onToggleBookmark: (Int) -> Unit,
    onOpenPost: (Int) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = state.query,
            onValueChange = onQueryChange,
            label = { Text("Search") },
            modifier = Modifier.fillMaxWidth(),
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp),
        ) {
            FilterChip(
                selected = state.showBookmarkedOnly,
                onClick = { onShowBookmarkedOnlyChange(!state.showBookmarkedOnly) },
                label = { Text("Bookmarked only") },
            )
            Text("${state.bookmarkCount} saved", style = MaterialTheme.typography.labelMedium)
        }

        // Exercise 4: render the three states here.
        TODO()
    }
}
