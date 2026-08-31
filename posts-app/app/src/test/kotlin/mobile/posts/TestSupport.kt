package mobile.posts

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/** A fake API that never touches the network, so tests are fast and deterministic. Provided. */
class FakePostsApi : PostsApi {
    var result: Result<List<PostDto>> = Result.success(emptyList())
    override suspend fun getPosts(): List<PostDto> = result.getOrThrow()
}

/** Swaps Dispatchers.Main for a test dispatcher so viewModelScope coroutines run under the test. */
class MainDispatcherRule : TestWatcher() {
    val dispatcher = StandardTestDispatcher()
    override fun starting(description: Description) = Dispatchers.setMain(dispatcher)
    override fun finished(description: Description) = Dispatchers.resetMain()
}
