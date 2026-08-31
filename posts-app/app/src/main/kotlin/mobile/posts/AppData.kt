package mobile.posts

import android.app.Application
import android.content.Context
import androidx.room.Room

/**
 * Mapping across the boundaries: network model -> entity (on the way in), entity -> domain model (on
 * the way out). Provided for you; it depends only on the field names of your entities.
 */
fun PostEntity.toDomain(): Post = Post(id = id, title = title, body = body)
fun PostDto.toEntity(): PostEntity = PostEntity(id = id, title = title, body = body)

/**
 * A tiny manual dependency container (a "service locator"). Room needs a Context to open the database,
 * and the repository needs the DAO, so we build both once when the app starts and hand out the ready
 * repository. This is deliberately hand-wired; Lab 8 replaces it with Hilt. Provided for you.
 */
object Graph {
    lateinit var repository: PostsRepository
        private set

    fun provide(context: Context) {
        val database = Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "posts.db",
        ).build()
        repository = PostsRepository(dao = database.postsDao(), api = RetrofitInstance.api)
    }
}

/** The Application builds the container before any screen is shown. Declared in the manifest. Provided. */
class PostsApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Graph.provide(this)
    }
}
