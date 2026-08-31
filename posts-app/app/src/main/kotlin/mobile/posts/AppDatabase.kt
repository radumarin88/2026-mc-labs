package mobile.posts

/*
 * Exercise 4. The database. See docs/lab6.md, Part 2 "The database class" and exercise 4.
 *
 * Write an abstract class `AppDatabase : RoomDatabase()` annotated with:
 *   @Database(entities = [PostEntity::class, BookmarkEntity::class], version = 1, exportSchema = false)
 * and one abstract accessor:
 *   abstract fun postsDao(): PostsDao
 *
 * Room generates the implementation at build time (via KSP). Once this compiles, the whole app does:
 * the provided Graph builds the database, the repository observes it, and the ViewModel shows it.
 * (Add the imports: androidx.room.Database, androidx.room.RoomDatabase.)
 */
