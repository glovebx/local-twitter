package xyz.mirage.app.business.interactors.account

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import xyz.mirage.app.business.datasources.cache.account.AccountDao
import xyz.mirage.app.business.datasources.network.core.handleUseCaseException
import xyz.mirage.app.business.domain.core.DataState
import xyz.mirage.app.business.domain.core.ErrorHandling
import xyz.mirage.app.business.domain.models.Account

class GetAccountFromCache(
    private val cache: AccountDao,
) {
    fun execute(
        id: String,
    ): Flow<DataState<Account>> = flow {
        emit(DataState.loading<Account>())
        // emit from cache
        val cachedAccount = cache.searchById(id)?.toAccount()
            ?: throw Exception(ErrorHandling.ERROR_UNABLE_TO_RETRIEVE_ACCOUNT_DETAILS)

        emit(DataState.data(response = null, cachedAccount))
    }.catch { e ->
        emit(handleUseCaseException(e))
    }
}
