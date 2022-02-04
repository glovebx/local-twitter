package xyz.mirage.app.business.datasources.network.main.post.dto

import com.squareup.moshi.Json
import xyz.mirage.app.business.domain.models.Profile

data class ProfileDto(
    @Json(name = "id")
    val id: String,

    @Json(name = "username")
    val username: String,

    @Json(name = "displayName")
    val displayName: String,

    @Json(name = "image")
    val image: String,

    @Json(name = "bio")
    val bio: String?,

    @Json(name = "banner")
    val banner: String?,

    @Json(name = "followers")
    val followers: Int = 0,

    @Json(name = "followee")
    val followee: Int = 0,

    // WHY？https://github.com/square/moshi/issues/446
    @Json(name = "following")
    val following: Boolean = false,

    @field:Json(name = "created_at")
    val createdAt: String,
) {
    fun toProfile(): Profile {
        return Profile(
            id = id,
            username = username,
            displayName = displayName,
            image = image,
            bio = bio,
            banner = banner,
            followers = followers,
            followee = followee,
            following = following,
            createdAt = createdAt
        )
    }
}