package xyz.mirage.app.interactors.profile

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import xyz.mirage.app.business.datasources.cache.profile.ProfileDao
import xyz.mirage.app.business.datasources.network.main.profile.ProfileService
import xyz.mirage.app.business.domain.core.ErrorHandling
import xyz.mirage.app.business.domain.core.SuccessHandling
import xyz.mirage.app.business.interactors.profile.ToggleFollow
import xyz.mirage.app.datasource.cache.AppDatabaseFake
import xyz.mirage.app.datasource.cache.ProfileDaoFake
import xyz.mirage.app.datasource.network.*
import xyz.mirage.app.business.domain.core.MessageType
import xyz.mirage.app.business.domain.core.UIComponentType
import java.net.HttpURLConnection


/**
 * 1. Successful change to followed
 * 2. Successful change to unfollowed
 * 2. Failure (Server Error)
 */
class ToggleFollowTest {

    private val appDatabase = AppDatabaseFake()
    private lateinit var mockWebServer: MockWebServer
    private lateinit var baseUrl: HttpUrl

    // system in test
    private lateinit var toggleFollow: ToggleFollow

    // dependencies
    private lateinit var service: ProfileService
    private lateinit var cache: ProfileDao

    @BeforeEach
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        baseUrl = mockWebServer.url("/v1/profiles/")
        service = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(ProfileService::class.java)

        cache = ProfileDaoFake(appDatabase)

        // instantiate the system in test
        toggleFollow = ToggleFollow(
            service = service,
            cache = cache,
        )
    }

    @Test
    fun successful_follow() = runBlocking {
        // condition the response
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(followedProfile)
        )

        val profile = mockProfile

        // Insert into cache
        cache.insertProfile(profile = profile.toEntity())

        // confirm it exists in the cache
        var cachedProfile = cache.getProfileByUsername(profile.username)
        assert(cachedProfile?.toProfile() == profile)

        // toggle like
        val emissions = toggleFollow.execute(
            username = profile.username
        ).toList()
        profile.followers = profile.followers + 1
        profile.following = true

        // first emission should be `loading`
        assert(emissions[0].isLoading)

        // confirm it was deleted from the cache
        cachedProfile = cache.getProfileByUsername(profile.username)
        assert(cachedProfile?.toProfile() == profile)

        // confirm second emission is a success message
        assert(emissions[1].stateMessage?.response?.message == SuccessHandling.SUCCESS_FOLLOW_TOGGLED)
        assert(emissions[1].stateMessage?.response?.uiComponentType is UIComponentType.None)
        assert(emissions[1].stateMessage?.response?.messageType is MessageType.Success)

        // loading done
        assert(!emissions[1].isLoading)
    }

    @Test
    fun successful_unfollow() = runBlocking {
        // condition the response
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(profileResponse)
        )

        val profile = mockProfile
        profile.followers = profile.followers + 1
        profile.following = true

        // Insert into cache
        cache.insertProfile(profile = profile.toEntity())

        // confirm it exists in the cache
        var cachedProfile = cache.getProfileByUsername(profile.username)
        assert(cachedProfile?.toProfile() == profile)

        // toggle like
        val emissions = toggleFollow.execute(
            username = profile.username
        ).toList()

        // first emission should be `loading`
        assert(emissions[0].isLoading)

        profile.followers = 0
        profile.following = false

        // confirm it was deleted from the cache
        cachedProfile = cache.getProfileByUsername(profile.username)
        assert(cachedProfile?.toProfile() == profile)

        // confirm second emission is a success message
        assert(emissions[1].stateMessage?.response?.message == SuccessHandling.SUCCESS_FOLLOW_TOGGLED)
        assert(emissions[1].stateMessage?.response?.uiComponentType is UIComponentType.None)
        assert(emissions[1].stateMessage?.response?.messageType is MessageType.Success)

        // loading done
        assert(!emissions[1].isLoading)
    }

    @Test
    fun failure_ServerError() = runBlocking {
        // condition the response
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR)
        )

        val profile = mockProfile

        // Insert into cache
        cache.insertProfile(profile = profile.toEntity())

        // confirm it exists in the cache
        var cachedProfile = cache.getProfileByUsername(profile.username)
        assert(cachedProfile?.toProfile() == profile)

        // toggle like
        val emissions = toggleFollow.execute(
            username = profile.username
        ).toList()

        // first emission should be `loading`
        assert(emissions[0].isLoading)

        // confirm the profile did not update
        cachedProfile = cache.getProfileByUsername(profile.username)
        assert(cachedProfile?.toProfile() == profile)

        // confirm second emission is a success message
        assert(emissions[1].stateMessage?.response?.message == ErrorHandling.SERVER_ERROR)
        assert(emissions[1].stateMessage?.response?.uiComponentType is UIComponentType.Dialog)
        assert(emissions[1].stateMessage?.response?.messageType is MessageType.Error)

        // loading done
        assert(!emissions[1].isLoading)
    }

}