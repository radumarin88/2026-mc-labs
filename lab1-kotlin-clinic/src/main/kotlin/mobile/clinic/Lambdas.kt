package mobile.clinic

/**
 * EXERCISE 4, Lambdas, higher-order functions & the trailing-lambda idiom.
 *
 * SYSTEMS FRAMING. Compose is an API of higher-order functions: `Button(onClick = { ... })`
 * passes BEHAVIOUR as a value. The "trailing lambda" syntax `foo(x) { ... }` and
 * lambdas-with-receiver `{ this.append(...) }` are what make Compose (and Kotlin DSLs) read
 * the way they do. Coming from Java, the mental shift is: a function is a value you can pass,
 * store and return.
 */

/**
 * Reimplement `count` with a predicate, to feel what a higher-order function IS.
 * Return how many elements match [predicate]. Do NOT call the stdlib `count`: write the loop.
 */
fun <T> List<T>.countMatching(predicate: (T) -> Boolean): Int {
    TODO()
}

/**
 * A retry policy expressed as a lambda (systems: transient failures on a flaky link).
 * Call [attempt] with the attempt number 1..[maxAttempts]. [attempt] returns true on success.
 * Stop as soon as it succeeds and return the successful attempt number; return -1 if all fail.
 */
fun retryUntilSuccess(maxAttempts: Int, attempt: (Int) -> Boolean): Int {
    TODO()
}

/**
 * A tiny builder using a LAMBDA WITH RECEIVER, the shape of a Compose/Kotlin DSL.
 * Inside [build], `this` is a StringBuilder starting with "/$base". Apply the caller's
 * builder block and return the resulting string.
 *
 * Example: buildRoute("api") { append("/v1"); append("/notes") }  ->  "/api/v1/notes"
 */
fun buildRoute(base: String, build: StringBuilder.() -> Unit): String =
    TODO()
