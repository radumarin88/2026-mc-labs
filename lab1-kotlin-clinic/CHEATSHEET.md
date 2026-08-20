# Kotlin for Java/C++ developers: one-page cheatsheet

Companion to the Lab 1 walkthrough. This is *spelling*, not new concepts, you already know the
ideas. Official deep-dives are linked from the README.

## Variables & types
| Java | Kotlin | Note |
|---|---|---|
| `final int x = 10;` | `val x = 10` | `val` = read-only; type inferred |
| `int x = 10;` | `var x = 10` | `var` = reassignable |
| `long`, `float` | `10L`, `0.2f` | no implicit widening: `x.toLong()` |
| `String s = null;` | `var s: String? = null` | `String` and `String?` are **different types** |
| `int[]`, `List<Integer>` | `IntArray`, `List<Int>` | `Int` boxes to nothing on JVM in `IntArray` |

## Nullability (the big one)
```kotlin
val n: Int? = s?.length          // safe call: null if s is null
val len = s?.length ?: 0         // Elvis: fallback when null
if (s != null) s.length          // smart cast: s is String inside the block
requireNotNull(s) { "required" } // fail fast at a boundary with a message
s?.let { use(it) }               // run only when non-null (also captures a mutable var safely)
```

## Control flow: everything is an expression
```kotlin
val label = if (x > 0) "pos" else "neg"
val bucket = when (pct) { in 0..14 -> "critical"; in 15..49 -> "low"; else -> "ok" }
val policy = when (conn) { OFFLINE -> "defer"; METERED -> "small"; UNMETERED -> "all" } // exhaustive, no else
for (i in 1..100) { }        // inclusive       for (i in 1 until 100) { }  // exclusive
for (i in 1..100 step 2) { } // progression     for (i in 100 downTo 1) { } // descending
```

## Functions
```kotlin
fun inc(x: Int): Int = x + 1                       // single-expression body
fun greet(name: String, greeting: String = "Hi")   // default arg, no overloads
greet(name = "Ada")                                 // named arg, any order
fun sum(vararg xs: Int) { }                         // vararg
fun String.shout() = this.uppercase()               // extension function
fun crash(): Nothing = throw IllegalStateException() // never returns
```

## Lambdas & higher-order functions
```kotlin
val add: (Int, Int) -> Int = { a, b -> a + b }     // function type + lambda
list.forEach { println(it) }                        // `it` = single implicit param
retry(3) { attempt -> attempt == 3 }                // trailing lambda goes outside ()
build { append("x") }                               // lambda with receiver: `this` = builder
list.map { _ -> 0 }                                 // `_` = unused param
```

## Collections: the 5 Cs
```kotlin
listOf(1,2,3); mutableListOf(); setOf(); mapOf(1 to "one")  // Create
list.toSet(); list.toList()                                 // Convert
list.map { it * 2 }; list.filter { it > 0 }                 // Change / Choose
list.sum(); list.average(); list.groupBy { it.type }        // Conclude
list.firstOrNull { it == x } ?: default                     // safe access
```

## Classes, data & sealed
```kotlin
class Car(private val engine: Engine)               // primary constructor
open class Base { open fun f() {} }                 // `open` to allow subclass/override
class Sub : Base() { override fun f() {} }
interface Clock { fun nowMs(): Long }               // program to interfaces (DI later)
val events: List<String> get() = _events            // read-only view over private mutable

data class User(val id: String, val name: String)   // equals/hashCode/toString/copy/destructure
val u2 = u.copy(name = "New")                        // immutable transition
val (id, name) = u                                   // destructuring

sealed interface Result                              // closed set → exhaustive `when`
data class Ok(val data: String) : Result
data object Loading : Result
```

## Gotchas coming from Java
- `==` calls `equals()` (structural); `===` is reference identity (the opposite of Java).
- No checked exceptions; no ternary `?:` in the Java sense (`?:` is Elvis, use `if/else`).
- No `static`: use `object` or a `companion object`.
- Everything is `public` and `final` by default; add `open` to allow inheritance.
- `Unit` is Kotlin's `void`; `Nothing` means "never returns".
