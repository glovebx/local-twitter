package xyz.mirage.app.business.interactors.session

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import xyz.mirage.app.business.datasources.cache.account.AccountDao
import xyz.mirage.app.business.domain.core.DataState
import xyz.mirage.app.business.domain.core.MessageType
import xyz.mirage.app.business.domain.core.Response
import xyz.mirage.app.business.domain.core.SuccessHandling.Companion.SUCCESS_LOGOUT
import xyz.mirage.app.business.domain.core.UIComponentType

class Logout(
    private val accountDao: AccountDao,
) {
    fun execute(): Flow<DataState<Response>> = flow {
        emit(DataState.loading())
        accountDao.clear()
        emit(
            DataState.data(
                data = Response(
                    message = SUCCESS_LOGOUT,
                    uiComponentType = UIComponentType.Dialog(),
                    messageType = MessageType.Error()
                ),
                response = null,
            )
        )
    }.catch { e ->
        e.printStackTrace()
        emit(
            DataState.error(
                response = Response(
                    message = e.message,
                    uiComponentType = UIComponentType.Dialog(),
                    messageType = MessageType.Error()
                )
            )
        )
    }
}
