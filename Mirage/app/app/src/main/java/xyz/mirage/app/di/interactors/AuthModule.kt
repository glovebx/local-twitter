package xyz.mirage.app.di.interactors

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import xyz.mirage.app.business.datasources.cache.account.AccountDao
import xyz.mirage.app.business.datasources.datastore.AppDataStore
import xyz.mirage.app.business.datasources.network.auth.AuthService
import xyz.mirage.app.business.interactors.auth.Login
import xyz.mirage.app.business.interactors.auth.Register
import xyz.mirage.app.business.interactors.session.CheckPreviousAuthUser
import xyz.mirage.app.business.interactors.session.Logout
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Singleton
    @Provides
    fun provideLogout(
        accountDao: AccountDao,
    ): Logout {
        return Logout(
            accountDao = accountDao,
        )
    }

    @Singleton
    @Provides
    fun provideCheckPreviousAuthUser(
        accountDao: AccountDao,
    ): CheckPreviousAuthUser {
        return CheckPreviousAuthUser(
            accountDao = accountDao,
        )
    }

    @Singleton
    @Provides
    fun provideLogin(
        accountDao: AccountDao,
        authService: AuthService,
        appDataStoreManager: AppDataStore
    ): Login {
        return Login(
            accountDao = accountDao,
            service = authService,
            appDataStoreManager = appDataStoreManager
        )
    }

    @Singleton
    @Provides
    fun provideRegister(
        accountDao: AccountDao,
        authService: AuthService,
        appDataStoreManager: AppDataStore
    ): Register {
        return Register(
            accountDao = accountDao,
            service = authService,
            appDataStoreManager = appDataStoreManager
        )
    }
}