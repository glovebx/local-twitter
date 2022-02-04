package xyz.mirage.app.di.interactors

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import xyz.mirage.app.business.datasources.cache.account.AccountDao
import xyz.mirage.app.business.datasources.network.main.account.AccountService
import xyz.mirage.app.business.interactors.account.GetAccount
import xyz.mirage.app.business.interactors.account.GetAccountFromCache
import xyz.mirage.app.business.interactors.account.UpdateAccount
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AccountModule {

    @Singleton
    @Provides
    fun provideGetCurrent(
        accountDao: AccountDao,
        service: AccountService,
    ): GetAccount {
        return GetAccount(
            cache = accountDao,
            service = service
        )
    }

    @Singleton
    @Provides
    fun provideGetCurrentFromCache(
        accountDao: AccountDao,
    ): GetAccountFromCache {
        return GetAccountFromCache(
            cache = accountDao,
        )
    }

    @Singleton
    @Provides
    fun provideUpdateAccount(
        accountDao: AccountDao,
        service: AccountService,
    ): UpdateAccount {
        return UpdateAccount(
            cache = accountDao,
            service = service
        )
    }
}