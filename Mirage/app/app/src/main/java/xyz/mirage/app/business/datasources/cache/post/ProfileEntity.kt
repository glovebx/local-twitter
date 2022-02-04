package xyz.mirage.app.business.datasources.cache.post

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import xyz.mirage.app.business.domain.models.Profile

@Entity(tableName = "authors")
data class ProfileEntity(

    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "username")
    val username: String,

    @ColumnInfo(name = "displayName")
    val displayName: String,

    @ColumnInfo(name = "image")
    val image: String,

    @ColumnInfo(name = "bio")
    val bio: String?,

    @ColumnInfo(name = "banner")
    val banner: String?,

    @ColumnInfo(name = "followers")
    val followers: Int = 0,

    @ColumnInfo(name = "followee")
    val followee: Int = 0,

    @ColumnInfo(name = "following")
    val following: Boolean,

    @ColumnInfo(name = "createdAt")
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
            following = following,
            followers = followers,
            followee = followee,
            createdAt = createdAt
        )
    }
}