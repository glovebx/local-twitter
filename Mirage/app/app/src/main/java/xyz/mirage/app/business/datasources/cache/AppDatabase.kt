package xyz.mirage.app.business.datasources.cache

import androidx.room.Database
import androidx.room.RoomDatabase
import xyz.mirage.app.business.datasources.cache.account.AccountDao
import xyz.mirage.app.business.datasources.cache.account.AccountEntity
import xyz.mirage.app.business.datasources.cache.post.PostDao
import xyz.mirage.app.business.datasources.cache.post.PostEntity
import xyz.mirage.app.business.datasources.cache.post.ProfileEntity
import xyz.mirage.app.business.datasources.cache.profile.ProfileDao

@Database(
    entities = [PostEntity::class, AccountEntity::class, ProfileEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun postDao(): PostDao
    abstract fun accountDao(): AccountDao
    abstract fun profileDao(): ProfileDao

    companion object {
        const val DATABASE_NAME: String = "mirage_db"
    }

}