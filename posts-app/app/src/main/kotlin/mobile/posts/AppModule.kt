package mobile.posts

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Singleton

/**
 * Exercise 2. The Hilt module: how to build the things Hilt cannot construct on its own.
 *
 * Hilt can create a class marked `@Inject constructor` by itself. But it does not know how to build a
 * Room database (that needs Room.databaseBuilder and a Context) or a Retrofit API (that needs a
 * builder). So you teach it, with @Provides functions in a @Module. @InstallIn(SingletonComponent)
 * puts these in the app-wide graph; @Singleton means "make one and reuse it".
 *
 * The DAO and the API here are exactly what the repository asks for in its constructor, so once this
 * module exists, Hilt can build the repository, the ViewModel that needs it, and the worker too.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "posts.db").build()

    @Provides
    fun providePostsDao(database: AppDatabase): PostsDao = database.postsDao()

    @Provides
    @Singleton
    fun providePostsApi(): PostsApi {
        val json = Json { ignoreUnknownKeys = true }
        return Retrofit.Builder()
            .baseUrl("https://jsonplaceholder.typicode.com/")
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(PostsApi::class.java)
    }
}
