# Lab 8: Dependency Injection with Hilt

You have been wiring the app by hand. A `Graph` object built the database, made the repository, and handed it out; the ViewModel took it as a default parameter; the worker reached into the global to get it. That works, but it is brittle: every class knows where to *find* its dependencies instead of just *declaring* them, the global is a hidden coupling, and it does not scale as the graph grows. This lab replaces all of it with **Hilt**, the standard dependency-injection framework for Android. The app behaves identically; the plumbing gets honest.

You change no behaviour and (almost) no logic. You add annotations that say "this class needs these things" and "here is how to build the things that cannot build themselves", and Hilt assembles the object graph for you, checked at compile time. You extend the same `posts-app`.

Time: about two and a half hours.

---

## Part 1: Before you start

### What is new since Lab 7
Nothing yet: there is no Hilt dependency. You add it. When you are done, the hand-wired `Graph` service locator is gone (deleted for you already), and Hilt supplies the repository to the ViewModel and the worker.

### Open the project and run the tests
As in Labs 6 and 7, the project does **not compile** at first. `PostsScreen` already calls `hiltViewModel()`, but the Hilt dependency, the annotations that turn Hilt on (`@HiltAndroidApp`, `@AndroidEntryPoint`), the module, and the injected constructors are all yours to add. Make it compile and green by doing the five exercises. Then:

```bash
./gradlew test                       # the repository/ViewModel tests, on the JVM
./gradlew connectedDebugAndroidTest  # the DAO and worker tests, on a device/emulator
```

Pinned tooling (verified Aug 2026): **Hilt 2.60.1** (`com.google.dagger:hilt-android`, its compiler run through **KSP**) and **androidx.hilt 1.4.0** (`hilt-navigation-compose` for `hiltViewModel()`, `hilt-work` for `@HiltWorker`).

### The one production concern: DEPENDENCIES ARE DECLARED, NOT HAND-WIRED

Ask where a class's collaborators come from. Two answers:

- **Hand-wired (Labs 6 and 7).** The class knows how to get them: it reads a global (`Graph.repository`), or news them up itself. The dependency is hidden inside the class, the same wiring is repeated in every entry point, and a test cannot substitute a fake without reaching into that global too.
- **Injected (this lab).** The class simply *declares* what it needs in its constructor and receives it. It does not know or care where the object came from. A single place (the DI container) knows how to build everything and assembles the graph. Tests just pass a fake to the constructor.

Constructor injection makes dependencies **explicit** (you can read a class's needs from its signature), makes classes **testable** (pass fakes in), and removes the hidden global. Hilt is the machinery that builds and connects the graph so you do not write that wiring by hand.

---

## Part 2: What you need to know

### What dependency injection is, manual and automatic

**Dependency injection** is a plain idea behind a heavy name: an object is *given* the other objects it needs (its dependencies) from outside, instead of creating or looking them up itself. That is all. The `PostsRepository` needs a `PostsDao` and a `PostsApi`; "injecting" them means someone hands them to it (through its constructor) rather than the repository building them.

You can do this **by hand**. Manual DI is just writing the wiring yourself:

```kotlin
val dao = database.postsDao()
val api = retrofit.create(PostsApi::class.java)
val repository = PostsRepository(dao, api)   // you construct and pass the dependencies
val viewModel = PostsViewModel(repository)
```

That is genuine dependency injection, and it is exactly what your JVM tests already do with fakes. It is also, in spirit, what the `Graph` did, though `Graph` was a *service locator*: instead of passing dependencies in, classes reached into a global to pull them out. Both wire the graph; a service locator just hides the wiring inside a global and couples every class to it.

Manual DI is fine for a small app and breaks down as the graph grows: the wiring code multiplies, every construction site repeats it, and the order you must build things in becomes a chore. **Automatic DI** hands that clerical work to a framework. You *declare* what each class needs and how to build the few things that cannot build themselves, and the framework generates the construction code and assembles the graph. Hilt is that framework for Android. The concept does not change; the boilerplate goes away.

### Why inject through the constructor

Passing dependencies through the constructor (rather than a setter, a global, or building them inside) is the good way to do DI, for three connected reasons:

- **Inversion of control.** Normally an object controls its collaborators: it decides which `PostsApi` to create, so it is bolted to that choice. Constructor injection *inverts* that control: the object gives up deciding, and its collaborators are supplied from outside. The class no longer depends on a *concrete* thing it built; it depends on an *abstraction* it was handed. That is what lets you swap a real API for a fake, or one implementation for another, without touching the class.
- **Composition over inheritance.** There are two ways to give a class new behaviour: extend a base class (inheritance) or hold another object that has the behaviour (composition). Composition is more flexible: a class can be handed *any* collaborator that fits the type, combinations are open-ended, and there is no rigid hierarchy to fight. Constructor injection is composition made routine, the class declares "I am built from these parts", and you choose the parts.
- **Unit testing falls out for free.** Because the dependencies come in through the constructor, a test simply passes fakes: `PostsRepository(fakeDao, fakeApi)`, `PostsViewModel(repository)`. No global to reset, no framework to start, no mocking magic. Your Lab 6 and 7 tests do exactly this, and they keep working unchanged in this lab, because a class that takes its dependencies is trivial to construct in a test. A class that reaches into a global, or `new`s its own network client, is not.

The pattern is so standard that its core annotation is standardised too: **`@Inject` is not a Dagger or Hilt invention.** It comes from **JSR-330, "Dependency Injection for Java"** (the `javax.inject` package, now `jakarta.inject`), a formal Java specification, the ecosystem's counterpart to an RFC. Dagger, Guice, and Spring all understand the same `@Inject` and `@Singleton`. So the annotations that mark *your* classes are a portable standard; only the machinery (`@Module`, `@Provides`, `@HiltViewModel`) is framework-specific. Your domain code stays decoupled from the DI framework you happen to use.

### What Hilt is

**Hilt** is Google's dependency-injection framework for Android, built on **Dagger**. It is the *automatic* DI from above: Dagger generates the construction code, and Hilt is a layer on top that knows Android's building blocks (Application, Activity, ViewModel, Worker) and gives you a small, opinionated set of annotations so you rarely touch Dagger directly. You annotate; it wires.

### Constructor injection: `@Inject`

The core move. Put `@Inject` on a class's constructor, and Hilt can build it, supplying each parameter from the graph:

```kotlin
class PostsRepository @Inject constructor(
    private val dao: PostsDao,
    private val api: PostsApi,
)
```

This says "to build a `PostsRepository`, Hilt needs a `PostsDao` and a `PostsApi`". Hilt will now provide a repository anywhere one is needed, as long as it also knows how to build a `PostsDao` and a `PostsApi`. Which raises the question: how does it build *those*?

### `@Module` and `@Provides`: teaching Hilt to build what it cannot

Hilt can build any class you mark `@Inject`. But some things you do not own or cannot annotate: a Room database is created by `Room.databaseBuilder(...)`, a Retrofit API by a builder. You teach Hilt how, with **`@Provides`** functions inside a **`@Module`**:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "posts.db").build()

    @Provides
    fun providePostsDao(database: AppDatabase): PostsDao = database.postsDao()
}
```

Read a `@Provides` function as "here is how to make one of these, and here (its parameters) is what I need to make it". `provideDatabase` needs a `Context`, and Hilt supplies the application context because of the `@ApplicationContext` qualifier. `providePostsDao` needs an `AppDatabase`, which Hilt already knows how to make. The graph composes: API and DAO feed the repository, the repository feeds the ViewModel and the worker.

- **`@InstallIn(SingletonComponent::class)`** puts these bindings in the application-wide part of the graph.
- **`@Singleton`** sets the binding's *scope* (how long its instance lives), which is important enough to have its own section below. (There is also `@Binds`, a leaner way to say "when someone asks for this interface, give them that implementation"; `@Provides` is enough for this lab.)

### Compile-time safety

Hilt does all of this at **build time**: it reads your annotations through KSP (the same processor Room uses) and generates the construction code. The payoff is validation. When you build, Hilt checks the **entire graph**: if a class needs a dependency nothing provides, if a binding is duplicated, or if there is a dependency cycle, the build **fails with a specific error** that names the type and traces the path to it. You learn about a wiring mistake at your desk, as a red build, not as a null or a crash on a user's phone when that code path finally runs, which is exactly how the `Graph` service locator would have failed. It is the same theme as Room checking your SQL at compile time (Lab 6), now applied to how objects are wired together.

### Scoping: `@Singleton`, and why less is more

A binding's **scope** decides how long its instance lives and who shares it:

- **Unscoped** (a `@Provides` function or an `@Inject` class with no scope annotation): Hilt creates a **new** instance every time one is requested. It lives as long as whoever holds it and is garbage-collected when they are gone.
- **`@Singleton`** (in the `SingletonComponent`): Hilt creates **one** instance and reuses it for the whole app process; it is never garbage-collected while the app lives. There are narrower scopes too (`@ViewModelScoped`, `@ActivityScoped`, and so on) meaning "one per ViewModel / Activity".

Choose a scope by matching three things: the object's natural **lifetime**, whether it must be **shared** (holds shared state or a pooled resource), and its **cost** to build. The database and the Retrofit/OkHttp client are the textbook singletons: expensive to create, holding connection pools and caches, and meant to be shared, so we mark them `@Singleton`.

But **do not reach for `@Singleton` by default.** Making everything a singleton "to be safe" is a real mistake:

- **Memory that never comes back.** A singleton lives for the entire process and is never freed. A pile of them, especially any holding data, is memory you can never reclaim. A cheap unscoped object is created on demand and collected when unused, which is often the healthier choice.
- **Slower cold starts.** Hilt builds bindings lazily, so an *unused* singleton costs nothing. But anything on the startup injection path is built at launch, and its dependencies with it. If your Application (or a splash screen) pulls in a heavy singleton that transitively constructs half your graph, or a `@Provides` does real work, you pay it on **every cold start** and users feel a slow launch. Keep `@Provides` functions cheap, keep expensive initialisation off the startup path (Room, for one, does not open the database file until the first query, not at `build()`), and do not eagerly create singletons you will not use yet.

Our app is a deliberate small example: the database and API are `@Singleton` (shared, expensive); the DAO is unscoped (it is just `database.postsDao()`, which Room caches anyway); and **the repository is unscoped**, not a singleton, because it is a thin, stateless wrapper over those singletons, so a fresh one per ViewModel or worker is cheap and is collected with its owner. If the repository held an in-memory cache, *then* you would scope it. Scope for a reason, not for reassurance.

### Entry points: `@HiltAndroidApp` and `@AndroidEntryPoint`

Hilt has to attach its graph to Android's own objects. `@HiltAndroidApp` on your `Application` bootstraps the whole thing (and generates the base class Hilt needs). `@AndroidEntryPoint` on an Activity (or Fragment, Service) lets Hilt inject into it and into the composables it hosts. You add **both** yourself in exercise 2. `@HiltAndroidApp` is the switch that turns Hilt on: leave it off and Hilt generates nothing and the build fails.

### ViewModels: `@HiltViewModel` and `hiltViewModel()`

A ViewModel is special: the framework, not you, creates it. `@HiltViewModel` plus an `@Inject` constructor lets Hilt build it with its dependencies, and in Compose you retrieve it with **`hiltViewModel()`** instead of `viewModel()`. Compare the two eras:

```kotlin
// Lab 7: build it by hand, reaching into the global for the repository.
val vm: PostsViewModel = viewModel { PostsViewModel() /* default = Graph.repository */ }

// Lab 8: ask Hilt; it constructs the ViewModel and its repository, DAO, and API for you.
val vm: PostsViewModel = hiltViewModel()
```

### Workers: `@HiltWorker` and assisted injection

The worker was the ugliest case in Lab 7: WorkManager constructs it, so you could not pass it a repository, and it reached into `Graph`. Hilt fixes this cleanly. WorkManager still supplies the `Context` and `WorkerParameters` (they are runtime values, marked **`@Assisted`**), while Hilt supplies the repository, and **`@HiltWorker`** with an **`@AssistedInject`** constructor combines the two:

```kotlin
@HiltWorker
class RefreshWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: PostsRepository,   // Hilt provides this
) : CoroutineWorker(context, params)
```

For this to work, WorkManager must ask Hilt to build workers. That is what `PostsApp` does (its WorkManager plumbing is provided; you only add `@HiltAndroidApp` to it in exercise 2): it is a `Configuration.Provider` that hands WorkManager a `HiltWorkerFactory` (which Hilt injects into it), and the manifest disables WorkManager's default initializer so ours is used. The upshot: no global, and the worker's dependency is now injectable, which is exactly why the test can hand it a fake through a `WorkerFactory`.

### Why this is better than the `Graph`

`Graph` was a **service locator**: classes pulled dependencies from a global. It worked, but the coupling was invisible (you had to read the body to see what a class used), it could not be scoped, and it fought testing. Hilt inverts it: classes **declare** their dependencies and are handed them, the container is generated and compile-checked, and swapping an implementation (a fake in a test, a different API in a build variant) is a local change. Same behaviour; honest, checkable plumbing.

---

## Part 3: The exercises

Do them in order. The app does not compile until they are done.

### 1 · Add the Hilt dependencies and plugin

- In `gradle/libs.versions.toml`: add versions `hilt = "2.60.1"` and `hiltExt = "1.4.0"`; libraries `hilt-android` (`com.google.dagger:hilt-android`), `hilt-compiler` (`com.google.dagger:hilt-compiler`), `androidx-hilt-navigation-compose` (`androidx.hilt:hilt-navigation-compose`, ref `hiltExt`), `androidx-hilt-work` (`androidx.hilt:hilt-work`, ref `hiltExt`), `androidx-hilt-compiler` (`androidx.hilt:hilt-compiler`, ref `hiltExt`); and a plugin `hilt` with id `com.google.dagger.hilt.android`, ref `hilt`.
- In the top-level `build.gradle.kts`: add `alias(libs.plugins.hilt) apply false`.
- In `app/build.gradle.kts`: apply `alias(libs.plugins.hilt)`; add `implementation(libs.hilt.android)`, `ksp(libs.hilt.compiler)`, `implementation(libs.androidx.hilt.navigation.compose)`, `implementation(libs.androidx.hilt.work)`, and `ksp(libs.androidx.hilt.compiler)`.

Note the two `ksp(...)` compilers (Dagger's and androidx.hilt's for `@HiltWorker`) versus the `implementation` runtime libraries. Sync Gradle.

**Docs:** [Hilt dependency injection](https://developer.android.com/training/dependency-injection/hilt-android)

### 2 · Turn Hilt on

Hilt does nothing until you attach it to Android's entry points (Part 2). Add **`@HiltAndroidApp`** to `PostsApp` (the switch that bootstraps the graph) and **`@AndroidEntryPoint`** to `MainActivity` (so the composables it hosts can receive an injected ViewModel). The WorkManager plumbing inside `PostsApp` is already there; you supply the two annotations. Leave either off and Hilt refuses to build.

**Docs:** [Hilt in Android classes](https://developer.android.com/training/dependency-injection/hilt-android#android-classes)

### 3 · Make your own classes injectable

These are classes *you* wrote, so Hilt can build them directly once you declare it:
- On `PostsRepository`, change the constructor to `@Inject constructor(...)`.
- On `PostsViewModel`, add `@HiltViewModel` and `@Inject constructor(private val repository: PostsRepository)` (drop the old `= Graph.repository` default; `Graph` is gone).

`PostsScreen` already calls `hiltViewModel()`, so once annotated, the screen gets its ViewModel from Hilt.

**Make pass (JVM):** `PostsRepositoryTest`, `PostsViewModelTest` (they construct these directly with fakes, and pass once the app compiles).

**Docs:** [Hilt and ViewModels](https://developer.android.com/training/dependency-injection/hilt-jetpack#viewmodels)

### 4 · Write the module: `AppModule.kt`

Hilt can build your `@Inject` classes, but not the **Room database** or the **Retrofit API** - those come from *builders*, not constructors, so you must provide them. Write an `object AppModule` (a `@Module` installed in `SingletonComponent`) with a `@Provides` for each thing the graph needs but cannot build itself: the `AppDatabase`, the `PostsDao`, and the `PostsApi`.

You already wrote the Room builder in **Lab 6** and the Retrofit setup in **Lab 5** - look back at your own code and move that construction here, behind `@Provides`. Then decide, and be ready to defend: **which bindings should be `@Singleton`, and which should not?** (Part 2, "Scoping: less is more.") That decision is the exercise, not the annotations.

**Docs:** [Hilt modules](https://developer.android.com/training/dependency-injection/hilt-android#hilt-modules)

### 5 · Inject the worker: `RefreshWorker.kt`

WorkManager constructs the worker, so you cannot hand it a repository through a normal constructor. The fix is **assisted injection**: WorkManager supplies the runtime values (`Context`, `WorkerParameters`), Hilt supplies the repository. Work out, from Part 2 and the docs, which annotations go on the class and the constructor and which parameters are assisted; then use the injected repository in `doWork()` instead of `Graph.repository`. The `HiltWorkerFactory` wiring in `PostsApp` is already provided.

**Make pass (instrumented):** `a_successful_sync_writes_to_the_database_and_returns_success`, `a_failing_sync_asks_for_retry`.

**Docs:** [Inject WorkManager with Hilt](https://developer.android.com/training/dependency-injection/hilt-jetpack#workmanager)

---

## Part 4: Extend it

Do at least one.

1. **A `@Binds` interface.** Extract a `PostsRepository` interface and a `DefaultPostsRepository` implementation, and bind them with `@Binds` in an abstract module. When would you reach for this over `@Provides`?
2. **A qualifier.** Add a `@Named` (or a custom `@Qualifier`) `@Provides` that supplies the base URL as a `String`, and inject it into the API provider, so the URL is a single declared dependency.
3. **Read the generated graph.** Build, then look under `app/build/generated/` for the Hilt/Dagger generated components, and trace how your `PostsRepository` gets built. Seeing the code Hilt writes for you demystifies it.

For each, write one line: *what did you declare, and what did Hilt build from it?*

---

## Part 5: You are done when

```bash
./gradlew test                       # repository + ViewModel tests pass
./gradlew connectedDebugAndroidTest  # the DAO and worker tests pass (device/emulator running)
```

And the app runs exactly as before, because you changed *how* objects are wired, not *what they do*. That is the whole point of this lab: the plumbing became explicit and checkable, and the app is none the wiser.

Commit your work:
```bash
git add -A && git commit -m "Lab 8 complete"
```

Notice what did **not** change: the JVM tests. They always constructed the repository and ViewModel by hand with fakes, and `@Inject` is just an annotation, so they still do. Good DI does not fight plain construction; it adds a container on top for the app to use.

---

## Apply it to your app

Your project milestone: convert your app to Hilt. Mark your Application `@HiltAndroidApp`, give your repository (and anything it needs) constructor injection, write a module for the things you cannot annotate (database, network client), and retrieve your ViewModels with `hiltViewModel()`. Then answer: *what did injecting your dependencies make easier to test or change?*

## Using AI on this lab

Good uses: an annotation's exact name, the `@Provides` signature, a Hilt build error's meaning (they are wordy but precise). Reason unaided about the real decision: *which things are `@Inject` classes and which need a `@Provides`, and why? what should be `@Singleton` and what should not?* If you cannot explain why the database is provided but the repository is injected, you have not finished the lab.

## What's next

Lab 9 puts a **CI pipeline** around everything you have built: the fast JVM tests and the instrumented tests you have accumulated become an automatic gate, so a change that breaks the app is caught before it merges. The testable design you have been building, logic behind interfaces, dependencies injected, is exactly what makes that pipeline fast and meaningful.

*This lab adapts ideas from [Android Basics with Compose](https://developer.android.com/courses/android-basics-compose/course) (Google) and the [Hilt](https://developer.android.com/training/dependency-injection/hilt-android) documentation.*
