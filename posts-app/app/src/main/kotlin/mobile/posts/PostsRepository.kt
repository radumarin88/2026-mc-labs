package mobile.posts

/**
 * The repository: the single place the app asks for posts (Lab 5). It calls the network and maps the
 * network model ([PostDto]) to the domain model ([Post]), so callers never see JSON shapes. It takes
 * the [PostsApi] as a constructor parameter, which lets tests pass in a fake API instead of the real
 * network. In Lab 6 this becomes the seam where a local database is added.
 *
 * Implement [loadPosts]: call the api and return the posts mapped to the domain model. Do not catch
 * errors here; let them propagate so the ViewModel can decide what to show.
 */
class PostsRepository(private val api: PostsApi) {
    suspend fun loadPosts(): List<Post> = TODO()
}
