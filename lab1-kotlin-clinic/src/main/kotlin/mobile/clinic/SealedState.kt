package mobile.clinic

/**
 * EXERCISE 3, Sealed hierarchies & exhaustive `when` (the UiState you'll reuse all semester).
 *
 * SYSTEMS FRAMING. Every screen that loads data from disk or network has exactly three shapes:
 * loading, loaded-with-data, or failed. A `sealed interface` enumerates that CLOSED set of
 * states, and `when` over it is EXHAUSTIVE: if you later add a fourth state (e.g. Empty),
 * the compiler flags every `when` that forgot to handle it. Illegal states become
 * unrepresentable and forgotten cases become compile errors. This is the same UiState<T>
 * you will hoist into a ViewModel in Lab 5.
 */

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

/**
 * Reduce a UiState to a short human-readable string. Use an EXHAUSTIVE `when` with NO `else`
 * branch, rely on the compiler to prove you covered every case.
 *   Loading      -> "loading…"
 *   Success(d)   -> "ok: $d"
 *   Error(msg)   -> "error: $msg"
 *
 * Hint: because the interface is sealed, `when` smart-casts each branch (in Success you can
 * read `it.data`; in Error you can read `it.message`).
 */
fun <T> UiState<T>.render(): String =
    when (this) {
        is UiState.Loading -> "loading…"
        is UiState.Success -> "ok: ${this.data}"
        is UiState.Error -> "error: ${this.message}"
    }

/** Return the data if this is Success, otherwise null. Systems: a caller that can degrade. */
fun <T> UiState<T>.dataOrNull(): T? =
    (this as? UiState.Success)?.data
