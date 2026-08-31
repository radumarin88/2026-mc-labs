# Lab 2: Compose, from Fundamentals to Navigation

Welcome to your first Android lab. You will build user interfaces with **Jetpack Compose**, Android's modern UI toolkit. In Compose you do not create widgets and mutate them by hand; you write functions that describe the screen as a function of state, and the framework redraws it for you. Everything from Lab 1 (immutable data, lambdas, `when`) is what Compose runs on.

This lab is **one task in two movements**. First you learn the fundamentals on small pieces: a composable, local state, and the one idea that makes composables reusable (**state hoisting**). Then you put them to work building a real **multi-screen app**: a scrollable, searchable, filterable list of items, and a detail screen for whichever one you tap. Along the way you meet the three things every real screen needs, **layouts**, **lazy lists**, and **navigation**. This is the shape of the app your team will build for the project.

It is **eight exercises** in `app/src/main/kotlin/mobile/catalog/Exercises.kt`. Exercises 1 to 3 are the fundamentals; 4 to 8 build the app. This lab also has automated **instrumented** tests that run on a device or emulator. Because it merges two topics it is a fuller lab than most, so budget a little extra time: work through the list (exercises 1 to 6) first, then the detail screen and navigation (7 and 8). Like every lab, this is **individual, at-home work** you do at your own pace.

---

## Part 1: Before you start

### What you should carry over from Lab 1
You should be comfortable with lambdas and trailing-lambda syntax, immutable data and `copy(...)`, reading a `when`, and the collection operations (`filter`, `map`, `contains`). Compose leans on all of it; the list filtering later in this lab is just those operations.

### Install Android Studio

This lab uses the Android SDK, and the smoothest way to get it is **Android Studio**, the official IDE.

1. Download and install Android Studio from **[developer.android.com/studio](https://developer.android.com/studio)**.
2. On first launch, let it install the Android SDK. This project targets **SDK 37**, so open *Settings → Languages & Frameworks → Android SDK* and make sure **Android SDK Platform 37** is checked and installed.

### Open the project

1. This repository holds all the labs side by side. Open the `lab2-compose` folder in Android Studio (*File → Open*, and pick that folder). Let Gradle sync; the first sync downloads the Android Gradle Plugin, Compose, and the libraries, so you need internet once. Android Studio writes your SDK path into `local.properties` automatically.
2. Open `app/src/main/kotlin/mobile/catalog/Exercises.kt`. That is the only file you edit. Right now every composable is a `TODO()`.

You check your work three ways, all covered below: the **preview pane** (`@Preview`), **running the app**, and the **instrumented tests**.

Pinned tooling (verified October 2026): **AGP 9.4.1**, **Kotlin 2.4.21**, **Compose BOM 2026.09.00** (Material 3), **Navigation Compose 2.10.2** (type-safe routes), **compileSdk 37**.

### Why a ready-made project, and where the versions live

You are opening a project we prepared instead of creating one from scratch, on purpose: it keeps everyone on the same known-good versions so this lab is about Compose, not setup. This is how Google's own codelabs work too. The build does not depend on your Android Studio version, because the Gradle **wrapper** pins the Gradle version and `gradle/libs.versions.toml` (the *version catalog*) pins the Android Gradle Plugin, Compose, and every library. It therefore builds the same on any machine with a reasonably current Android Studio.

Take two minutes to see where things live, because you will edit these yourself soon:
- `gradle/libs.versions.toml`: version numbers and the library list (the `[versions]`, `[libraries]`, and `[plugins]` tables).
- `app/build.gradle.kts`: which of those libraries this module actually uses, in the `dependencies { }` block.
- `gradle/wrapper/gradle-wrapper.properties`: the Gradle version.

You will run the real *File → New → New Project* flow and own your versions when your team creates its own app in **Lab 3**, and from **Lab 4** onward you will add new libraries (Room, Retrofit, and so on) to these files yourself as you productionize it.

### Run it on a device or emulator

Set up a device or emulator now: you need one for the instrumented test, and seeing the app run is the payoff. You will use it for the rest of the course.

**Option A, an emulator.** In Android Studio open *Device Manager* (the phone icon in the right toolbar), click *Add a virtual device*, pick a phone and a recent system image (API 35, 36, or 37, download it if prompted), and finish. Press the green ▶ *Run* button to launch the app on it.

**Option B, a real phone.** On the phone, open *Settings → About phone* and tap *Build number* seven times to unlock *Developer options*. In *Developer options*, turn on *USB debugging* (and *Wireless debugging* if you would rather skip the cable, Android 11+). Connect the phone, accept the "Allow USB debugging?" prompt, and confirm your computer sees it:
```bash
adb devices        # should list your device (adb lives in the SDK's platform-tools)
```
Then pick the device in the toolbar and press ▶.

**Install the ADB Idea plugin.** It saves constant trips to the terminal. In *Settings → Plugins → Marketplace*, search **ADB Idea**, install it, and restart. Invoke it from the action search (**Cmd/Ctrl+Shift+A**, type *ADB Idea*) to do, in one click: uninstall the app, kill it, **clear app data**, start or stop it, or **grant/revoke a runtime permission**. Those last two matter a lot in later labs: when you work with permissions, persistence, and background sync you will constantly want to reset the app to a clean state.

### Your first instrumented test

Lab 1's tests were plain JVM unit tests: they run on your computer in milliseconds but cannot touch Android. This lab adds an **instrumented** test, in `app/src/androidTest/`: it is built into a second APK, installed onto a **real device or emulator**, and run there against the actual Android framework. That makes it high fidelity (real navigation, real rendering) but slower, and it needs a device. Most projects use both, and in **Lab 9** you will wire the fast ones into an automatic CI check.

Run the test once now to see it fail (nothing is implemented yet). Start an emulator (or plug in a phone), then:
```bash
./gradlew connectedDebugAndroidTest        # on Windows: gradlew.bat connectedDebugAndroidTest
```
or in Android Studio, click the ▶ in the gutter next to `CatalogNavigationTest`. Red is your starting line.

---

## Part 2: How Compose works, and the two ideas to take away

A **composable** is a function marked `@Composable` that describes a piece of UI. You call other composables inside it (`Text(...)`, `Button(...)`), and Compose turns that description into pixels. When the data a composable reads changes, Compose calls it again, which is called **recomposition**. The screen is always a function of the current state.

**State** is any value that should update the UI when it changes. You keep it with `remember { mutableStateOf(initial) }`: `mutableStateOf` makes it observable, and `remember` keeps it across recompositions instead of resetting it every redraw. The `by` delegate lets you read and assign it like a normal variable. Change the state and Compose recomposes whatever read it.

**Layouts and modifiers.** You arrange composables with `Column` (vertical), `Row` (horizontal), and `Box` (stacked). A **`Modifier`** decorates a composable: `Modifier.fillMaxWidth().clickable { }.padding(16.dp)`. Order matters, because modifiers apply outside-in. By convention every composable takes a `modifier: Modifier = Modifier` parameter and applies it to its root, so callers can position it.

**Lazy lists.** A `Column` composes all its children at once, fine for a few but wasteful for hundreds. `LazyColumn` (and `LazyRow`) only compose the items currently on screen and recycle as you scroll. You describe content in its `items(...)` block, and you give each item a **stable `key`** so Compose can follow it across insertions, removals, and reorders.

**Navigation.** A `NavController` holds the back stack; a `NavHost` maps **routes** to screens. With type-safe navigation, each route is a `@Serializable` object or data class, you register screens with `composable<Route> { }`, you move with `navController.navigate(Route(...))`, and you read a route's arguments with `entry.toRoute<Route>()`. `navController.popBackStack()` goes back.

### Seeing your work: `@Preview`

`@Preview` is your fastest feedback loop: annotate a parameterless composable with `@Preview` and Android Studio renders it in a pane next to your code (use the split-editor icons at the top-right of the file). It updates as you type, with no emulator and no app launch.

```kotlin
@Preview(showBackground = true)
@Composable
private fun ItemRowPreview() {
    AppTheme {                 // wrap it so it picks up Material 3
        ItemRow(item = SampleItems.all.first(), onClick = {})
    }
}
```

Add one next to whatever exercise you are working on. The preview shows an error while the composable is still `TODO()`, then lights up once you implement it.

> ## Production concern #1: STATE HOISTING (unidirectional data flow)
> A composable that owns its own state is hard to reuse, test, or control from elsewhere. The fix is to **hoist** the state out. The composable becomes **stateless**: it takes the current value as a parameter and reports events through callbacks, the `value: T, onEvent: (T) -> Unit` shape. State flows **down**, events flow **up**. This is the same unidirectional data flow you met as `UiState` in Lab 1, now in the UI layer. It is what makes composables testable, and it is exactly the seam you connect to a ViewModel in **Lab 4**. Exercises 2 and 3 are the same toggle written both ways so you feel the difference.

> ## Production concern #2: NAVIGATE BY ID, NOT BY OBJECT
> When you move to the detail screen, it is tempting to hand the whole `Item` across. Do not. A destination is addressed by a **route**, and a route should carry only a small identifier (here, the item id). The detail screen then **looks up its own data** from that id. Screens stay decoupled, routes stay small and could even come from a deep link or a saved back stack, and you are ready for **Lab 6**, where "look up the item" becomes a real database query instead of an in-memory list. Data flows *down* through a tiny id, not through fat objects.

---

## Part 3: The eight exercises

Do them in order. Build and preview each piece on its own before wiring it up. The immutable `Item` and the fixed `SampleItems` data source are provided in `Item.kt`.

### 1 · Your first composable: `ItemRow`

A composable is just a function that describes UI: you annotate it `@Composable` and, inside, call other composables. Material 3 gives you the building blocks (`Text`, `Button`, `Surface`) from `androidx.compose.material3`, and they pick up colours and fonts from the surrounding `MaterialTheme`. Stack the item's name, category, and tagline in a `Column`, make the whole row fill the width, respond to taps, and pad it. `Modifier.clickable { }` turns anything into a button.

```kotlin
@Composable
fun Tile(title: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 12.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
    }
}
```

**Do it:** implement `ItemRow` (name, category, tagline; whole row clickable and padded). Check it with a `@Preview`.

**Docs:** [Thinking in Compose](https://developer.android.com/develop/ui/compose/mental-model) · [Compose layouts](https://developer.android.com/develop/ui/compose/layouts/basics) · [Modifiers](https://developer.android.com/develop/ui/compose/modifiers)

### 2 · State and recomposition: `FavoriteToggle`

To make UI react, you need state that Compose watches. `mutableStateOf(x)` creates an observable holder, and `remember { }` keeps it across recompositions. When you change it, Compose recomposes everything that read it, so the screen updates on its own.

```kotlin
@Composable
fun ToggleDemo() {
    var on by remember { mutableStateOf(false) }   // observable, remembered across redraws
    Button(onClick = { on = !on }) { Text(if (on) "On" else "Off") }
}
```

**Do it:** implement `FavoriteToggle`: a `TextButton` reading `☆ Save` that flips to `★ Saved` when tapped. A `@Preview` is interactive in the preview pane, so click it there. Notice the state lives *inside* the composable, which is exactly what the next exercise changes.

**Docs:** [State in Compose](https://developer.android.com/develop/ui/compose/state)

### 3 · State hoisting: `LabeledSwitch`

A **stateless** composable holds no state of its own. It receives the current value and reports events, and someone else decides what those events do. That is the shape you want for anything you intend to reuse or test. `LabeledSwitch(label, checked, onCheckedChange)` only displays and reports; the caller owns the `Boolean`. A `Row` lays children left to right, and `Modifier.weight(1f)` on the label takes the leftover space, pushing the switch to the right edge.

```kotlin
Row(verticalAlignment = Alignment.CenterVertically) {
    Text("Wi-Fi", modifier = Modifier.weight(1f))
    Switch(checked = on, onCheckedChange = onToggle)
}
```

**Do it:** implement `LabeledSwitch`. To preview it, write a small wrapper that owns a `Boolean` and passes it down (`var on by remember { mutableStateOf(false) }`, then `LabeledSwitch("Only favorites", on) { on = it }`). This is state down, events up, the central Compose idea.

**Docs:** [State hoisting](https://developer.android.com/develop/ui/compose/state-hoisting) · [Material 3 components](https://developer.android.com/develop/ui/compose/components)

### 4 · Filter chips: `CategoryChips`

The same hoisting shape, now with real content. A stateless row of Material 3 `FilterChip`s: an "All" chip plus one per category; the selected one is highlighted, and tapping a chip reports the choice up through `onSelect` (null means "All"). Put them in a `Row` with `Modifier.horizontalScroll(rememberScrollState())` so they scroll sideways if there are many.

```kotlin
Row(modifier.horizontalScroll(rememberScrollState())) {
    FilterChip(selected = value == null, onClick = { onSelect(null) }, label = { Text("All") })
    // one chip per option...
}
```

**Do it:** implement `CategoryChips(categories, selected, onSelect)`. Preview it with a wrapper that owns a `String?`.

**Docs:** [Chips](https://developer.android.com/develop/ui/compose/components/chip) · [Scroll modifiers](https://developer.android.com/develop/ui/compose/touch-input/scroll/scroll-modifiers)

### 5 · The list, with an empty state: `ItemList`

Render many rows efficiently with `LazyColumn` and `items(list, key = { ... })`, keyed by item id, reporting the tapped id via `onItemClick`. But first handle the **empty case**: when there are no items to show (a search matched nothing), show a centered "No matching items" message instead of a blank screen. A real list screen always answers "what if there is nothing?".

```kotlin
if (items.isEmpty()) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Nothing here") }
    return
}
LazyColumn(modifier) { items(items, key = { it.id }) { /* row */ } }
```

**Do it:** implement `ItemList` with the empty state and the keyed `LazyColumn`.

**Docs:** [Lists and grids](https://developer.android.com/develop/ui/compose/lists)

### 6 · The list screen: `CatalogListScreen`

Now the screen that owns the state and ties the pieces together. Keep a **search query** and a **selected category** in `remember` state. Compute the visible items from `SampleItems.all` by keeping an item when the selected category is null or matches, **and** its name contains the query (case-insensitive). Lay out, in a `Column`: a search `OutlinedTextField`, then `CategoryChips`, then `ItemList(visible, onItemClick)`.

This is the real skill: a screen is state plus a computed view of your data plus the stateless pieces that render it. The filtering is just the Lab 1 collection operations applied to `SampleItems.all`.

**Do it:** implement `CatalogListScreen`. Run the app: typing narrows the list, chips filter by category, clearing both shows everything. **Now rotate the device: the search box clears.** That is not a bug you can fix here, because the state lives in a composable. Hold that thought; **Lab 4** is where you move it to a ViewModel that survives rotation.

**Docs:** [State in Compose](https://developer.android.com/develop/ui/compose/state) · [Text and input](https://developer.android.com/develop/ui/compose/text/user-input)

### 7 · The detail screen: `ItemDetail`

Stack the name (headline), category (label), a `HorizontalDivider`, the tagline, and the description in a padded `Column`, plus a "Back" button that calls `onBack`. `Arrangement.spacedBy(8.dp)` puts even gaps between children.

**Do it:** implement `ItemDetail`. Preview it with any `SampleItems.all` element.

**Docs:** [Compose layouts](https://developer.android.com/develop/ui/compose/layouts/basics) · [Material 3 components](https://developer.android.com/develop/ui/compose/components)

### 8 · Wire it together: `CatalogNavHost`

The navigation graph. The two routes are already declared for you (`ListRoute`, and `DetailRoute` carrying the item id). Build a `NavHost` starting at the list, register both destinations, navigate to `DetailRoute(id)` on a tap, and on the detail destination read the id back and look the item up with `SampleItems.byId`. Remember production concern #2: the route carries the **id**, and the detail screen fetches its own item.

```kotlin
@Serializable object Home
@Serializable data class Profile(val userId: String)

NavHost(navController, startDestination = Home) {
    composable<Home> { HomeScreen(onOpen = { id -> navController.navigate(Profile(id)) }) }
    composable<Profile> { entry ->
        val args = entry.toRoute<Profile>()
        ProfileScreen(userId = args.userId, onBack = { navController.popBackStack() })
    }
}
```

**Do it:** implement `CatalogNavHost`. Then run the app and run `CatalogNavigationTest` on your device or emulator until all three pass.

**Docs:** [Navigation with Compose](https://developer.android.com/guide/navigation) · [Type safety in Navigation](https://developer.android.com/guide/navigation/design/type-safety) · [Test Compose navigation](https://developer.android.com/guide/navigation/testing/compose)

---

## Part 4: Extend it

The exercises above are the core, and the tests check the app. These extensions are open-ended; verify them by running the app. Do at least one.

1. **Sort the list.** Show the visible items alphabetically by name. Where does the sort belong, and does the order of `filter` and `sortedBy` matter?
2. **A result count.** Above the list, show "N results" that updates as you search and filter. What state does it depend on?
3. **A third screen.** Add an `AboutRoute` and an "About" button on the list screen that navigates to a small About screen with a Back button. You are adding a destination to the graph, exactly as you did for detail.

For each extension, write one line: *what state did it touch, and did any data cross a navigation boundary?*

---

## Part 5: You are done when

- The app runs: exercises 2 and 3 preview and toggle, search and chips filter the list, an empty search shows the empty state, tapping a row opens its detail, and Back returns.
- The instrumented tests pass:
  ```bash
  ./gradlew connectedDebugAndroidTest        # an emulator or device must be running
  ```
- You have completed at least one extension.

Commit your work:
```bash
git add -A && git commit -m "Lab 2 complete"
```

In **Lab 9** these instrumented tests move into an automatic CI check that runs on every push.

---

## Using AI on this lab

You are encouraged to use AI, but you own everything you write, and you must be able to explain it and change it yourself. This lab isn't submitted or graded - the understanding is the whole point, and the project defense is where it gets tested.

- **Good uses:** looking up a `Modifier`, a composable's parameters, the `LazyColumn` `items` signature, or how `composable<T>` and `toRoute` fit together; fixing an import or a build error.
- **Not this:** pasting an exercise in and copying the result before you understand *why* the state lives where it does, or letting it generate the whole navigation graph before you understand why the route carries an id. Those decisions are the point of this lab.

For each exercise, write one sentence in your own words: *where does the state live, and why there?* and, for 4 to 8, *what information crosses each boundary (row to list, list to detail), and why is it that and not more?* Keep a short note of what you asked AI and what you kept or rejected.

---

## Go deeper (optional): Google's official codelabs

This lab is built on Google's own Compose teaching materials, which are genuinely good. The exercises here are yours to solve, but for a fuller guided walk-through of the same ideas:

- **[Jetpack Compose basics](https://developer.android.com/codelabs/jetpack-compose-basics)**: composables, Material 3, and recomposition.
- **[Intro to state in Compose](https://developer.android.com/codelabs/basic-android-kotlin-compose-using-state)**: state, text input, and state hoisting.
- **[Basic layouts in Compose](https://developer.android.com/codelabs/jetpack-compose-layouts)**: rows, columns, modifiers, and lists.
- The full **[Android Basics with Compose](https://developer.android.com/courses/android-basics-compose/course)** course is the hub for all of it.

Use the Compose and Material 3 versions linked above; older Compose tutorials you may find elsewhere can be out of date. Treat these as learning, not as answers to paste in.

---

## What's next

**Lab 3** is the pivot: your team uses AI to stand up a rapid prototype of *your own* app idea, creating the project yourselves. From **Lab 4** on you productionize that app (architecture, networking, persistence, background work, tests, CI), adding each new library to the build files yourself. The list-plus-detail shape you built here is the shape it starts from.

*This lab adapts ideas from [Android Basics with Compose](https://developer.android.com/courses/android-basics-compose/course) (Google) and the [Navigation](https://developer.android.com/guide/navigation) guide.*
