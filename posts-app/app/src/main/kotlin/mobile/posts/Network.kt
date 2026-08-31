package mobile.posts

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/**
 * Builds the Retrofit client and exposes the API (added in Lab 5). Provided for you: in Lab 8 you
 * will move this wiring into dependency injection. The base URL is JSONPlaceholder, a free practice
 * API; `ignoreUnknownKeys` tolerates extra JSON fields we did not model.
 */
object RetrofitInstance {
    private const val BASE_URL = "https://jsonplaceholder.typicode.com/"

    private val json = Json { ignoreUnknownKeys = true }

    val api: PostsApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(PostsApi::class.java)
    }
}
