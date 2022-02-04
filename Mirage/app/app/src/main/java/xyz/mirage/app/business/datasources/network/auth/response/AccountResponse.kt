package xyz.mirage.app.business.datasources.network.auth.response

import com.squareup.moshi.Json
import xyz.mirage.app.business.domain.models.Account

data class AccountResponse(
    @Json(name = "id")
    val id: String,

    @Json(name = "email")
    val email: String,

    @Json(name = "username")
    val username: String,

    @Json(name = "displayName")
    val displayName: String,

    @Json(name = "bio")
    val bio: String?,

    @Json(name = "banner")
    val banner: String?,

    @Json(name = "image")
    val image: String,
) {
    fun toAccount(): Account {
        return Account(
            id = id,
            username = username,
            displayName = displayName,
            image = image,
            bio = bio,
            email = email,
            banner = banner,
        )
    }
}