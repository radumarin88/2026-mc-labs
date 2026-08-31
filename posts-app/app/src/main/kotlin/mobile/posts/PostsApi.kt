package mobile.posts

import retrofit2.http.GET

/**
 * The Retrofit API description (Lab 5). Retrofit turns this interface into a working network client:
 * the annotation says the method and path, the return type says what to parse the response into,
 * and `suspend` means Retrofit runs the call off the main thread and resumes you with the result.
 */
interface PostsApi {
    @GET("posts")
    suspend fun getPosts(): List<PostDto>
}
