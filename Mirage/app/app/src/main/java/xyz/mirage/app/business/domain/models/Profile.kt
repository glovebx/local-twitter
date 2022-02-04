package xyz.mirage.app.business.domain.models

import xyz.mirage.app.business.datasources.cache.post.ProfileEntity

data class Profile(
    val id: String,
    val username: String,
    val displayName: String,
    val image: String,
    val bio: String?,
    val banner: String?,
    var followers: Int = 0,
    val followee: Int = 0,
    var following: Boolean,
    val createdAt: String,
) {
    fun toEntity(): ProfileEntity {
        return ProfileEntity(
            id = id,
            username = username,
            displayName = displayName,
            image = image,
            bio = bio,
            banner = banner,
            following = following,
            followers = followers,
            followee = followee,
            createdAt = createdAt,
        )
    }
}