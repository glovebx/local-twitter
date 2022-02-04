package xyz.mirage.app.business.datasources.network.main.profile

import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import xyz.mirage.app.business.datasources.network.main.post.dto.ProfileDto
import xyz.mirage.app.business.datasources.network.main.post.response.PostListResponse

interface ProfileService {

    @GET("profiles")
    suspend fun searchProfiles(
        @Query("search") search: String
    ): List<ProfileDto>

    @GET("profiles/{username}")
    suspend fun getProfile(
        @Path("username") username: String
    ): ProfileDto

    @GET("profiles/{username}/posts")
    suspend fun getProfilePosts(
        @Path("username") username: String,
        @Query("cursor") cursor: String?
    ): PostListResponse

    @GET("profiles/{username}/likes")
    suspend fun getProfileLikes(
        @Path("username") username: String,
        @Query("cursor") cursor: String?
    ): PostListResponse

    @GET("profiles/{username}/media")
    suspend fun getProfileMedia(
        @Path("username") username: String,
        @Query("cursor") cursor: String?
    ): PostListResponse


    @POST("profiles/{username}/follow")
    suspend fun toggleFollow(
        @Path("username") username: String,
    ): ProfileDto
}
