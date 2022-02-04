package xyz.mirage.app.business.interactors.profile

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import xyz.mirage.app.business.datasources.cache.profile.ProfileDao
import xyz.mirage.app.business.datasources.network.core.handleUseCaseException
import xyz.mirage.app.business.datasources.network.main.profile.ProfileService
import xyz.mirage.app.business.domain.core.DataState
import xyz.mirage.app.business.domain.models.Profile

class SearchProfiles(
    private val service: ProfileService,
    private val cache: ProfileDao,
) {

    fun execute(
        search: String,
    ): Flow<DataState<List<Profile>>> = flow {
        emit(DataState.loading())

        val profiles = service.searchProfiles(
            search = search,
        ).map { it.toProfile() }

        cache.insertProfiles(profiles = profiles.map { it.toEntity() })

        val profileCache = cache.searchProfiles(search = search).map { it.toProfile() }

        emit(DataState.data(response = null, data = profileCache))
    }.catch { e ->
        emit(handleUseCaseException(e))
    }
}