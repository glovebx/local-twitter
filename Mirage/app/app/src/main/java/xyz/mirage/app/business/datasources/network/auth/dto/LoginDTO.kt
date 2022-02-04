package xyz.mirage.app.business.datasources.network.auth.dto

import com.squareup.moshi.Json

data class LoginDTO(
    @Json(name = "email")
    val email: String,

    @Json(name = "password")
    val password: String,
)