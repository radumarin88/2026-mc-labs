package mobile.posts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
 * A fully STATELESS screen: it renders [state] and reports events. It shows a spinner while the first
 * load is in flight, an error with a retry when the load fails and there is nothing to show, and
 * otherwise the (searched, filtered) list of posts, each a network image + title + body with a bookmark
 * toggle and clickable to open the detail screen. The ViewModel is hoisted in PostsNavHost.
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

        when {
            state.isLoading && state.posts.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            state.errorMessage != null && state.posts.isEmpty() -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("Could not load posts: ${state.errorMessage}")
                    Button(onClick = onRetry) { Text("Retry") }
                }
            }

            else -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(items = state.visiblePosts, key = { it.id }) { post ->
                        Column(
                            modifier = Modifier.fillMaxWidth()
                                .clickable { onOpenPost(post.id) }
                                .padding(vertical = 12.dp),
                        ) {
                            AsyncImage(
                                model = "https://picsum.photos/seed/${post.id}/600/300",
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxWidth().height(160.dp),
                            )
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
    }
}
