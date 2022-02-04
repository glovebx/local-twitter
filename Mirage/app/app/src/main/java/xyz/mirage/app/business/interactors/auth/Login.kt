package xyz.mirage.app.business.interactors.auth

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import xyz.mirage.app.business.datasources.cache.account.AccountDao
import xyz.mirage.app.business.datasources.datastore.AppDataStore
import xyz.mirage.app.business.datasources.network.auth.AuthService
import xyz.mirage.app.business.datasources.network.auth.dto.LoginDTO
import xyz.mirage.app.business.domain.core.*
import xyz.mirage.app.business.domain.core.ErrorHandling.Companion.ERROR_SAVE_AUTH_TOKEN
import xyz.mirage.app.business.domain.models.Account
import java.net.HttpURLConnection

class Login(
    private val service: AuthService,
    private val accountDao: AccountDao,
    private val appDataStoreManager: AppDataStore,
) {
    fun execute(
        email: String,
        password: String,
    ): Flow<DataState<Account>> = flow {
        emit(DataState.loading())
//        val response = service.loginAccount(LoginDTO(email, password))

//        val entity = Account(
//            id = response.id,
//            displayName = response.displayName,
//            image = response.image,
//            username = response.username,
//            bio = response.bio,
//            banner = response.banner,
//            email = response.email,
//        ).toEntity()

        val entity = Account(
            id = "1000",
            displayName = "test",
            image = "https://xcimg.szwego.com/20210331/i1617184109_5641_0.jpg?imageMogr2/auto-orient/thumbnail/!310x310r/quality/100/format/jpg",
            username = "test",
            bio = "tester",
            banner = "",
            email = "test@test.com",
        ).toEntity()

        val result = accountDao.insertAndReplace(entity)

        if (result < 0) {
            throw Exception(ERROR_SAVE_AUTH_TOKEN)
        }
        // save authenticated user to datastore for auto-login next time
//        appDataStoreManager.setValue(DataStoreKeys.PREVIOUS_AUTH_USER, response.username)
        appDataStoreManager.setValue(DataStoreKeys.PREVIOUS_AUTH_USER, "test")
        emit(DataState.data(data = entity.toAccount(), response = null))
    }.catch { e ->

        var message = ErrorHandling.GENERIC_AUTH_ERROR

        if (e is HttpException) {

            message = when (e.code()) {
                HttpURLConnection.HTTP_UNAUTHORIZED -> {
                    ErrorHandling.INVALID_CREDENTIALS
                }
                else -> {
                    ErrorHandling.SERVER_ERROR
                }
            }
        }

        emit(
            DataState.error(
                response = Response(
                    message = message,
                    uiComponentType = UIComponentType.Dialog(),
                    messageType = MessageType.Error()
                )
            )
        )
    }
}