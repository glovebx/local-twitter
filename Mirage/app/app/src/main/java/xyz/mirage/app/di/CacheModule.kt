package xyz.mirage.app.di

import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import xyz.mirage.app.business.datasources.cache.AppDatabase
import xyz.mirage.app.business.datasources.cache.account.AccountDao
import xyz.mirage.app.business.datasources.cache.post.PostDao
import xyz.mirage.app.business.datasources.cache.profile.ProfileDao
import xyz.mirage.app.presentation.BaseApplication
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CacheModule {

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE posts ADD COLUMN files TEXT")
        }
    }

    @Singleton
    @Provides
    fun provideDb(app: BaseApplication): AppDatabase {
        return Room
            .databaseBuilder(app, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
            .addMigrations(MIGRATION_1_2)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Singleton
    @Provides
    fun providePostDao(db: AppDatabase): PostDao {
        return db.postDao()
    }

    @Singleton
    @Provides
    fun provideAccountDao(db: AppDatabase): AccountDao {
        return db.accountDao()
    }

    @Singleton
    @Provides
    fun provideProfileDao(db: AppDatabase): ProfileDao {
        return db.profileDao()
    }

}