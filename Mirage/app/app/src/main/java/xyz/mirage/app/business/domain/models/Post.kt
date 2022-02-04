package xyz.mirage.app.business.domain.models

data class Post(
    val id: String,
    val text: String?,
    var likes: Int = 0,
    var liked: Boolean = false,
    var retweets: Int = 0,
    var retweeted: Boolean = false,
    val isRetweet: Boolean = false,
    var file: Attachment?,
    val files: List<Attachment>?,
    val profile: Profile,
    val createdAt: String
)