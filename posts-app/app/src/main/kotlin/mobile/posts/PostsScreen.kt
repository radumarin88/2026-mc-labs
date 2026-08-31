package mobile.posts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Exercise 7. Wire the screen to the ViewModel: obtain it with viewModel(), collect its state
 * lifecycle-aware with collectAsStateWithLifecycle(), and pass the state down while forwarding each
 * event (onQueryChange, onShowBookmarkedOnlyChange, onToggleBookmark) to the matching ViewModel
 * function. (Let Android Studio add the imports as you type.)
 */
@Composable
fun PostsRoute(modifier: Modifier = Modifier) {
    TODO()
}

/**
 * A fully STATELESS screen: it renders [state] and reports events. It searches and filters the posts
 * and lets you bookmark each one. Provided for you; you build the ViewModel behind it and the wiring
 * above. Lab 5 adds a loading spinner and an error state when the posts come from the network.
 */
@Composable
fun PostsScreen(
    state: PostsUiState,
    onQueryChange: (String) -> Unit,
    onShowBookmarkedOnlyChange: (Boolean) -> Unit,
    onToggleBookmark: (Int) -> Unit,
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

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(items = state.visiblePosts, key = { it.id }) { post ->
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                    Text(text = post.title, style = MaterialTheme.typography.titleMedium)
                    Text(text = post.body, style = MaterialTheme.typography.bodyMedium)
                    val saved = post.id in state.bookmarkedIds
                    TextButton(onClick = { onToggleBookmark(post.id) }) {
                        Text(if (saved) "Saved" else "Save")
                    }
                }
            }
        }
    }
}
