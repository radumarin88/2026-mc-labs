package mobile.posts

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Exercise 4. The database. It lists every entity (its tables) and exposes each DAO. Room reads this
 * annotation at build time (through KSP) and generates the real implementation, including the SQLite
 * schema. exportSchema is off for the lab; a shipping app exports schemas to test migrations.
 */
@Database(entities = [PostEntity::class, BookmarkEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun postsDao(): PostsDao
}
