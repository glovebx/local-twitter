package xyz.mirage.app.business.datasources.network.auth.dto

import com.squareup.moshi.Json

data class RegisterDTO(
    @Json(name = "email")
    val email: String,

    @Json(name = "username")
    val username: String,

    @Json(name = "displayName")
    val displayName: String,

    @Json(name = "password")
    val password: String,
)