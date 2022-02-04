package xyz.mirage.app.business.interactors.session

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import xyz.mirage.app.business.datasources.cache.account.AccountDao
import xyz.mirage.app.business.domain.core.*
import xyz.mirage.app.business.domain.core.ErrorHandling.Companion.ERROR_NO_PREVIOUS_AUTH_USER
import xyz.mirage.app.business.domain.models.Account

/**
 * Attempt to authenticate as soon as the user launches the app.
 * If no user was authenticated in a previous session then do nothing.
 */
class CheckPreviousAuthUser(
    private val accountDao: AccountDao,
) {
    fun execute(
        username: String,
    ): Flow<DataState<Account>> = flow {
        emit(DataState.loading())

        val entity = accountDao.searchByUsername(username)
        if (entity != null) {
            emit(DataState.data(response = null, data = entity.toAccount()))
        } else {
            throw Exception(ERROR_NO_PREVIOUS_AUTH_USER)
        }
    }.catch { e ->
        e.printStackTrace()
        emit(returnNoPreviousAuthUser())
    }

    /**
     * If no user was previously authenticated then emit this error. The UI is waiting for it.
     */
    private fun returnNoPreviousAuthUser(): DataState<Account> {
        return DataState.error(
            response = Response(
                SuccessHandling.RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE,
                UIComponentType.None(),
                MessageType.Error()
            )
        )
    }
}