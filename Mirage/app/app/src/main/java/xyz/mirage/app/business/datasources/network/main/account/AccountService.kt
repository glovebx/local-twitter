package xyz.mirage.app.business.datasources.network.main.account

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PUT
import retrofit2.http.Part
import xyz.mirage.app.business.datasources.network.auth.response.AccountResponse

interface AccountService {

    @GET("accounts")
    suspend fun getCurrent(): AccountResponse

    @Multipart
    @PUT("accounts")
    suspend fun updateAccount(
        @Part("email") email: RequestBody,
        @Part("username") username: RequestBody,
        @Part("displayName") displayName: RequestBody,
        @Part("bio") bio: RequestBody?,
        @Part image: MultipartBody.Part?,
        @Part banner: MultipartBody.Part?
    ): AccountResponse
}
