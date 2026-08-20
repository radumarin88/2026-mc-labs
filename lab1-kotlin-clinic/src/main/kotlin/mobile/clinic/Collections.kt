package mobile.clinic

/**
 * MORE KOTLIN, collections & the functional pipeline (the "5 Cs":
 * Create, Convert, Change, Choose, Conclude).
 *
 * SYSTEMS FRAMING. This is the single most-used skill in the whole course after null safety:
 * you constantly transform a list of domain objects (sensor readings, cache rows, API items)
 * into what a screen or a sync job needs: filter stale entries, map to a view shape, group and
 * aggregate. The exact `map`/`filter`/`groupBy` pipelines you write here reappear as Compose list
 * state (Lab 3) and Room query post-processing (Lab 7). Prefer this declarative style over manual
 * loops: it says WHAT, not HOW.
 */

/** A single sensor sample. */
data class Reading(
    val sensorId: String,
    val type: String,      // e.g. "temp", "humidity"
    val value: Double,
    val epochMs: Long,
)

/**
 * CHOOSE + CHANGE. Keep only readings no older than [maxAgeMs] relative to [nowMs], oldest-first,
 * and return just their values.
 */
fun freshValues(readings: List<Reading>, nowMs: Long, maxAgeMs: Long): List<Double> =
    readings
        .filter { nowMs - it.epochMs <= maxAgeMs }
        .sortedBy { it.epochMs }
        .map { it.value }

/**
 * CONCLUDE per group. Average value per sensor type.
 * e.g. {"temp" -> 21.5, "humidity" -> 40.0}.
 */
fun averageByType(readings: List<Reading>): Map<String, Double> =
    readings
        .groupBy { it.type }
        .mapValues { (_, group) -> group.map { it.value }.average() }

/**
 * The latest reading per sensor id (max epochMs within each sensor's group).
 */
fun latestPerSensor(readings: List<Reading>): Map<String, Reading> =
    readings
        .groupBy { it.sensorId }
        .mapValues { (_, group) -> group.maxByOrNull { it.epochMs }!! }

/**
 * The most frequently reported type, or null if there are no readings.
 */
fun mostCommonType(readings: List<Reading>): String? =
    readings
        .groupingBy { it.type }
        .eachCount()
        .maxByOrNull { it.value }
        ?.key
