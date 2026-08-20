package mobile.clinic

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeoutOrNull

/**
 * EXERCISE 6, Coroutines: a FIRST TASTE, as SYSTEMS tools (not the deep dive, that's Lab 6/7).
 *
 * SYSTEMS FRAMING. On a mobile node you must never block the main thread, work can be
 * cancelled (the user leaves the screen), and the network is slow and unreliable. Coroutines
 * give you two systems primitives you'll lean on all course:
 *   - STRUCTURED CONCURRENCY: `coroutineScope { }` launches children that all complete (or all
 *     cancel together) before it returns: no leaked background work.
 *   - BOUNDED WAITING: `withTimeoutOrNull` caps how long you'll wait on a flaky call instead
 *     of hanging forever.
 * `suspend` marks a function that can pause without blocking a thread.
 */

/**
 * Run [a] and [b] CONCURRENTLY (not one-after-the-other) and return the sum of their results.
 * Use `coroutineScope { }` with two `async { }` children and `await()` both.
 *
 * Systems point: two independent I/O calls overlap instead of serializing, and if either
 * throws, the scope cancels the other. This is offloading + fan-out in three lines.
 */
suspend fun sumInParallel(a: suspend () -> Int, b: suspend () -> Int): Int = coroutineScope {
    val da = async { a() }
    val db = async { b() }
    da.await() + db.await()
}

/**
 * Wait for [block] but give up after [timeoutMs], returning [fallback] instead of hanging.
 * Use `withTimeoutOrNull`.
 *
 * Systems point: a bounded wait on an unreliable link is the difference between a responsive
 * app and a spinner that never ends when connectivity drops.
 */
suspend fun <T> awaitOrFallback(timeoutMs: Long, fallback: T, block: suspend () -> T): T =
    withTimeoutOrNull(timeoutMs) { block() } ?: fallback
