package mobile.clinic

/**
 * MORE KOTLIN, control flow as EXPRESSIONS: `when`, ranges, enums.
 *
 * SYSTEMS FRAMING. On a mobile node you constantly map a raw signal (battery %, RSSI,
 * connectivity) to a POLICY (sync now? defer? degrade?). Kotlin's `when` is an expression that
 * returns a value, is exhaustive over enums (forget a case → won't compile), and reads ranges
 * directly (`in 0..14`). This is the decision logic of the whole course, in miniature.
 */

/** The connectivity a node currently has. */
enum class Connectivity { OFFLINE, METERED, UNMETERED }

/**
 * Map connectivity to a sync policy using an EXHAUSTIVE `when` over the enum (no `else`):
 *   OFFLINE   -> "defer"        (queue locally, sync later)
 *   METERED   -> "sync-small"   (only cheap/critical data on mobile data)
 *   UNMETERED -> "sync-all"     (on Wi-Fi, do the full sync)
 */
fun syncPolicy(c: Connectivity): String =
    when (c) {
        Connectivity.OFFLINE -> "defer"
        Connectivity.METERED -> "sync-small"
        Connectivity.UNMETERED -> "sync-all"
    }

/**
 * Classify a battery percentage using `when` with RANGES. Coerce out-of-range input into 0..100.
 *   0..14 -> "critical" ; 15..49 -> "low" ; 50..89 -> "ok" ; else -> "full"
 */
fun batteryBucket(pct: Int): String =
    when (pct.coerceIn(0, 100)) {
        in 0..14 -> "critical"
        in 15..49 -> "low"
        in 50..89 -> "ok"
        else -> "full"
    }

/**
 * Label a Wi-Fi signal (RSSI in dBm, higher/less-negative = stronger) using a SUBJECT-LESS
 * `when` (boolean conditions), which reads like a guarded if/else-if chain:
 *   >= -60 "excellent" ; >= -75 "good" ; >= -85 "weak" ; else "poor"
 */
fun rssiLabel(rssi: Int): String =
    when {
        rssi >= -60 -> "excellent"
        rssi >= -75 -> "good"
        rssi >= -85 -> "weak"
        else -> "poor"
    }

/**
 * Build an EXPONENTIAL BACKOFF schedule with a loop/range: delay i = baseMs * 2^i for
 * i in 0 until attempts. e.g. backoffDelaysMs(100, 4) -> [100, 200, 400, 800].
 * Systems: never hammer a flaky link at a fixed interval, back off.
 */
fun backoffDelaysMs(baseMs: Long, attempts: Int): List<Long> =
    (0 until attempts).map { baseMs shl it }
