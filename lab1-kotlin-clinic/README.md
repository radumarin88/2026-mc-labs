# Lab 1: Kotlin Clinic

Welcome! This is your first lab, and it is all about **Kotlin**, the language you will use for the rest of the course. You already know how to program, so this is not "learn to code." It is a fast tour of the handful of Kotlin ideas that matter most for building apps that run on a phone: a device that is often low on battery, short on memory, and drifting in and out of signal.

The lab is **one task made of ten short exercises**. Each exercise is a small function with its body missing (marked `TODO()`) and a unit test that currently fails. **Your job is to fill in the code until every test passes.** That is it: when the tests are green, you are done.

This is **individual, at-home work** you do at your own pace - budget about two hours for all ten. Finish every exercise before you move on to Lab 2, because it is one continuous task, not two.

---

## Part 1: Before you start

### 1a. Warm up with a little Kotlin

If you have never written Kotlin, spend an hour or two here first. Both resources are free and self-paced:

- **[Kotlin Koans](https://kotlinlang.org/docs/koans.html)** is a set of tiny exercises made *for people coming from Java*. Each one is a failing test you make pass, the exact format of this lab. Do them online in the **[Koans playground](https://play.kotlinlang.org/koans/overview)** (click **Run** to check, **Show answer** only after you have tried), or inside an IDE with the **JetBrains Academy** plugin. If you are short on time, just do **Introduction**, **Classes**, and **Null safety**: they line up directly with the exercises below.
- **[Android Basics with Compose, Unit 1, Pathway 1](https://developer.android.com/courses/pathways/android-basics-compose-unit-1-pathway-1)** is Google's gentle intro to Kotlin. If you are an experienced programmer, skim it just to learn Kotlin's *spelling* of things you already know. You do not need Android Studio for this lab, so you can stop after Pathway 1.

Keep **[`CHEATSHEET.md`](./CHEATSHEET.md)** (in this folder) open while you work: it is a one-page Java/C++ to Kotlin reference.

### 1b. Install your tools

You need two things: a **Java 21 JDK** (Kotlin compiles to run on it, and Gradle needs it) and an **editor with Kotlin support**. You do **not** need Android Studio yet, that comes in Lab 2.

#### Step 1: Install a Java 21 JDK

Pick the line for your operating system, run it in a terminal, then check it worked with `java -version` (it should print `21`).

- **macOS** (with [Homebrew](https://brew.sh)):
  ```bash
  brew install --cask temurin@21
  ```
- **Windows** (with winget, built into Windows 10/11):
  ```powershell
  winget install EclipseAdoptium.Temurin.21.JDK
  ```
  Then open a **new** terminal so `java` is on your PATH.
- **Linux (Ubuntu/Debian):**
  ```bash
  sudo apt update && sudo apt install openjdk-21-jdk
  ```

No package manager? Download an installer for any OS from **[adoptium.net](https://adoptium.net/temurin/releases/?version=21)**. On macOS or Linux you can also use **[SDKMAN](https://sdkman.io)**: `sdk install java 21.0.4-tem`.

#### Step 2: Install an editor

**Recommended: IntelliJ IDEA (free).** It has the best Kotlin and Gradle support, so you get autocomplete, error highlighting, and you can run a test by clicking the ▶ arrow next to it.

1. Download it from **[jetbrains.com/idea/download](https://www.jetbrains.com/idea/download/)**. There is a single download now, and the **free tier is enabled by default** on first launch, with no purchase and no sign-up needed. (Bonus: the JetBrains Academy plugin lets you do the Koans above right inside the IDE.)
2. Install and open it.

**Lighter alternative: VS Code.** If you already live in VS Code, install the official **"Kotlin" extension by JetBrains**. It is newer (in alpha as of 2026) but supports exactly this kind of project (a plain JVM Gradle project), giving you completion and error checking. You will run the tests from the terminal (below).

#### Step 3: Open the project and run the tests

This repository holds all the labs side by side. Open the folder for this lab:

```bash
cd lab1-kotlin-clinic
```

- **In IntelliJ IDEA:** *File → Open* and pick the `lab1-kotlin-clinic` folder. Wait for it to import the Gradle project (first time only, it downloads Kotlin, Gradle, and the libraries, so you need internet once). Then run the tests: open the built-in terminal and run `./gradlew test`, or right-click the `test` folder and choose *Run Tests*.
- **In VS Code or any terminal:**
  ```bash
  ./gradlew test        # on Windows: gradlew.bat test
  ```

The project will **compile** and **most tests will fail** with `NotImplementedError`. That is your starting line: those are the `TODO()`s waiting for you.

---

## Part 2: How to work

**The one idea to take away:** Kotlin lets you turn *"this might crash at runtime"* into *"this will not compile."* A value that might be missing has a different type (`String?`) you are **forced** to handle. A fixed set of states you list once (a `sealed` type) makes the compiler **check** you handled them all. Immutable data means nothing changes behind your back. Across all ten exercises, watch how the compiler catches mistakes that in other languages you would only discover when the app crashed on someone's phone. Making bad states impossible to even write down is the habit the whole course builds on.

**Your loop for each exercise:**

1. Open the file for the section (they all live under `src/main/kotlin/mobile/clinic/`).
2. Replace the `TODO(...)` with real code. The doc-comment above each function explains what it should do and gives a hint.
3. Run just that section's tests while you iterate, for example:
   ```bash
   ./gradlew test --tests "*ControlFlow*"
   ```
4. When it is green, move on. Run everything with `./gradlew test` at the end.

**Only edit the files under `src/main/`. Do not change the tests** in `src/test/`, because they define what "correct" means.

Stuck? Re-read the hint, check the cheatsheet, or look up the linked docs. A reference solution exists on the `lab1-solution` branch, but try first, and when you peek, read *why* it works rather than copying.

---

## Part 3: The ten exercises

Do them **in order**, because each builds on the last. Sections 1 to 3 are the language basics; 4 to 7 are how you model data and state; 8 to 10 are the tools you will reach for constantly in real apps.

### 1 · Control flow & expressions: `ControlFlow.kt`

Kotlin has no `switch`; it has `when`, which is more capable. And unlike Java, both `if` and `when` are **expressions**: the last line of the chosen branch becomes the value, so you can assign the result straight into a `val`.

`when` comes in a few shapes:
- **Match a value** against constants or ranges. A range like `0..14` is inclusive; `1 until 10` excludes the end; `10 downTo 1` counts down; `step 2` skips. You test membership with `in`.
- **No subject at all**: `when { condA -> …; condB -> …; else -> … }` takes the first branch whose condition is true, exactly like a chain of `if / else if`.
- **Over an `enum`** (or a sealed type, section 7) it is **exhaustive**: cover every constant and you need no `else`, and if someone adds a constant later the compiler flags every `when` that forgot it.

For repetition you loop over a range with `for (i in 1..n)`, and you can turn a range into a list using the collection tools from section 8.

```kotlin
val grade = when (score) {        // match against ranges, returns a value
    in 90..100 -> "A"
    in 80..89  -> "B"
    else       -> "C"
}

val sign = when {                 // no subject: first true branch wins
    n > 0 -> "positive"
    n < 0 -> "negative"
    else  -> "zero"
}
```

Why it matters on a phone: turning a raw reading (battery %, signal strength, whether you are online) into a *decision* (sync now? wait? use less?) is logic you will write all the time. One of the exercises builds an exponential backoff, the standard way to retry a shaky network without hammering it.

**Do it:** implement `syncPolicy`, `batteryBucket`, `rssiLabel`, `backoffDelaysMs`. Make the `ControlFlow` tests pass.

**Docs:** [Conditions & loops](https://kotlinlang.org/docs/control-flow.html) · [Ranges](https://kotlinlang.org/docs/ranges.html) · [Enum classes](https://kotlinlang.org/docs/enum-classes.html)

### 2 · Functions: `Functions.kt`

A Kotlin function can have a **block body** (`fun f(): Int { return 1 }`) or, when it is a single expression, a shorter **expression body** (`fun f() = 1`) where the return type is inferred and `return` is omitted.

- **Default arguments** give a parameter a fallback value, so one function replaces the stack of overloads you would write in Java. Callers can also pass **named arguments** (`f(width = 10, height = 5)`) in any order, which makes the call self-documenting.
- **`vararg`** lets a function accept zero or more values of a type; inside the function that parameter behaves like an array you can loop over.
- **Extension functions** add a method to a type you do not own. `fun String.shout()` reads as though `shout()` were built into `String`, and inside it `this` is the receiver. Nothing is really subclassed (they are resolved statically), but call sites stay clean.

```kotlin
fun connect(host: String, port: Int = 443): String = "$host:$port"  // default arg + expression body
connect("example.com")               // uses 443
connect("example.com", port = 8080)  // named argument

fun sumAll(vararg xs: Int): Int { /* loop over xs */ }
fun String.shout(): String = uppercase() + "!"   // extension: `this` is the String
```

Why it matters: you will build things like request URLs and adapt platform types exactly this way later, and a small extension that cleans up a value is nicer than a bag of utility classes.

**Do it:** implement `greet`, `clampPercent`, `buildQuery`. Make the `Functions` tests pass.

**Docs:** [Functions](https://kotlinlang.org/docs/functions.html) · [Extensions](https://kotlinlang.org/docs/extensions.html)

### 3 · Lambdas & higher-order functions: `Lambdas.kt`

In Kotlin a function is a value with a **function type** such as `(Int) -> Boolean` (takes an `Int`, returns a `Boolean`). A **lambda** is a literal of that type, written in braces: `{ n -> n > 0 }`. When the lambda has a single parameter you can omit the arrow and refer to it as `it`: `{ it > 0 }`.

A **higher-order function** takes a function as a parameter, or returns one. That is how you pass *behaviour* into other code: a predicate to filter by, an action to run on a tap, a policy to retry with.

Two pieces of syntax you will meet constantly:
- **Trailing lambda**: when the last argument is a lambda it moves outside the parentheses, and if it is the only argument the parentheses disappear. `repeat(3) { … }` is the same call as `repeat(3, { … })`.
- **Lambda with receiver**: a type like `StringBuilder.() -> Unit` means that inside the lambda `this` is a `StringBuilder`, so you call its members directly. This is what powers builder DSLs and Compose.

```kotlin
val isEven: (Int) -> Boolean = { it % 2 == 0 }   // a function stored in a value
listOf(1, 2, 3).forEach { println(it) }          // `it` is the single parameter

fun repeatN(n: Int, action: () -> Unit) { /* call action() n times */ }
repeatN(3) { println("hi") }                      // trailing lambda

val s = buildString { append("a"); append("b") }  // receiver lambda: `this` is a StringBuilder
```

Why it matters: the UI toolkit you will use next (Compose) is built entirely on this. `Button(onClick = { … })` is just a function taking a function, so getting comfortable now pays off immediately.

**Do it:** implement `countMatching`, `retryUntilSuccess`, `buildRoute`. Make the `Lambdas` tests pass.

**Docs:** [Higher-order functions & lambdas](https://kotlinlang.org/docs/lambdas.html)

### 4 · Null safety: `NullSafety.kt`

In Kotlin, `null` lives in the type system. `String` can never be null; `String?` might be. Because they are different types, the compiler makes you handle the `null` case before you can use a nullable value, which removes most `NullPointerException`s before the app ever runs.

The everyday tools:
- **Safe call** `a?.b` gives `null` if `a` is null instead of throwing, and it chains: `a?.b?.c`.
- **Elvis** `a ?: fallback` supplies a value to use when the left side is null.
- **Smart casts**: after `if (x != null) { … }`, inside the block `x` is treated as non-null automatically.
- **Safe conversions** such as `toIntOrNull()` return `null` on bad input rather than throwing, so parsing untrusted text never blows up.
- **Fail fast** with `requireNotNull(x) { "message" }` (or `checkNotNull`): if `x` is null it throws right there with your message, which is what you want at a boundary for something that must exist. Avoid `!!`, which throws a bare exception and hides why.

```kotlin
val name: String? = user.nickname            // may be null
val length: Int = name?.length ?: 0          // safe call, then Elvis fallback
if (name != null) println(name.uppercase())  // smart-cast to non-null inside the block
val id = requireNotNull(config["id"]) { "id is required" }
```

Why it matters: on a phone, missing or malformed data (a dropped sensor read, an absent setting, a half-finished response) is *normal*. Null safety turns "will crash on someone's device" into "will not compile."

**Do it:** implement `batteryPctOrNull`, `endpointOrDefault`, `requireDeviceId`. Make the `NullSafety` tests pass.

**Docs:** [Null safety](https://kotlinlang.org/docs/null-safety.html) · [Nullability: coming from Java](https://kotlinlang.org/docs/java-to-kotlin-nullability-guide.html)

### 5 · Data classes & immutable state: `DataClasses.kt`

Mark a class `data` and the compiler generates `equals`, `hashCode`, `toString`, `copy`, and the `componentN` functions from the properties in its primary constructor. Two data instances with equal fields are equal, which is what you almost always want for a value.

The habit to build is **immutability**. Declare fields as `val` so an instance never changes after it is created. To represent a change you do not mutate in place; you derive a **new** instance with `copy(...)`, overriding only the fields that differ. The old instance stays valid and untouched, which is what keeps state predictable when several parts of the app read it at once.

```kotlin
data class Point(val x: Int, val y: Int)

val a = Point(1, 2)
val b = a.copy(y = 9)   // Point(1, 9); `a` is still Point(1, 2)
a == Point(1, 2)        // true: value equality for free
```

Why it matters: this is exactly how app screens hold their state later, one immutable value that is replaced whenever something changes. Immutable data is also far easier to reason about when more than one thing is happening at once.

**Do it:** implement `startSync`, `completeSync`, `enqueue`. Make the `DataClasses` tests pass.

**Docs:** [Data classes](https://kotlinlang.org/docs/data-classes.html)

### 6 · Classes, interfaces & encapsulation: `ClassesAndInterfaces.kt`

A Kotlin class declares its constructor and properties together, for example `class Account(val owner: String, var balance: Int)`. Members are `public` and `final` by default; add `open` to allow overriding.

- An **interface** declares behaviour without state (its methods may have default bodies). A class implements it with `: Name` and `override`. Depending on an interface rather than a concrete class is **dependency inversion**: pass the real implementation in production and a simple fake in a test. That is the main way you make code testable.
- **Encapsulation**: keep the mutable part private and expose a read-only view. The common pattern is a private `_items` (a `MutableList`) plus a public `val items: List<T>` whose getter returns it, so callers can read but not add or remove. Visibility modifiers are `public` (default), `internal` (same module), and `private`.
- **Destructuring** pulls a data object apart into locals: `val (owner, balance) = account`, using the generated `componentN` functions. Use `_` to skip a part you do not need.

```kotlin
interface Greeter { fun greet(name: String): String }
class Formal : Greeter { override fun greet(name: String) = "Good day, $name" }

class Counter {
    private var _value = 0           // only this class can change it
    val value: Int get() = _value    // outsiders can read, not write
    fun increment() { _value++ }
}
```

Why it matters: passing in an interface (like a clock) makes behaviour controllable in tests instead of tied to the real world. Exposing mutable state as read-only is the exact shape you will use to publish a screen's state safely, where observers can look but not poke.

**Do it:** implement `FixedClock.nowMs`, `EventBuffer` (`events` and `record`), and `renew`. Make the `ClassesAndInterfaces` tests pass.

**Docs:** [Classes](https://kotlinlang.org/docs/classes.html) · [Interfaces](https://kotlinlang.org/docs/interfaces.html) · [Properties & backing fields](https://kotlinlang.org/docs/properties.html) · [Destructuring](https://kotlinlang.org/docs/destructuring-declarations.html)

### 7 · Sealed types & exhaustive `when`: `SealedState.kt`

A `sealed` interface or class defines a **closed set** of subtypes: they are all declared together, so the compiler knows the complete list. The subtypes are ordinary classes. Use a `data class` for a case that carries data and a `data object` for a case that does not (a singleton).

Because the set is closed, a `when` over a sealed type is **exhaustive**: cover every subtype and you need no `else`. Add a new subtype later and every `when` that does not handle it stops compiling, which is the point: the compiler hands you a checklist of places to update. Inside each `is` branch Kotlin **smart-casts** the value to that subtype, so you can read its fields directly. To pick one case out without a full `when`, use a **safe cast** `value as? Subtype`, which is the typed value or `null` for any other case.

The exercise file already defines a `UiState` sealed type for you; you write the functions over it.

```kotlin
sealed interface Shape
data class Circle(val r: Double) : Shape
data class Rect(val w: Double, val h: Double) : Shape
data object Unknown : Shape

fun area(s: Shape): Double = when (s) {   // exhaustive: no `else` needed
    is Circle -> Math.PI * s.r * s.r       // smart-cast to Circle here
    is Rect   -> s.w * s.h
    Unknown   -> 0.0
}
```

Why it matters: anything that loads data has exactly three shapes, loading, loaded, or failed. Model them as a sealed type and the compiler makes you answer *"what do I show while it is loading? on error?"*, the questions that are easy to forget until the app is offline.

**Do it:** implement `render` (exhaustive `when`, **no `else`**) and `dataOrNull`. Make the `SealedState` tests pass.

**Docs:** [Sealed classes & interfaces](https://kotlinlang.org/docs/sealed-classes.html) · [`when`](https://kotlinlang.org/docs/control-flow.html#when-expressions-and-statements) · [Type checks & casts](https://kotlinlang.org/docs/typecasts.html)

### 8 · Collections & the functional pipeline: `Collections.kt`

Kotlin's standard library has read-only collections (`List`, `Set`, `Map`) and mutable variants (`MutableList`, and so on). You create them with `listOf(…)`, `setOf(…)`, `mapOf(k to v)`. The power is a large family of **transformation functions** that each take a lambda and return a new collection, so you describe *what* you want instead of writing loops.

The building blocks:
- **Change** each element: `map { … }` returns a new list of transformed elements.
- **Choose** elements: `filter { … }` keeps the ones that match; `sortedBy { … }` orders them.
- **Conclude** into a single value: `sum()`, `average()`, `count()`, `firstOrNull { … }`, `maxByOrNull { … }`.
- **Group**: `groupBy { … }` builds a `Map` from a key to the list of items with that key, and you then transform each group (for example with `mapValues`).

Each step returns a collection, so you **chain** them and read the pipeline top to bottom. Reach for these instead of manual loops.

```kotlin
val nums = listOf(1, 2, 3, 4, 5)
nums.filter { it % 2 == 0 }   // [2, 4]     keep some
    .map { it * it }          // [4, 16]    transform each
    .sum()                    // 20         reduce to one value

data class Person(val name: String, val city: String)
people.groupBy { it.city }    // Map<String, List<Person>>: one entry per city
```

Why it matters: you will shape lists of data (sensor readings, cached rows, items from a server) into what a screen needs exactly like this, over and over.

**Do it:** implement `freshValues`, `averageByType`, `latestPerSensor`, `mostCommonType`. Make the `Collections` tests pass.

**Docs:** [Collections overview](https://kotlinlang.org/docs/collections-overview.html) · [Operations](https://kotlinlang.org/docs/collection-operations.html) · [Grouping](https://kotlinlang.org/docs/collection-grouping.html)

### 9 · Scope functions: `ScopeFunctions.kt`

The scope functions (`let`, `run`, `with`, `apply`, `also`) all run a block "in the context of" an object. They differ on just two axes, and that is the whole thing to learn:
- **How the object appears inside the block**: as `this` (the receiver, so you call its members directly) or as `it` (an ordinary parameter you can rename).
- **What the call returns**: the object itself, or the result of the block.

| function | object inside is | returns | typical use |
|---|---|---|---|
| `apply` | `this` | the object | configure an object, then hand it back |
| `also`  | `it`   | the object | run a side effect (log, validate) and pass the value on |
| `let`   | `it`   | block result | transform a value, often only when non-null via `?.let` |
| `run` / `with` | `this` | block result | compute something from an object |

They overlap, so the goal is readability: choose the one whose "returns" and "inside is" match what you want, and do not nest them deeply.

```kotlin
val sb = StringBuilder().apply { append("a"); append("b") }  // returns the StringBuilder
val upper = nickname?.let { it.uppercase() }                 // returns the result, only if non-null
val saved = order.also { log.add("saved ${it.id}") }         // returns order, runs a side effect
```

Why it matters: you will read these constantly in real Kotlin, and choosing the right one keeps the flow of data obvious.

**Do it:** implement `buildConfig`, `toUrl`, `sendAndLog`. Make the `ScopeFunctions` tests pass.

**Docs:** [Scope functions](https://kotlinlang.org/docs/scope-functions.html)

### 10 · Coroutines, a first taste: `Coroutines.kt`

Coroutines are how Kotlin does asynchronous work without a tangle of callbacks. A function marked **`suspend`** can pause at certain points and resume later **without blocking its thread**, so one thread can serve many operations that are waiting. You call a `suspend` function from another `suspend` function or from a coroutine builder.

Two ideas you need here (the full treatment comes later in the course):
- **Structured concurrency**: work runs inside a **scope**. `coroutineScope { … }` does not return until every child started inside it has finished, and if one child fails or you cancel the scope, the siblings are cancelled too. Nothing leaks or outlives the scope.
- **Running things at once**: inside a scope, `async { … }` starts a piece of work immediately and returns a `Deferred<T>`, a handle to the eventual result. Calling `.await()` suspends until that result is ready. If you start several with `async` before you `await` any, they overlap instead of running one after the other.
- **Bounded waiting**: `withTimeoutOrNull(ms) { … }` runs the block but gives up after `ms`, returning `null` rather than hanging forever, so a slow call cannot freeze you.

```kotlin
suspend fun fetch(id: Int): Int { delay(100); return id * 2 }  // suspends, does not block

val handle = async { fetch(1) }   // starts now, returns a Deferred<Int>
val result = handle.await()        // suspends here until fetch(1) is ready

val maybe: Int? = withTimeoutOrNull(500) { fetch(2) }  // the result, or null if too slow
```

Why it matters: overlapping two network calls instead of waiting for each in turn, and giving up gracefully when the signal drops, is the difference between a snappy app and a spinner that never stops. The tests use a virtual clock, so these "slow" calls run instantly.

**Do it:** implement `sumInParallel` and `awaitOrFallback`. Make the `Coroutines` tests pass.

**Docs:** [Coroutines basics](https://kotlinlang.org/docs/coroutines-basics.html) · [Composing suspending functions](https://kotlinlang.org/docs/composing-suspending-functions.html) · [Cancellation & timeouts](https://kotlinlang.org/docs/cancellation-and-timeouts.html)

---

## Part 4: You are done when it is all green

```bash
./gradlew test        # expect: BUILD SUCCESSFUL, all 31 tests passing
```

There is an HTML report at `build/reports/tests/test/index.html` if you want to see the full list. Commit your work so your progress is on record:

```bash
git add -A && git commit -m "Lab 1 complete"
```

---

## Using AI on this lab

You are encouraged to use AI, but **you own everything you write, and you must be able to explain it and change it yourself.** This lab isn't submitted or graded - but understanding your own code is exactly what the project defense will later test, so build the habit now.

- **Good uses:** translate a Java idiom you know into Kotlin, look up what a standard-library function returns, or make sense of a confusing compiler error. AI is a great Kotlin tutor for syntax.
- **Not this:** pasting an exercise in and copying the answer before you understand *why* it works. You will get a green bar and learn nothing, and later you will be asked to explain and modify your own code, where that gap shows immediately.

For each of the ten sections, write one sentence in your own words: *what changes because this code runs on a phone that is low on battery, memory, and signal?* If you used AI, keep a short note of what you asked and what you kept or threw away. That reflection is the real point of the lab; the passing tests are just the checkpoint.

---

## What's next

Lab 2 moves to **Android and Jetpack Compose** (that is when you will install Android Studio). Everything you practiced here, immutable state, sealed `UiState`, lambdas, collection transforms, and coroutines, is what Compose is built on, so this hour pays off right away.

*This lab adapts ideas from [Kotlin Koans](https://kotlinlang.org/docs/koans.html) (JetBrains) and [Android Basics with Compose](https://developer.android.com/courses/android-basics-compose/course) (Google).*
