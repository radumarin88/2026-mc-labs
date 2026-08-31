package mobile.posts

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import javax.inject.Inject

/**
 * Mapping across the boundaries: network model -> entity (on the way in), entity -> domain model (on
 * the way out). Provided for you; it depends only on the field names of your entities.
 */
fun PostEntity.toDomain(): Post = Post(id = id, title = title, body = body)
fun PostDto.toEntity(): PostEntity = PostEntity(id = id, title = title, body = body)

/**
 * Exercise 2 (turn Hilt on). This Application is the ROOT of Hilt's graph. Add the one annotation from
 * Part 2 that bootstraps Hilt, so the `@Inject` field below is satisfied and `@AndroidEntryPoint` /
 * `@HiltViewModel` elsewhere can compile. Without it, Hilt generates nothing and the build fails.
 *
 * The WorkManager plumbing is PROVIDED: this is a `Configuration.Provider` that hands WorkManager a
 * `HiltWorkerFactory` (Hilt injects it into the field below), and the manifest disables WorkManager's
 * default initializer so ours is used. You supply the switch; the factory wiring is done. (The
 * hand-wired `Graph` service locator from Labs 6 and 7 is gone: Hilt does that wiring now.)
 */
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
