package mobile.posts

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Mapping across the boundaries: network model -> entity (on the way in), entity -> domain model (on
 * the way out). Provided for you; it depends only on the field names of your entities.
 */
fun PostEntity.toDomain(): Post = Post(id = id, title = title, body = body)
fun PostDto.toEntity(): PostEntity = PostEntity(id = id, title = title, body = body)

/**
 * The Application, now a Hilt entry point (@HiltAndroidApp) that also supplies WorkManager's
 * configuration so Hilt can construct workers (@HiltWorker) with their injected dependencies. The
 * hand-wired Graph service locator from Labs 6 and 7 is gone: Hilt does that wiring now. Provided.
 *
 * Because we provide our own Configuration, the manifest disables WorkManager's default initializer.
 */
@HiltAndroidApp
class PostsApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        Sync.schedule(this)
    }
}
