# Lab 6: Persistence (Room and Flow)

Your app fetches posts from the network, but it forgets everything the moment the request fails or the process dies: no signal means a blank screen, and your bookmarks vanish on restart. This lab fixes that. You give the app a **local database** with **Room**, and make that database the single thing the UI reads from. The network becomes just a way to keep the database fresh. This is the heart of an offline-capable app, and it is what Lab 7 builds on to go fully offline-first.

Unlike the last few labs, here you build the persistence layer yourself: you **add the dependencies**, **define the tables (entities)**, **write the queries (a DAO)**, and **create the database**. The repository and the ViewModel that use them are already written, so you can focus on Room. You extend the **same `posts-app`**; the screen does not change.

Time: about three hours. Room is a real step, and this lab front-loads the reading so the code makes sense.

---

## Part 1: Before you start

### What is new since Lab 5
Nothing, yet: the project has **no database code and no Room dependency**. That is your job. When you are done, the app will read its posts and bookmarks from a local SQLite database that Room manages for you.

### Open the project and run the tests
Open `posts-app`. Right now it does **not compile**, because the provided repository and tests refer to classes you have not written yet (`PostEntity`, `PostsDao`, `AppDatabase`). That is expected: you make it compile and go green by doing the four exercises in order. Once it builds, run:

```bash
./gradlew test                       # fast JVM tests: fake API + fake DAO, no device
./gradlew connectedDebugAndroidTest  # the real DAO's SQL, on a device or emulator
```

Pinned tooling (verified Aug 2026): you will add **Room 2.8.5** and the **KSP 2.3.11** plugin (KSP's version number trails Kotlin's, but 2.3.11 works with the Kotlin 2.4.21 this project uses).

### The one production concern: THE LOCAL DATABASE IS THE SINGLE SOURCE OF TRUTH

A robust app does not show the user "the network response". It shows the user "what we have stored", and separately tries to keep that store fresh. The UI **observes the database**, never the network directly; the network's only job is to **write fresh data into the database**; and because the reads are **Flows**, any change to the database re-renders the screen by itself. This inversion is why your bookmarks will survive a restart, why the list appears offline, and why Lab 7 can add background sync without touching the UI.

---

## Part 2: What you need to know

This is a longer read than usual. Room has a few moving parts, and they only make sense together.

### What Room is, and why not raw SQLite

Every Android device ships with **SQLite**, a small embedded SQL database. You *could* use it directly through `SQLiteOpenHelper`: write CREATE TABLE strings, open a database, run queries that return a `Cursor`, and read each column out of the cursor by index into your objects, by hand. It works, and it is miserable: the SQL is unchecked strings, the cursor code is repetitive, a typo in a column name blows up at runtime, and nothing runs off the main thread unless you arrange it.

**Room** is a thin, official layer over SQLite that removes all of that. You describe your database as annotated Kotlin (tables as data classes, queries as interface methods), and Room **generates** the tedious cursor-and-mapping code for you at build time. You still write SQL for the interesting queries, but Room checks it, binds parameters safely, maps rows to your objects, and integrates with coroutines and Flow. You get SQLite's power without its boilerplate.

Room has exactly three kinds of piece, and this lab writes all three:

- **Entities** define the tables.
- **DAOs** define the queries.
- A **database** class ties them together.

### Entities: your tables as data classes

An **entity** is a table, written as a plain data class annotated `@Entity`. Each property becomes a column; the property types map to SQLite types automatically (`Int`, `String`, `Boolean`, and so on). One property must be the primary key, marked `@PrimaryKey`.

```kotlin
@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val body: String,
)
```

A few useful annotations you will meet: `@Entity(tableName = "...")` names the table (otherwise it uses the class name); `@ColumnInfo(name = "...")` names a column; `@PrimaryKey(autoGenerate = true)` lets SQLite assign ids. Notice the entity is a **separate model** from the domain object your UI uses and the DTO the network sends. Keeping them apart means the database schema does not change just because the server renames a JSON field or the UI needs a computed property; the repository maps at the boundary.

### The DAO: your queries as annotated methods

A **DAO** (Data Access Object) is an `interface` annotated `@Dao`, with one method per operation. You annotate each method with what it does; Room writes the body. There are two families:

**Convenience writes**, where the annotation is the whole story (no SQL):
- `@Insert(onConflict = ...)` inserts rows. The conflict strategy decides what happens if a row with that primary key already exists: `IGNORE` (keep the old one) or `REPLACE` (overwrite).
- `@Upsert` is insert-or-update: new rows are inserted, existing ones (by primary key) are replaced. Exactly what you want for a cache you refresh.
- `@Delete` deletes the passed entities.

**Explicit queries** with `@Query`, where you write the SQL:
- `@Query("SELECT * FROM posts")` reads rows.
- `@Query("DELETE FROM bookmarks WHERE id = :id")` deletes by a bound parameter. The `:id` in the SQL refers to the method parameter named `id`; Room binds it safely (no string concatenation, so no SQL injection).

```kotlin
@Dao
interface ExampleDao {
    @Query("SELECT * FROM posts WHERE title LIKE :needle")
    fun search(needle: String): Flow<List<PostEntity>>

    @Upsert
    suspend fun upsert(posts: List<PostEntity>)

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun removeBookmark(id: Int)
}
```

#### Return types, mapping, and joins

Here is a point worth internalising: **a `@Query` method's return type is not the query; it is a request for how to shape the result.** Room runs your SQL, gets back rows and columns, and then maps those columns onto whatever type you asked for, by name. It does not care what that type is, as long as every column in the result can be matched to a property.

That has real consequences:
- `SELECT * FROM posts` can return `List<PostEntity>` (all columns map) or `Flow<List<PostEntity>>` (the same, observed over time).
- `SELECT id FROM bookmarks` returns just one column, so you can map it to `List<Int>` directly.
- `SELECT COUNT(*) FROM posts` maps to `Int`.
- A **join** across two tables can return a data class that is not an entity at all:

  ```kotlin
  data class PostWithBookmark(val id: Int, val title: String, val bookmarked: Boolean)

  @Query("""
      SELECT p.id, p.title, (b.id IS NOT NULL) AS bookmarked
      FROM posts p LEFT JOIN bookmarks b ON p.id = b.id
  """)
  fun postsWithBookmarkFlag(): Flow<List<PostWithBookmark>>
  ```

  Room sees the columns `id`, `title`, `bookmarked` and fills the class's properties of the same names. The result class is just a shape; it needs no `@Entity`, no table, nothing. (This app keeps posts and bookmarks in two Flows and combines them in the ViewModel, which is simpler to start with; the join is how you would do it in one query, and a good thing to try in the extensions.)

### KSP: how Room turns annotations into code

Your DAO is an interface with no bodies, and your `@Database` is an abstract class. Something has to generate the real implementations. That something is **KSP** (Kotlin Symbol Processing): a build-time plugin that reads the annotations in your source and **generates Kotlin code** before the compiler runs. Room ships a KSP processor (`room-compiler`); when you build, it produces the concrete DAO (the cursor code) and the concrete database.

This is why Room has two dependencies with different roles:
- `implementation("androidx.room:room-runtime:...")` is the library your code calls at runtime.
- `ksp("androidx.room:room-compiler:...")` is the code generator, applied through the KSP plugin. It is **not** shipped in your app; it only runs at build time. That is why it is declared with `ksp(...)`, not `implementation(...)`.

(KSP is the modern replacement for KAPT, the older annotation processor; KSP is Kotlin-native and much faster.)

### Compile-time safety: your SQL is checked before you run

Because KSP runs at build time with full knowledge of your schema, **Room validates your queries when you compile, not when the user taps a button.** Misspell a column, `SELECT` a field that does not exist, return a type whose properties do not match the columns, forget to bind a parameter, and the build fails with a specific error pointing at the method. This is a big deal: a whole class of database bugs becomes a red build instead of a production crash. It is also why the DAO cannot be left half-written and still compile.

### Thread safety, main-thread safety, and coroutines

Touching the disk is slow, and Android forbids slow work on the main thread (it freezes the UI, and Room will actually throw if you try a blocking query on the main thread). Room is built for coroutines, and the shape of a method decides its threading:

- **Writes are `suspend`.** `suspend fun upsert(...)` runs on a background dispatcher and suspends the caller without blocking a thread. You call it from a coroutine; Room handles the threading.
- **Reactive reads are `Flow`.** A `@Query` that returns `Flow<List<T>>` is not a one-shot read: Room watches the tables the query touches and **emits a new result every time they change**. The ViewModel collects that Flow, so a write anywhere (a refresh, a bookmark toggle) reaches the screen automatically. Reads are reactive; writes are one-shot suspend calls.

So `suspend` versus `Flow` is a real choice: `suspend` for a one-time action (write, or a single read), `Flow` for "keep me updated". Room itself is thread-safe; you do not add locks.

### The database class

Finally, a single abstract class annotated `@Database` lists your entities and exposes each DAO. Room generates its implementation. You never instantiate it with `new`; you build it with `Room.databaseBuilder(context, AppDatabase::class.java, "posts.db")`, which needs a `Context` (a file has to live somewhere). That is why the provided `Graph` builds it once in the `Application`.

### The repository's role

The repository is the seam that makes "the database is the source of truth" real, and it is **provided** so you can read it as the reference. Its job:
- expose the DAO's Flows to the rest of the app, mapping entities to domain models (`observePosts()`, `observeBookmarkIds()`);
- offer write actions that go to the database (`refresh()` fetches from the network and writes to the DAO; `setBookmarked()` adds or removes a bookmark);
- and crucially, **never hand network data straight to the UI**. `refresh()` returns nothing to the screen; it writes to Room, and the Flow does the rest.

Read `PostsRepository.kt` and `PostsViewModel.kt` once you have the database compiling, and trace how a tapped "Save" becomes a row in the `bookmarks` table and then a filled star on screen, with the ViewModel never told directly.

---

## Part 3: The exercises

Do them in order. The app does not compile until all four are done, because the provided code depends on them. The JVM tests use a fake DAO, so they cannot check your SQL; the instrumented `PostsDaoTest` runs your real queries, so run it too.

### 1 · Add the Room dependencies and the KSP plugin

Add Room and KSP to the build yourself (this is a skill: from here on, you own your dependencies).

- In `gradle/libs.versions.toml`: add versions `room = "2.8.5"` and `ksp = "2.3.11"`; add libraries `androidx-room-runtime` (`androidx.room:room-runtime`) and `androidx-room-compiler` (`androidx.room:room-compiler`), both `version.ref = "room"`; and a plugin `ksp` with id `com.google.devtools.ksp`, `version.ref = "ksp"`.
- In the top-level `build.gradle.kts`: add `alias(libs.plugins.ksp) apply false`.
- In `app/build.gradle.kts`: apply `alias(libs.plugins.ksp)` in the `plugins {}` block, and in `dependencies {}` add `implementation(libs.androidx.room.runtime)` and `ksp(libs.androidx.room.compiler)`.

Note the two different scopes: `implementation` for the runtime library, `ksp` for the compiler. Sync Gradle.

**Docs:** [Room setup](https://developer.android.com/training/data-storage/room#setup) · [KSP](https://kotlinlang.org/docs/ksp-overview.html)

### 2 · Write the entities: `Entities.kt`

Define two `@Entity` data classes:
- `PostEntity` (table `posts`): `id: Int` (the `@PrimaryKey`), `title: String`, `body: String`.
- `BookmarkEntity` (table `bookmarks`): `id: Int` (the `@PrimaryKey`).

(The provided mappers in `AppData.kt` use these exact field names.)

**Docs:** [Defining data with Room entities](https://developer.android.com/training/data-storage/room/defining-data)

### 3 · Write the DAO: `PostsDao.kt`

Write a `@Dao interface PostsDao` with these methods (match the signatures; the repository and the fake DAO in the tests rely on them):
- `observePosts(): Flow<List<PostEntity>>` with `@Query` selecting every row from `posts`.
- `upsertPosts(posts: List<PostEntity>)`, `suspend`, `@Upsert`.
- `observeBookmarkIds(): Flow<List<Int>>` with `@Query` selecting the `id` column from `bookmarks`.
- `addBookmark(bookmark: BookmarkEntity)`, `suspend`, `@Insert` with `onConflict = OnConflictStrategy.IGNORE`.
- `removeBookmark(id: Int)`, `suspend`, `@Query` deleting the `bookmarks` row whose `id` equals the bound `:id`.

**Make pass (instrumented):** `upserting_posts_makes_them_observable`, `bookmarks_can_be_added_and_removed`.

**Docs:** [Accessing data with DAOs](https://developer.android.com/training/data-storage/room/accessing-data) · [Observable queries](https://developer.android.com/training/data-storage/room/async-queries#observable)

### 4 · Create the database: `AppDatabase.kt`

Write an abstract class `AppDatabase : RoomDatabase()` annotated `@Database` that lists both entities (`version = 1`, `exportSchema = false`) and exposes `abstract fun postsDao(): PostsDao`.

Once this compiles, the whole app compiles: the provided `Graph` builds the database, the repository observes it, and the ViewModel shows it.

**Docs:** [Save data in a local database](https://developer.android.com/training/data-storage/room)

### Then: run it

The repository (`PostsRepository.kt`) and ViewModel (`PostsViewModel.kt`) are written for you; read them (see "the repository's role" above). Run the app: bookmark some posts, kill it, reopen it, and the bookmarks are still there; turn off the network and the posts still load from the cache.

**Make pass (JVM):** all of `PostsRepositoryTest` and `PostsViewModelTest` (they exercise the provided code against your DAO through a fake, and go green once your Room layer compiles and behaves).

---

## Part 4: Extend it

Verify by running the app; add a test where you can. Do at least one.

1. **One query instead of two Flows.** Add the join from Part 2 (`postsWithBookmarkFlag()` returning a `Flow<List<PostWithBookmark>>`) to the DAO and prove it works in `PostsDaoTest`. This is the return-type-is-just-a-shape idea in action.
2. **Prove it is offline.** Run once with internet (it caches), then airplane-mode and relaunch: the posts still appear. Add a "showing cached data" note when `errorMessage != null` but `posts` is not empty.
3. **Clear the cache.** Add a DAO `@Query("DELETE FROM posts")` and a button that empties the cache, then watch the list refill on the next refresh.

For each, write one line: *what wrote to the database, and how did the screen find out?*

---

## Part 5: You are done when

```bash
./gradlew test                       # repository + ViewModel tests pass
./gradlew connectedDebugAndroidTest  # the DAO test passes (device/emulator running)
```

And the app proves the point: bookmarks survive a restart, and posts load from the cache with the network off. That is persistence, and it is the single-source-of-truth architecture doing its job.

Notice again which tests need a device: only the DAO, because only its SQL is real Android. The repository and ViewModel are pure logic over a fake DAO, tested in milliseconds on the JVM. That split is the CI story of Lab 9.

Commit your work:
```bash
git add -A && git commit -m "Lab 6 complete"
```

---

## Apply it to your app

Your project milestone: give your app a Room database that is its source of truth. Add the Room dependency, define an entity and a DAO for your main record, and have your repository observe the DAO with Flow and write network results into it, so one screen keeps working with no network. Then answer the systems question you will defend: *what is stored on the device, and what happens the first time the user opens the app offline?* Work it through the **five questions** you use for every design decision this course - the same frame you meet in Course 6, on the written exam, and in the project defence: *What binds? · What will you do? · What are you giving up? · What breaks? · What would change your mind?*

## Using AI on this lab

Good uses: Room annotation syntax, an SQLite query, a KSP build error, how `@Upsert` differs from `@Insert`. Reason unaided about the real decisions: *why does the UI observe the database instead of the network? why are bookmarks a separate table from the cached posts? why is a read a Flow but a write a suspend function?* If you cannot answer those for your own app, you have not finished the lab.

## What's next

Lab 7 makes the app truly **offline-first**: a background worker (WorkManager) refreshes the database on a schedule and under constraints (only on a network, not on low battery), so the app always shows recent data without the user pressing refresh. The database as source of truth, which you built here, is what makes that possible.

*This lab adapts ideas from [Android Basics with Compose](https://developer.android.com/courses/android-basics-compose/course) (Google) and the [Room](https://developer.android.com/training/data-storage/room) documentation.*
