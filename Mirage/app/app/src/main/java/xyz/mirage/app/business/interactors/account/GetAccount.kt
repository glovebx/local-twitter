package xyz.mirage.app.business.interactors.account

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import xyz.mirage.app.business.datasources.cache.account.AccountDao
import xyz.mirage.app.business.datasources.network.core.handleUseCaseException
import xyz.mirage.app.business.datasources.network.main.account.AccountService
import xyz.mirage.app.business.domain.core.DataState
import xyz.mirage.app.business.domain.core.ErrorHandling.Companion.ERROR_UNABLE_TO_RETRIEVE_ACCOUNT_DETAILS
import xyz.mirage.app.business.domain.models.Account

class GetAccount(
    private val service: AccountService,
    private val cache: AccountDao,
) {
    fun execute(): Flow<DataState<Account>> = flow {
        emit(DataState.loading<Account>())

        // get from network
//        val account = service.getCurrent().toAccount()

        // update/insert into the cache
//        cache.insertOrIgnore(account.toEntity())

        // emit from cache
        val cachedAccount = cache.searchById("1000"/*account.id*/)?.toAccount()
            ?: throw Exception(ERROR_UNABLE_TO_RETRIEVE_ACCOUNT_DETAILS)

        emit(DataState.data(response = null, cachedAccount))
    }.catch { e ->
        emit(handleUseCaseException(e))
    }
}
