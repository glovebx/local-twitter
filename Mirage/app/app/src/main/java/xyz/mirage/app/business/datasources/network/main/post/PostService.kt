package xyz.mirage.app.business.datasources.network.main.post

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*
import xyz.mirage.app.business.datasources.network.main.post.dto.PostDto
import xyz.mirage.app.business.datasources.network.main.post.response.PostListResponse

interface PostService {

    @GET("posts")
    suspend fun searchPosts(
        @Query("cursor") cursor: String?,
        @Query("search") search: String
    ): PostListResponse

    @GET("posts/feed")
    suspend fun feed(
        @Query("cursor") cursor: String?
    ): PostListResponse

    @GET("posts/{id}")
    suspend fun get(
        @Path("id") id: String
    ): PostDto

    @POST("posts/{id}/like")
    suspend fun toggleLike(
        @Path("id") id: String
    ): PostDto

    @POST("posts/{id}/retweet")
    suspend fun toggleRetweet(
        @Path("id") id: String
    ): PostDto

    @DELETE("posts/{id}")
    suspend fun deletePost(
        @Path("id") id: String
    ): PostDto

    @Multipart
    @POST("posts")
    suspend fun createPost(
        @Part("text") text: RequestBody?,
        @Part file: MultipartBody.Part?
    ): PostDto
}
