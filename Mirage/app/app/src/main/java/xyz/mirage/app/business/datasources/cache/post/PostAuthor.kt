package xyz.mirage.app.business.datasources.cache.post

import androidx.room.Embedded
import androidx.room.Relation
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import xyz.mirage.app.business.datasources.network.main.post.dto.PostDto
import xyz.mirage.app.business.domain.models.Attachment
import xyz.mirage.app.business.domain.models.Post
import xyz.mirage.app.presentation.core.util.DateUtils
import java.lang.reflect.ParameterizedType

val moshi: Moshi = Moshi.Builder().build()
val fileAdapter: JsonAdapter<Attachment> = moshi.adapter(Attachment::class.java)

val jsonListType: ParameterizedType = Types.newParameterizedType(List::class.java, Attachment::class.java)
val filesAdapter: JsonAdapter<List<Attachment>> = moshi.adapter(jsonListType)

data class PostAuthor(
    @Embedded
    val post: PostEntity,
    @Relation(
        parentColumn = "authorId",
        entityColumn = "id"
    )
    val author: ProfileEntity
) {
    fun toPost(): Post {
        return Post(
            id = post.id,
            text = post.text,
            likes = post.likes,
            liked = post.liked,
            retweets = post.retweets,
            retweeted = post.retweeted,
            isRetweet = post.isRetweet,
            file = post.file?.let { convertStringToFile(it) },
            files = post.files?.let { convertStringToFiles(it) },
            profile = author.toProfile(),
            createdAt = post.createdAt,
        )
    }

    private fun convertStringToFile(file: String): Attachment {
        return fileAdapter.fromJson(file) ?: throw Exception("Couldn't restore attachment")
    }

    private fun convertStringToFiles(files: String): List<Attachment> {
        return filesAdapter.fromJson(files) ?: throw Exception("Couldn't restore attachments")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PostAuthor

        if (post != other.post) return false
        if (author != other.author) return false

        return true
    }

    override fun hashCode(): Int {
        var result = post.hashCode()
        result = 31 * result + author.hashCode()
        return result
    }
}

private fun convertFileToString(file: Attachment): String {
    return fileAdapter.toJson(file)
}
private fun convertFilesToString(files: List<Attachment>): String {
    return filesAdapter.toJson(files)
}

fun Post.toEntity(isFeed: Boolean = true): PostEntity {
    return PostEntity(
        id = id,
        text = text,
        likes = likes,
        liked = liked,
        retweets = retweets,
        retweeted = retweeted,
        isRetweet = isRetweet,
        file = file?.let { convertFileToString(it) },
        files = files?.let { convertFilesToString(it) },
        authorId = profile.id,
        createdAt = createdAt,
        dateCached = DateUtils.createTimestamp(),
        isFeed = isFeed
    )
}

fun List<PostAuthor>.toPostList(): List<Post> {
    return map { it.toPost() }
}