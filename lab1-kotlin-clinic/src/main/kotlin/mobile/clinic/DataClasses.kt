package mobile.clinic

/**
 * EXERCISE 2, Data classes & immutable state (the shape of UDF state).
 *
 * SYSTEMS FRAMING. Compose (Labs 2+) is built on Unidirectional Data Flow: state flows down,
 * events flow up, and the UI is a pure function of an IMMUTABLE state value. You never mutate
 * state in place; you compute a NEW state and emit it. Data classes give you value semantics
 * (equals/hashCode/toString) and `copy(...)` for cheap "change one field" transitions, the
 * exact idiom you'll use for ViewModel state later.
 */

/**
 * A snapshot of a background-sync feature's state. Note: no `var`, everything is `val`:
 * an instance is immutable once created. `lastSyncEpochMs` is nullable because "never synced"
 * is a real, distinct state (Exercise 1's lesson applied to a field).
 */
data class SyncState(
    val pendingCount: Int,
    val lastSyncEpochMs: Long?,
    val isSyncing: Boolean,
) {
    companion object {
        /** The initial state of a node that has just booted and never synced. */
        val INITIAL = SyncState(pendingCount = 0, lastSyncEpochMs = null, isSyncing = false)
    }
}

/** Transition: a sync just began. Return a NEW state with isSyncing = true (do not mutate). */
fun SyncState.startSync(): SyncState =
    TODO()

/**
 * Transition: a sync just finished successfully at [nowMs]. Return a NEW state that is no
 * longer syncing, has zero pending items, and records the timestamp.
 */
fun SyncState.completeSync(nowMs: Long): SyncState =
    TODO()

/** Transition: [n] new items were queued while offline. Return a NEW state with them added. */
fun SyncState.enqueue(n: Int): SyncState =
    TODO()
