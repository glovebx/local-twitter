package xyz.mirage.app.business.datasources.cache.post

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "text")
    val text: String?,

    @ColumnInfo(name = "likes")
    val likes: Int = 0,

    @ColumnInfo(name = "liked")
    val liked: Boolean,

    @ColumnInfo(name = "retweets")
    val retweets: Int = 0,

    @ColumnInfo(name = "retweeted")
    val retweeted: Boolean,

    @ColumnInfo(name = "is_retweet")
    val isRetweet: Boolean,

    @ColumnInfo(name = "file")
    val file: String?,

    @ColumnInfo(name = "files")
    val files: String?,

    @ColumnInfo(name = "authorId")
    val authorId: String,

    @ColumnInfo(name = "created_at")
    val createdAt: String,

    @ColumnInfo(name = "date_cached")
    var dateCached: Long,

    @ColumnInfo(name = "isFeed")
    var isFeed: Boolean,
)