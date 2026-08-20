package mobile.clinic

import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * The tests you must make pass. Run: `./gradlew test`
 * Each nested class maps to one exercise file, in the same order as the README (1 to 10).
 * Do not edit the tests: edit the src/ files.
 */
class ClinicTest {

    @Nested
    inner class ControlFlow {
        @Test fun `syncPolicy maps every connectivity`() {
            assertEquals("defer", syncPolicy(Connectivity.OFFLINE))
            assertEquals("sync-small", syncPolicy(Connectivity.METERED))
            assertEquals("sync-all", syncPolicy(Connectivity.UNMETERED))
        }

        @Test fun `batteryBucket uses ranges and coerces`() {
            assertEquals("critical", batteryBucket(5))
            assertEquals("low", batteryBucket(30))
            assertEquals("ok", batteryBucket(80))
            assertEquals("full", batteryBucket(100))
            assertEquals("full", batteryBucket(250)) // coerced into range
            assertEquals("critical", batteryBucket(-9))
        }

        @Test fun `rssiLabel uses a subjectless when`() {
            assertEquals("excellent", rssiLabel(-50))
            assertEquals("good", rssiLabel(-70))
            assertEquals("weak", rssiLabel(-80))
            assertEquals("poor", rssiLabel(-100))
        }

        @Test fun `backoff doubles each attempt`() {
            assertEquals(listOf(100L, 200L, 400L, 800L), backoffDelaysMs(100, 4))
            assertEquals(emptyList<Long>(), backoffDelaysMs(100, 0))
        }
    }

    @Nested
    inner class Functions {
        @Test fun `greet uses default and named args`() {
            assertEquals("Hello, Ada", greet("Ada"))
            assertEquals("Hi, Ada", greet("Ada", "Hi"))
            assertEquals("Hi, Ada", greet(greeting = "Hi", name = "Ada"))
        }

        @Test fun `clampPercent is an extension that coerces`() {
            assertEquals(0, (-5).clampPercent())
            assertEquals(100, 250.clampPercent())
            assertEquals(42, 42.clampPercent())
        }

        @Test fun `buildQuery handles zero and many varargs`() {
            assertEquals("/search", buildQuery("/search"))
            assertEquals("/search?q=kotlin&p=2", buildQuery("/search", "q" to "kotlin", "p" to "2"))
        }
    }

    @Nested
    inner class Lambdas {
        @Test fun `countMatching counts via a predicate`() {
            assertEquals(2, listOf(1, 2, 3, 4).countMatching { it % 2 == 0 })
            assertEquals(0, emptyList<Int>().countMatching { true })
        }

        @Test fun `retry stops at first success`() {
            assertEquals(3, retryUntilSuccess(5) { attempt -> attempt == 3 })
            assertEquals(-1, retryUntilSuccess(3) { false })
        }

        @Test fun `buildRoute uses a lambda with receiver`() {
            val route = buildRoute("api") {
                append("/v1")
                append("/notes")
            }
            assertEquals("/api/v1/notes", route)
        }
    }

    @Nested
    inner class NullSafety {
        @Test fun `parses a valid battery percentage`() {
            assertEquals(87, batteryPctOrNull(" 87 "))
        }

        @Test fun `returns null for missing or malformed input`() {
            assertNull(batteryPctOrNull(null))
            assertNull(batteryPctOrNull("full"))
        }

        @Test fun `endpoint falls back when missing or blank`() {
            assertEquals("https://api.example", endpointOrDefault(null, "https://api.example"))
            assertEquals("https://api.example", endpointOrDefault("   ", "https://api.example"))
            assertEquals("https://custom", endpointOrDefault("https://custom", "https://api.example"))
        }

        @Test fun `requireDeviceId returns id or fails loudly`() {
            assertEquals("dev-1", requireDeviceId("dev-1"))
            assertThrows(IllegalArgumentException::class.java) { requireDeviceId(null) }
            assertThrows(IllegalArgumentException::class.java) { requireDeviceId("  ") }
        }
    }

    @Nested
    inner class DataClasses {
        @Test fun `transitions are immutable and produce new values`() {
            val initial = SyncState.INITIAL
            val syncing = initial.startSync()
            assertEquals(false, initial.isSyncing) // original untouched
            assertEquals(true, syncing.isSyncing)

            val queued = syncing.enqueue(3)
            assertEquals(3, queued.pendingCount)

            val done = queued.completeSync(nowMs = 1_000L)
            assertEquals(false, done.isSyncing)
            assertEquals(0, done.pendingCount)
            assertEquals(1_000L, done.lastSyncEpochMs)
        }

        @Test fun `value equality via data class`() {
            assertEquals(
                SyncState(pendingCount = 2, lastSyncEpochMs = null, isSyncing = false),
                SyncState(pendingCount = 2, lastSyncEpochMs = null, isSyncing = false),
            )
        }
    }

    @Nested
    inner class ClassesAndInterfaces {
        @Test fun `FixedClock injects deterministic time`() {
            val clock: Clock = FixedClock(1_234L)
            assertEquals(1_234L, clock.nowMs())
        }

        @Test fun `EventBuffer records and exposes a read-only view`() {
            val buffer = EventBuffer()
            buffer.record("boot")
            buffer.record("sync")
            assertEquals(listOf("boot", "sync"), buffer.events)
        }

        @Test fun `renew refreshes startedMs via injected clock`() {
            val original = Session(id = "1", user = "ada", startedMs = 0)
            val renewed = renew(original, FixedClock(5_000L))
            assertEquals(5_000L, renewed.startedMs)
            assertEquals("1", renewed.id)          // copy kept the rest
            assertEquals(0L, original.startedMs)   // original untouched
        }
    }

    @Nested
    inner class SealedState {
        @Test fun `render covers every state`() {
            assertEquals("loading…", (UiState.Loading).render())
            assertEquals("ok: 42", (UiState.Success(42)).render())
            assertEquals("error: boom", (UiState.Error("boom")).render())
        }

        @Test fun `dataOrNull degrades safely`() {
            assertEquals("payload", UiState.Success("payload").dataOrNull())
            assertNull((UiState.Loading as UiState<String>).dataOrNull())
            assertNull((UiState.Error("x") as UiState<String>).dataOrNull())
        }
    }

    @Nested
    inner class Collections {
        private val now = 10_000L
        private val readings = listOf(
            Reading("s1", "temp", 20.0, 9_000),
            Reading("s1", "temp", 22.0, 9_800),
            Reading("s2", "humidity", 40.0, 3_000),   // stale
            Reading("s2", "humidity", 44.0, 9_900),
            Reading("s3", "temp", 24.0, 9_950),
        )

        @Test fun `freshValues filters stale, sorts, maps`() {
            // maxAge 1000ms from now=10000 -> keep epoch >= 9000; drop the 3000 one; oldest-first
            assertEquals(listOf(20.0, 22.0, 44.0, 24.0), freshValues(readings, now, maxAgeMs = 1_000))
        }

        @Test fun `averageByType aggregates per group`() {
            val avg = averageByType(readings)
            assertEquals(22.0, avg["temp"])          // (20+22+24)/3
            assertEquals(42.0, avg["humidity"])      // (40+44)/2
        }

        @Test fun `latestPerSensor picks max timestamp`() {
            val latest = latestPerSensor(readings)
            assertEquals(22.0, latest["s1"]!!.value)
            assertEquals(44.0, latest["s2"]!!.value)
            assertEquals(24.0, latest["s3"]!!.value)
        }

        @Test fun `mostCommonType counts groups`() {
            assertEquals("temp", mostCommonType(readings)) // 3 temp vs 2 humidity
            assertNull(mostCommonType(emptyList()))
        }
    }

    @Nested
    inner class ScopeFunctions {
        @Test fun `apply configures and returns the object`() {
            val cfg = buildConfig(host = "h", port = 8080, deviceId = "d1")
            assertEquals("h", cfg.host)
            assertEquals(8080, cfg.port)
            assertEquals(listOf("x-device: d1"), cfg.headers)
        }

        @Test fun `let omits a null port`() {
            assertEquals("https://h:8080", buildConfig("h", 8080, "d1").toUrl())
            assertEquals("https://h", buildConfig("h", null, "d1").toUrl())
        }

        @Test fun `also logs without changing the value`() {
            val log = mutableListOf<String>()
            val url = sendAndLog("https://h", log)
            assertEquals("https://h", url)
            assertEquals(listOf("sent:https://h"), log)
        }
    }

    @Nested
    inner class Coroutines {
        @Test fun `sumInParallel overlaps two suspend calls`() = runTest {
            // With virtual time, two 1000ms delays that run concurrently finish at ~1000ms,
            // not 2000ms. runTest auto-advances virtual time.
            val start = testScheduler.currentTime
            val total = sumInParallel(
                a = { delay(1000); 20 },
                b = { delay(1000); 22 },
            )
            assertEquals(42, total)
            assertEquals(1000, testScheduler.currentTime - start) // proves parallel, not serial
        }

        @Test fun `awaitOrFallback returns the value when fast enough`() = runTest {
            val v = awaitOrFallback(timeoutMs = 1000, fallback = -1) { delay(10); 7 }
            assertEquals(7, v)
        }

        @Test fun `awaitOrFallback falls back when too slow`() = runTest {
            val v = awaitOrFallback(timeoutMs = 50, fallback = -1) { delay(10_000); 7 }
            assertEquals(-1, v)
        }
    }
}
