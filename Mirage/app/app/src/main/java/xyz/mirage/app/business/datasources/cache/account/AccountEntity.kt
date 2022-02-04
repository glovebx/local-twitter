package xyz.mirage.app.business.datasources.cache.account

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import xyz.mirage.app.business.domain.models.Account

@Entity(tableName = "accounts")
data class AccountEntity(

    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "email")
    val email: String,

    @ColumnInfo(name = "username")
    val username: String,

    @ColumnInfo(name = "displayName")
    val displayName: String,

    @ColumnInfo(name = "image")
    val image: String,

    @ColumnInfo(name = "banner")
    val banner: String?,

    @ColumnInfo(name = "bio")
    val bio: String?,
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