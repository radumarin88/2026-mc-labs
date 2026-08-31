package mobile.posts

/**
 * The Retrofit API description (Lab 5). Retrofit turns this interface into a working network client:
 * the annotation says the method and path, the return type says what to parse the response into,
 * and `suspend` means Retrofit runs the call off the main thread and resumes you with the result.
 *
 * Add the annotation so `getPosts` performs a GET on the "posts" endpoint (relative to the base URL).
 * (Let Android Studio add the import when you type the annotation.)
 */
interface PostsApi {
    suspend fun getPosts(): List<PostDto>
}
