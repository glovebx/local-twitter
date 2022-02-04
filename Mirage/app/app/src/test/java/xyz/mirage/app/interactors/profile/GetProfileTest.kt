package xyz.mirage.app.interactors.profile

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import xyz.mirage.app.business.domain.core.ErrorHandling
import xyz.mirage.app.business.interactors.profile.GetProfile
import xyz.mirage.app.datasource.cache.AppDatabaseFake
import xyz.mirage.app.datasource.cache.ProfileDaoFake
import xyz.mirage.app.datasource.network.mockAuthor
import xyz.mirage.app.business.domain.core.MessageType
import xyz.mirage.app.business.domain.core.UIComponentType

/**
 * 1. Get success
 * 2. Get failure (Does not exist in cache)
 */
class GetProfileTest {

    private val appDatabase = AppDatabaseFake()

    // system in test
    private lateinit var getProfile: GetProfile

    // dependencies
    private lateinit var cache: ProfileDaoFake

    @BeforeEach
    fun setup() {
        cache = ProfileDaoFake(appDatabase)

        // instantiate the system in test
        getProfile = GetProfile(
            cache
        )
    }

    @Test
    fun getProfileSuccess() = runBlocking {
        // Post
        val profile = mockAuthor

        // Make sure the post is in the cache
        cache.insertProfile(profile.toEntity())

        // Confirm the profile is in the cache
        val cachedProfile = cache.getProfileByUsername(profile.username)
        assert(cachedProfile?.toProfile() == profile)

        // execute use case
        val emissions = getProfile.execute(profile.username).toList()

        // first emission should be `loading`
        assert(emissions[0].isLoading)

        // confirm second emission is the cached PostPost
        assert(emissions[1].data == profile)

        // loading done
        assert(!emissions[1].isLoading)
    }

    @Test
    fun getProfileFail_doesNotExistInCache() = runBlocking {
        val profile = mockAuthor

        // Confirm the profile is not in the cache
        val cachedProfile = cache.getProfileByUsername(profile.username)
        assert(cachedProfile == null)

        // execute use case
        val emissions = getProfile.execute(profile.username).toList()

        // first emission should be `loading`
        assert(emissions[0].isLoading)

        // confirm second emission is an error dialog
        assert(emissions[1].data == null)
        assert(emissions[1].stateMessage?.response?.message == ErrorHandling.ERROR_PROFILE_UNABLE_TO_RETRIEVE)
        assert(emissions[1].stateMessage?.response?.uiComponentType is UIComponentType.Dialog)
        assert(emissions[1].stateMessage?.response?.messageType is MessageType.Error)

        // loading done
        assert(!emissions[1].isLoading)
    }
}