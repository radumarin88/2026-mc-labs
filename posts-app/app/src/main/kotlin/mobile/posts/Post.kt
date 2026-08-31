package mobile.posts

import kotlinx.serialization.Serializable

/**
 * The NETWORK model: the exact shape the server sends. Added in Lab 5. @Serializable lets
 * kotlinx.serialization turn the JSON into this object.
 */
@Serializable
data class PostDto(
    val id: Int,
    val title: String,
    val body: String,
)

/**
 * The DOMAIN model the rest of the app uses. Kept separate from [PostDto] so the app does not depend
 * on the server's JSON shape; the repository maps at the boundary.
 */
data class Post(
    val id: Int,
    val title: String,
    val body: String,
)
