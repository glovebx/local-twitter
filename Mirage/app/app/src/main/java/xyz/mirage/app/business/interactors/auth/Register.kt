package xyz.mirage.app.business.interactors.auth

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import xyz.mirage.app.business.datasources.cache.account.AccountDao
import xyz.mirage.app.business.datasources.datastore.AppDataStore
import xyz.mirage.app.business.datasources.network.auth.AuthService
import xyz.mirage.app.business.datasources.network.auth.dto.RegisterDTO
import xyz.mirage.app.business.datasources.network.core.handleUseCaseException
import xyz.mirage.app.business.domain.core.DataState
import xyz.mirage.app.business.domain.core.DataStoreKeys
import xyz.mirage.app.business.domain.core.ErrorHandling
import xyz.mirage.app.business.domain.models.Account

class Register(
    private val service: AuthService,
    private val accountDao: AccountDao,
    private val appDataStoreManager: AppDataStore,
) {
    fun execute(
        email: String,
        password: String,
        displayName: String,
        username: String,
    ): Flow<DataState<Account>> = flow {
        emit(DataState.loading())
        val response = service.registerAccount(
            RegisterDTO(
                email = email,
                password = password,
                displayName = displayName,
                username = username
            )
        )

        val entity = Account(
            id = response.id,
            displayName = response.displayName,
            image = response.image,
            username = response.username,
            bio = response.bio,
            banner = response.banner,
            email = response.email,
        ).toEntity()

        val result = accountDao.insertAndReplace(entity)

        if (result < 0) {
            throw Exception(ErrorHandling.ERROR_SAVE_AUTH_TOKEN)
        }
        // save authenticated user to datastore for auto-login next time
        appDataStoreManager.setValue(DataStoreKeys.PREVIOUS_AUTH_USER, response.username)
        emit(DataState.data(data = entity.toAccount(), response = null))
    }.catch { e ->
        emit(handleUseCaseException(e))
    }
}