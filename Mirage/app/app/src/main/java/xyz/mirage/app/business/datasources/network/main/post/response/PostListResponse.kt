package xyz.mirage.app.business.datasources.network.main.post.response

import com.squareup.moshi.Json
import xyz.mirage.app.business.datasources.network.main.post.dto.PostDto

data class PostListResponse(
    @Json(name = "posts")
    val posts: List<PostDto>,

    @Json(name = "hasMore")
    val hasMore: Boolean
)
