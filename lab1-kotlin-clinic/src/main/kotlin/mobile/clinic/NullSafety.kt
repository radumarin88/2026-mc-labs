package mobile.clinic

/**
 * EXERCISE 1, Null safety: make "missing" explicit at the boundary.
 *
 * SYSTEMS FRAMING. A mobile node reads config, sensor values and server responses that
 * may be absent, malformed, or arrive late. In Java these are NullPointerExceptions waiting
 * to happen: runtime crashes on the user's device, in the field, offline. Kotlin's type
 * system lets you push that whole class of failure from RUNTIME to COMPILE TIME: `String?`
 * is a different type from `String`, and the compiler forces you to handle the null.
 *
 * This is the lab's ONE production concern in miniature: making illegal states
 * unrepresentable so the compiler, not your users, finds the bug.
 */

/**
 * Safely parse a battery percentage that arrived as text (e.g. from a config file or a
 * broadcast extra). Return null if it is missing or not a valid integer, never throw.
 *
 * Hint: safe-call `?.` chains through null, and `toIntOrNull()` never throws.
 */
fun batteryPctOrNull(raw: String?): Int? =
    raw?.trim()?.toIntOrNull()

/**
 * Return the configured sync endpoint, or a default when it is missing/blank.
 * Hint: the Elvis operator `?:` supplies the fallback.
 */
fun endpointOrDefault(configured: String?, default: String): String =
    configured?.takeIf { it.isNotBlank() } ?: default

/**
 * A device id is required to talk to the backend. If it is missing this is a programmer/boot
 * error, not a recoverable state, fail FAST and LOUD at the boundary with a clear message,
 * rather than letting a null leak deep into the system and crash somewhere unrelated.
 *
 * Hint: `requireNotNull(value) { "message" }` returns the non-null value or throws
 * IllegalArgumentException with your message.
 */
fun requireDeviceId(id: String?): String =
    requireNotNull(id?.takeIf { it.isNotBlank() }) { "device id is required but was missing/blank" }
