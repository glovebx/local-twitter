package xyz.mirage.app.business.interactors.profile

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import xyz.mirage.app.business.datasources.cache.profile.ProfileDao
import xyz.mirage.app.business.datasources.network.core.handleUseCaseException
import xyz.mirage.app.business.domain.core.*
import xyz.mirage.app.business.domain.models.Profile

class GetProfile(
    private val cache: ProfileDao,
) {

    fun execute(
        username: String,
    ): Flow<DataState<Profile>> = flow {
        emit(DataState.loading())

        val profile = cache.getProfileByUsername(username)?.toProfile()

        if (profile != null) {
            emit(DataState.data(response = null, data = profile))
        } else {
            emit(
                DataState.error<Profile>(
                    response = Response(
                        message = ErrorHandling.ERROR_PROFILE_UNABLE_TO_RETRIEVE,
                        uiComponentType = UIComponentType.Dialog(),
                        messageType = MessageType.Error()
                    )
                )
            )
        }
    }.catch { e ->
        emit(handleUseCaseException(e))
    }
}