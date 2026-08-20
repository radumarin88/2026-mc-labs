package mobile.clinic

/**
 * EXERCISE 5, Scope functions (let / run / apply / also / with).
 *
 * SYSTEMS FRAMING. These are small tools you'll read constantly in Android code:
 *   - `apply`  configure an object and return IT (builder-style setup).
 *   - `let`    transform a nullable value only when non-null (pairs with `?.`).
 *   - `also`   run a side effect (logging/metrics) and return the value unchanged.
 * Choosing the right one is about what you want back (the receiver vs the lambda result)
 * and whether the receiver is `this` or `it`.
 */

/** A mutable config we build up then freeze into a request line. */
class RequestConfig {
    var scheme: String = "https"
    var host: String = "localhost"
    var port: Int? = null
    val headers: MutableList<String> = mutableListOf()
}

/**
 * Use `apply` to configure a RequestConfig and return the configured object.
 * Set host = [host], port = [port], and add one header "x-device: $deviceId".
 */
fun buildConfig(host: String, port: Int?, deviceId: String): RequestConfig =
    RequestConfig().apply {
        this.host = host
        this.port = port
        headers.add("x-device: $deviceId")
    }

/**
 * Render a config into a URL string. Use `let` so the port only contributes when non-null:
 *   port present -> "scheme://host:port"
 *   port null    -> "scheme://host"
 */
fun RequestConfig.toUrl(): String {
    val authority = port?.let { "$host:$it" } ?: host
    return "$scheme://$authority"
}

/**
 * Use `also` to record a side effect WITHOUT changing the returned value: append
 * "sent:$url" to [log], then return the url unchanged.
 */
fun sendAndLog(url: String, log: MutableList<String>): String =
    url.also { log.add("sent:$it") }
