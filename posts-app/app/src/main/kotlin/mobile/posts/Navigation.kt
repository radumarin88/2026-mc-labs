package mobile.posts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable

/** Type-safe navigation routes (Lab 5). [DetailRoute] carries the post id it opens. */
@Serializable
object ListRoute

@Serializable
data class DetailRoute(val postId: Int)

/**
 * The app's navigation graph: a list screen and a detail screen. One ViewModel is hoisted here and
 * SHARED by both destinations, so the detail screen reads the selected post from the same state the
 * list observes (no second data source). Navigation Compose saves and restores the back stack across
 * configuration change AND process death, so returning to a backgrounded-then-killed app lands the user
 * back on the screen they left. In Lab 8 the `viewModel { }` becomes `hiltViewModel()`.
 */
@Composable
fun PostsNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val vm: PostsViewModel = viewModel { PostsViewModel() }
    val state by vm.uiState.collectAsStateWithLifecycle()
    NavHost(navController = navController, startDestination = ListRoute, modifier = modifier) {
        composable<ListRoute> {
            PostsScreen(
                state = state,
                onQueryChange = vm::setQuery,
                onShowBookmarkedOnlyChange = vm::setShowBookmarkedOnly,
                onToggleBookmark = vm::toggleBookmark,
                onOpenPost = { id -> navController.navigate(DetailRoute(id)) },
                onRetry = vm::refresh,
            )
        }
        composable<DetailRoute> { entry ->
            val postId = entry.toRoute<DetailRoute>().postId
            PostDetailScreen(
                post = state.posts.firstOrNull { it.id == postId },
                bookmarked = postId in state.bookmarkedIds,
                onToggleBookmark = { vm.toggleBookmark(postId) },
                onBack = { navController.popBackStack() },
            )
        }
    }
}
