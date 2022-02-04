package xyz.mirage.app.business.datasources.network.auth

import retrofit2.http.Body
import retrofit2.http.POST
import xyz.mirage.app.business.datasources.network.auth.dto.LoginDTO
import xyz.mirage.app.business.datasources.network.auth.dto.RegisterDTO
import xyz.mirage.app.business.datasources.network.auth.response.AccountResponse

interface AuthService {

    @POST("accounts/login")
    suspend fun loginAccount(
        @Body body: LoginDTO
    ): AccountResponse

    @POST("accounts/register")
    suspend fun registerAccount(
        @Body body: RegisterDTO
    ): AccountResponse

    @POST("accounts/logout")
    suspend fun logout(): Boolean
}