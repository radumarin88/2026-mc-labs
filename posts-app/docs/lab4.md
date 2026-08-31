# Lab 4: Architecture (ViewModel and unidirectional state)

In Lab 3 you saw a prototype fall over: rotate the screen and the state reset, because the logic and state lived inside composables. This lab fixes the root cause. You move the state and the decisions out of the UI and into a **ViewModel**, and the UI becomes a pure function of one immutable state value. This is the **MVVM** pattern with **unidirectional data flow (UDF)**, and it is the backbone every later lab builds on.

This is the app you grow for the rest of the course. It starts as a **Posts** reader: a searchable, bookmarkable list of posts, held in memory for now. In Lab 5 the posts start coming from the network, in Lab 6 they get a local database, in Lab 7 the app goes offline-first, and so on. Today you give this app a proper architecture.

The screen is already written; your job is to build the state and the logic behind it, and wire the two together. This lab has fast **JVM unit tests** for the logic and, from Lab 5 on, instrumented tests for the wiring, so fill in the code until the tests pass.

Time: about two hours.

---

## Part 1: Before you start

### What you should carry over
Immutable data and `copy()` (Lab 1), `StateFlow` and read-only views (Lab 1), composables and state hoisting (Lab 2), and collection operations like `filter` and `count` (Lab 1). If state hoisting is fuzzy, revisit Lab 2, because a ViewModel is state hoisting taken to its natural home.

### Open the project and run the tests
Open the `posts-app` folder in Android Studio and let Gradle sync. The exercises are in `PostsViewModel.kt` (the state and logic) and `PostsScreen.kt` (the wiring). Run:

```bash
./gradlew test        # fast JVM unit tests for the ViewModel, no device
```

They start red. That is your starting line.

Pinned tooling (verified October 2026): **AGP 9.4.1**, **Kotlin 2.4.21**, **Compose BOM 2026.09.00**, **Lifecycle 2.11.0** (ViewModel and `collectAsStateWithLifecycle`), **compileSdk 37**.

### The one production concern: SEPARATE UI FROM LOGIC (state down, events up)

A composable should decide *how things look*, never *what the data is or how it changes*. Those belong in a **ViewModel**, which:

- **owns the state** as a single immutable value (here, `PostsUiState`), exposed as a read-only `StateFlow`;
- **is the only thing that changes it**, by producing a new state in response to events (`setQuery`, `toggleBookmark`, ...);
- **outlives configuration changes**, so rotating the screen no longer wipes your data.

The UI then does two things only: it **reads** the state (flows down) and **reports events** (flow up) by calling ViewModel functions. This is unidirectional data flow, and its immediate reward is that your logic, now free of Android and UI, can be tested in milliseconds on the JVM. That is exactly what the unit tests here do.

---

## Part 2: What you need to know

### Why architecture at all

A first app tends to put everything in one place: the screen draws itself, holds the data, talks to the network, and decides what happens on every tap. It works for a demo and then collapses, because there is no seam anywhere. You cannot test the logic without launching the whole UI, two features fight over the same variable, and a small change ripples everywhere. This is the "everything in the Activity" problem, and your Lab 3 prototype was a version of it.

The cure is **separation of concerns**: split the app into layers that each have one job. Google's recommended architecture has a **UI layer** (what the user sees and interacts with) and a **data layer** (where data comes from and is stored). This lab is entirely about getting the **UI layer** right; the data layer arrives in Labs 5 and 6. Inside the UI layer there are two roles:

- **UI (composables):** decide *how things look* given some state, and turn user actions into events. They hold no app data and make no decisions.
- **State holder (ViewModel):** owns the screen's state and all the logic that changes it.

Keeping these apart is what makes the rest of the course possible: logic you can test without a device, UI you can restyle without touching logic, and a clear answer to "where does this belong?"

### The Activity lifecycle, and why your prototype reset

An **Activity** is the Android object that hosts your screen. The system drives it through a **lifecycle** of callbacks:

`onCreate` (created) then `onStart` (visible) then `onResume` (in the foreground, interactive) then your app runs, then `onPause`, `onStop` (no longer visible), `onDestroy` (gone).

The part that matters here: on a **configuration change** (rotating the device, switching to dark mode, changing language, resizing a window) the system **destroys the current Activity and creates a brand new one**. Anything you stored in the Activity, or in a plain composable variable, is gone and reinitialised. *That* is why the Lab 3 prototype forgot everything on rotation: its state lived somewhere the system throws away and rebuilds.

You could try to patch this by saving each value into a Bundle (`rememberSaveable` does this for small UI values), but that does not scale to a screen's worth of state and logic. You want somewhere to keep state that is tied to the screen but does **not** die on every rotation.

### The ViewModel lifecycle (the contrast)

That somewhere is a **ViewModel**. A ViewModel is a state holder whose lifecycle is deliberately *longer* than the Activity's. It is created the first time the screen needs it and then **survives configuration changes**: when the Activity is destroyed and recreated on rotation, the *same* ViewModel instance is handed to the new Activity. It is only cleared (its `onCleared()` runs) when the screen is finished **for good**, for example the user navigates away permanently or the Activity truly finishes, not when it is merely being recreated.

```
Activity:   onCreate ... onDestroy   |   onCreate (recreated) ... onDestroy   |
                  \_____ rotate _____/                    \___ back pressed __/
ViewModel:  [ created ....................... same instance survives rotation ....... onCleared ]
```

So the division of responsibility is: the **Activity and composables are disposable views** that the system recreates freely; the **ViewModel is the durable holder** of state and logic behind them. Two rules follow, and they matter:

- **Put state and logic in the ViewModel**, not the UI, so they survive recreation.
- **Never put a `Context`, an Activity, or a composable/View inside a ViewModel.** Because the ViewModel outlives them, holding a reference would keep a destroyed Activity in memory (a leak). The ViewModel deals in plain data, which is also why it is so easy to test.

### Unidirectional data flow (UDF)

With logic in the ViewModel and drawing in the UI, how do they talk? Through a one-way loop:

- **State flows down.** The ViewModel exposes one immutable state value; the UI reads it and renders it. When the state changes, the UI recomposes to match.
- **Events flow up.** The UI never changes state itself. On a tap it calls a ViewModel function (`toggleBookmark(id)`), the ViewModel computes a **new** state, and that new state flows back down.

```
        state (immutable)
ViewModel   ==========>   UI (composables)
    ^                           |
    +===========================+
        events (function calls)
```

This is **unidirectional data flow**, and the discipline buys a lot. There is a **single source of truth** (the ViewModel's state), so the UI can never disagree with itself. Because you always emit a *new* immutable value (Lab 1) rather than mutating in place, transitions are predictable and safe when more than one thing happens at once. And because the loop is one-directional, you can reason about the app as "given this state, the screen looks like this," instead of chasing two-way bindings.

### How it looks in code

The ViewModel keeps state in a private `MutableStateFlow` and exposes it as a read-only `StateFlow` (the Lab 1 read-only-view idea again). To change state it calls `update { }`, which atomically swaps in a new immutable value that every observer then sees. The composable obtains the ViewModel with `viewModel()` and reads the flow with `collectAsStateWithLifecycle()`, which collects only while the screen is actually visible and recomposes on each new value.

```kotlin
class CounterViewModel : ViewModel() {
    private val _state = MutableStateFlow(CounterState(0))
    val state: StateFlow<CounterState> = _state.asStateFlow()   // read-only to the outside
    fun increment() = _state.update { it.copy(count = it.count + 1) }
}

@Composable
fun CounterRoute() {
    val vm: CounterViewModel = viewModel()                       // same instance across rotation
    val state by vm.state.collectAsStateWithLifecycle()          // state flows down
    CounterScreen(state = state, onIncrement = vm::increment)    // event flows up
}
```

**Docs:** [Guide to app architecture](https://developer.android.com/topic/architecture) · [The Activity lifecycle](https://developer.android.com/guide/components/activities/activity-lifecycle) · [ViewModel overview](https://developer.android.com/topic/libraries/architecture/viewmodel) · [UI state and UDF](https://developer.android.com/topic/architecture/ui-layer#unidirectional-data-flow)

---

## Part 3: The exercises

Exercises 1 to 6 are in `PostsViewModel.kt`; exercise 7 is in `PostsScreen.kt`. Do them in order.

### 1 · Derive state, do not store it twice

`PostsUiState` stores only the source of truth: the full list of `posts`, the set of `bookmarkedIds`, the search `query`, and the `showBookmarkedOnly` flag. What the screen shows and the count it displays are **derived**. Implement the two computed properties:
- `visiblePosts`: the posts kept when (not filtering to bookmarks, or the post is bookmarked) **and** the title contains the query, case-insensitive.
- `bookmarkCount`: how many posts are bookmarked.

Deriving instead of storing means these can never drift from the source of truth.

**Make pass:** `initial state shows the seed posts with no bookmarks and no filter`.

**Docs:** [UI state](https://developer.android.com/topic/architecture/ui-layer#define-ui-state)

### 2 · Expose read-only state

The ViewModel holds a private `MutableStateFlow` called `_uiState`. Expose it to the UI as a public read-only `StateFlow` so nothing outside the ViewModel can change the state directly.

**Docs:** [StateFlow](https://developer.android.com/kotlin/flow/stateflow-and-sharedflow)

### 3 · Set the search query

Implement `setQuery(query)`: produce a new state with the query changed. You do not touch the post list; changing the query changes what `visiblePosts` returns, and the UI updates on its own.

**Make pass:** `search filters the visible posts by title`.

### 4 · Toggle "bookmarked only"

Implement `setShowBookmarkedOnly(only)`: a new state with the flag changed.

**Make pass:** `bookmarked-only filter shows only saved posts`, `search and bookmark filter combine`.

### 5 · Toggle a bookmark

Implement `toggleBookmark(id)`: if the id is already in `bookmarkedIds`, remove it; otherwise add it. Produce a new state (do not mutate the set).

**Make pass:** `bookmarking is tracked and the count updates`, `state transitions do not mutate the previous state`.

### 6 · `refresh()` (nothing to implement until Lab 5)

There is no task here in Lab 4: the ViewModel is complete once exercises 1 to 5 pass. Exercise 6 is the `refresh()` slot, left empty for now; it is filled in **Lab 5**, where it fetches the posts from the network. It is numbered here so the exercise numbers stay stable across the arc.

### 7 · Wire the screen to the ViewModel

In `PostsScreen.kt`, complete `PostsRoute`: obtain the `PostsViewModel` with `viewModel()`, collect its `uiState` with `collectAsStateWithLifecycle()`, and pass the state down to `PostsScreen`, forwarding each event (`onQueryChange`, `onShowBookmarkedOnlyChange`, `onToggleBookmark`) to the matching ViewModel function.

**Do it:** run the app. Search narrows the list, the chip filters to bookmarks, and tapping Save bookmarks a post. **Now rotate the device: your search and bookmarks survive.** That is the ViewModel doing its job.

**Docs:** [State and Jetpack Compose](https://developer.android.com/develop/ui/compose/state) · [Integrate Lifecycle with Compose](https://developer.android.com/topic/libraries/architecture/compose)

---

## Part 4: Extend it

Open-ended, verify by running (and add a JVM test where you can). Do at least one.

1. **Sort the list.** Show `visiblePosts` alphabetically by title. Where does the sort belong, and does the order of `filter` and `sortedBy` matter?
2. **A "clear bookmarks" action.** Add a ViewModel function that empties `bookmarkedIds`, and a button for it. What is the new state, and what becomes of `bookmarkCount`?
3. **Show the result count.** Add "N of M shown" using `visiblePosts` and `posts`. Which is state and which is derived?

For each, write one line: *what state did it touch, and who owns it?*

---

## Part 5: You are done when

```bash
./gradlew test        # all ViewModel unit tests pass
```

And the app runs: search, filter, and bookmarking work, and they survive a rotation. You have also done at least one extension.

Commit your work:
```bash
git add -A && git commit -m "Lab 4 complete"
```

---

## Apply it to your app

Your project milestone: take one screen of your Lab 3 prototype and give it this structure, a ViewModel holding an immutable UI state, a stateless screen that reads it and sends events up. You decide the shape of the state; that decision is yours to defend. Work it through the **five questions** you use for every design decision this course - the same frame you meet in Course 6, on the written exam, and in the project defence: *What binds? · What will you do? · What are you giving up? · What breaks? · What would change your mind?*

## Using AI on this lab

Good uses: the `StateFlow` or `viewModel()` API, a coroutine `update` signature. Reason unaided about the real question: *where does each piece of state live, and who is allowed to change it?* If you cannot say why the ViewModel owns the state and the composable only reads it, you have not learned the lab.

## What's next

Lab 5 gives this same app real data over the network with Retrofit and coroutines. The state you just built becomes the place network results land, and it grows two new fields: are we loading, and did it fail?

*This lab adapts ideas from [Android Basics with Compose](https://developer.android.com/courses/android-basics-compose/course) and the [Guide to app architecture](https://developer.android.com/topic/architecture) (Google).*
