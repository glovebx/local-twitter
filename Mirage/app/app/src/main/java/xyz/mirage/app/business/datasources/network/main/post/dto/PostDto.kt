package xyz.mirage.app.business.datasources.network.main.post.dto

import com.squareup.moshi.Json
import xyz.mirage.app.business.domain.models.Post

data class PostDto(
    @Json(name = "id")
    val id: String,

    @Json(name = "text")
    var text: String?,

    @Json(name = "likes")
    val likes: Int = 0,

    @Json(name = "liked")
    val liked: Boolean = false,

    @Json(name = "retweets")
    val retweets: Int = 0,

    @Json(name = "retweeted")
    val retweeted: Boolean = false,

    @Json(name = "isRetweet")
    val isRetweet: Boolean = false,

    @Deprecated("use files property instead")
    @Json(name = "file")
    val file: AttachmentDto?,

    @Json(name = "files")
    val files: List<AttachmentDto>?,

    @Json(name = "author")
    val author: ProfileDto,

    @field:Json(name = "created_at")
    val createdAt: String,

    @field:Json(name = "text_zh")
    val textZh: String?,
) {
    fun toPost(): Post {
        return Post(
            id = id,
            text = text,
            likes = likes,
            liked = liked,
            retweets = retweets,
            retweeted = retweeted,
            isRetweet = isRetweet,
            file = file?.toAttachment(),
            files = files?.toAttachmentList(),
            profile = author.toProfile(),
            createdAt = createdAt
        )
    }
}

fun List<PostDto>.toPostList(): List<Post> {
    return map { it.toPost() }
}
