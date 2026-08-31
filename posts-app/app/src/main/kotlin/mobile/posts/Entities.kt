package mobile.posts

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Exercise 2. The DATABASE models: how a post and a bookmark are stored on the device.
 *
 * An @Entity is a table; each property is a column; @PrimaryKey marks the unique id. These are kept
 * separate from the network model (PostDto) and the domain model (Post): the repository maps between
 * them, so a change to the server's JSON or the UI never forces a change to your schema.
 *
 * PostEntity is the cached posts (table "posts"); BookmarkEntity is the saved ids in their own table
 * ("bookmarks"), so refreshing the posts cache never disturbs the user's bookmarks.
 */
@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val body: String,
)

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val id: Int,
)
