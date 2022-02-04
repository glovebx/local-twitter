package xyz.mirage.app.business.domain.models

import xyz.mirage.app.business.datasources.cache.account.AccountEntity

data class Account(
    val id: String,
    val email: String,
    val username: String,
    val displayName: String,
    val bio: String?,
    val image: String,
    val banner: String?,
) {
    fun toEntity(): AccountEntity {
        return AccountEntity(
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