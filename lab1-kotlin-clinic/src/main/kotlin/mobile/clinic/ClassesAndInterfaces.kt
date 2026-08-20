package mobile.clinic

/**
 * MORE KOTLIN, classes, interfaces, encapsulation, data-class destructuring.
 *
 * SYSTEMS FRAMING. Two patterns here are load-bearing for the rest of the course:
 *   1. Program to an INTERFACE, not a concretion (`Clock`). Injecting time makes code testable
 *      and deterministic: the seed of Dependency Injection (Lab 9) and unit testing (Lab 10).
 *   2. Encapsulate mutable state behind a READ-ONLY type (private `_events`, public `events`).
 *      This is exactly how you'll expose a MutableStateFlow as an immutable StateFlow in Lab 5:
 *      the outside world can observe but not mutate, which is what keeps UDF (and threads) sane.
 */

/** An abstraction over "what time is it": inject it instead of calling System.currentTimeMillis(). */
interface Clock {
    fun nowMs(): Long
}

/** A deterministic Clock for tests, always returns [fixed]. */
class FixedClock(private val fixed: Long) : Clock {
    override fun nowMs(): Long = TODO()
}

/**
 * Encapsulated append-only buffer. The mutable list is PRIVATE; the public view is read-only.
 * Implement [record] (append) and the [events] getter (expose the contents as a read-only List).
 */
class EventBuffer {
    private val _events = mutableListOf<String>()

    /** Read-only view of recorded events (callers cannot mutate the buffer through this). */
    val events: List<String>
        get() = TODO()

    /** Append [event] to the buffer. */
    fun record(event: String) {
        TODO()
    }
}

/** A user session; immutable value. */
data class Session(val id: String, val user: String, val startedMs: Long)

/**
 * Return a NEW session with startedMs refreshed to `clock.nowMs()` (copy + injected Clock).
 * Demonstrates dependency inversion (Clock) + immutable transition (copy).
 */
fun renew(session: Session, clock: Clock): Session =
    TODO()
