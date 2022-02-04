package xyz.mirage.app.business.interactors.account

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import okhttp3.MultipartBody
import okhttp3.RequestBody
import xyz.mirage.app.business.datasources.cache.account.AccountDao
import xyz.mirage.app.business.datasources.network.core.handleUseCaseException
import xyz.mirage.app.business.datasources.network.main.account.AccountService
import xyz.mirage.app.business.domain.core.DataState
import xyz.mirage.app.business.domain.core.MessageType
import xyz.mirage.app.business.domain.core.Response
import xyz.mirage.app.business.domain.core.SuccessHandling.Companion.SUCCESS_ACCOUNT_UPDATED
import xyz.mirage.app.business.domain.core.UIComponentType
import xyz.mirage.app.business.domain.models.Account

class UpdateAccount(
    private val service: AccountService,
    private val cache: AccountDao,
) {
    fun execute(
        email: RequestBody,
        displayName: RequestBody,
        username: RequestBody,
        bio: RequestBody?,
        image: MultipartBody.Part?,
        banner: MultipartBody.Part?,
    ): Flow<DataState<Account>> = flow {
        emit(DataState.loading())

        // Update network
        val response = service.updateAccount(
            email = email,
            displayName = displayName,
            username = username,
            bio = bio,
            image = image,
            banner = banner
        )

        // update cache
        cache.updateAccount(
            id = response.id,
            email = response.email,
            displayName = response.displayName,
            username = response.username,
            bio = response.bio,
            image = response.image,
            banner = response.banner
        )

        // Tell the UI it was successful
        emit(
            DataState.data(
                data = response.toAccount(),
                response = Response(
                    message = SUCCESS_ACCOUNT_UPDATED,
                    uiComponentType = UIComponentType.Snackbar(),
                    messageType = MessageType.Success()
                )
            )
        )
    }.catch { e ->
        emit(handleUseCaseException(e))
    }
}