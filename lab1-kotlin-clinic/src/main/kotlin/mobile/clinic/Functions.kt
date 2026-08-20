package mobile.clinic

/**
 * MORE KOTLIN, functions: default & named args, vararg, single-expression
 * bodies, and EXTENSION functions.
 *
 * SYSTEMS FRAMING. Default + named arguments delete the overload explosion you'd write in Java
 * and make call-sites self-documenting. Extension functions let you add behaviour to types you
 * don't own (e.g. sanitising a value at a boundary) without inheritance. You'll use both to build
 * request URLs and adapt platform types in later labs.
 */

/**
 * Single-expression function with a DEFAULT argument. greet("Ada") -> "Hello, Ada";
 * greet("Ada", "Hi") -> "Hi, Ada". No overloads needed.
 */
fun greet(name: String, greeting: String = "Hello"): String =
    "$greeting, $name"

/**
 * EXTENSION function on Int: clamp any value into a valid percentage 0..100.
 * (-5).clampPercent() -> 0 ; 250.clampPercent() -> 100 ; 42.clampPercent() -> 42.
 * Hint: `coerceIn(min, max)`.
 */
fun Int.clampPercent(): Int =
    coerceIn(0, 100)

/**
 * VARARG + a single-expression body. Build a query string from a path and zero or more
 * key/value params:
 *   buildQuery("/search")                              -> "/search"
 *   buildQuery("/search", "q" to "kotlin", "p" to "2") -> "/search?q=kotlin&p=2"
 */
fun buildQuery(path: String, vararg params: Pair<String, String>): String =
    if (params.isEmpty()) path
    else path + "?" + params.joinToString("&") { (k, v) -> "$k=$v" }
