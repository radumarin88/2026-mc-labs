package mobile.posts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

/**
 * The DETAIL screen (Lab 5): a full, scrollable view of one post, reached by tapping a row in the list.
 * Fully STATELESS: it renders the given [post] and reports events. It shows a hero image (Coil), the
 * title, the full body, and a bookmark toggle. Lab 10 adds on-device translation here.
 */
@Composable
fun PostDetailScreen(
    post: Post?,
    bookmarked: Boolean,
    onToggleBookmark: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TextButton(onClick = onBack) { Text("Back") }
        if (post == null) {
            Text("Post not found.")
            return@Column
        }
        AsyncImage(
            model = "https://picsum.photos/seed/${post.id}/800/400",
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth().height(220.dp),
        )
        Text(text = post.title, style = MaterialTheme.typography.headlineSmall)
        Text(text = post.body, style = MaterialTheme.typography.bodyLarge)
        Button(onClick = onToggleBookmark) { Text(if (bookmarked) "Saved" else "Save") }
    }
}
