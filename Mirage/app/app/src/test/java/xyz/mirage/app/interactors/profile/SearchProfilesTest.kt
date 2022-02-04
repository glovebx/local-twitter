package xyz.mirage.app.interactors.profile

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import xyz.mirage.app.business.datasources.cache.profile.ProfileDao
import xyz.mirage.app.business.datasources.network.main.profile.ProfileService
import xyz.mirage.app.business.domain.core.ErrorHandling
import xyz.mirage.app.business.domain.models.Profile
import xyz.mirage.app.business.interactors.profile.SearchProfiles
import xyz.mirage.app.datasource.cache.AppDatabaseFake
import xyz.mirage.app.datasource.cache.ProfileDaoFake
import xyz.mirage.app.datasource.network.profileListResponse
import xyz.mirage.app.business.domain.core.MessageType
import xyz.mirage.app.business.domain.core.UIComponentType
import java.net.HttpURLConnection

class SearchProfilesTest {

    private val appDatabase = AppDatabaseFake()
    private lateinit var mockWebServer: MockWebServer
    private lateinit var baseUrl: HttpUrl

    // system in test
    private lateinit var searchProfiles: SearchProfiles

    // Dependencies
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

        cache = ProfileDaoFake(db = appDatabase)

        // instantiate the system in test
        searchProfiles = SearchProfiles(
            service = service,
            cache = cache,
        )
    }

    /**
     * 1. Success (Retrieve 20 profiles)
     * 2. Success (Retrieve 10 profiles)
     * 3. Success (Retrieve empty list)
     * 5. Failure (Server error)
     * 6. Failure (Malformed data)
     */

    @Test
    fun success_10Profiles() = runBlocking {
        // condition the response
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(profileListResponse)
        )

        // Confirm there are no profiles in the cache
        var cachedProfiles = cache.searchProfiles("")
        assert(cachedProfiles.isEmpty())

        // Execute the use case
        val emissions = searchProfiles.execute(
            search = "",
        ).toList()

        // first emission should be `loading`
        assert(emissions[0].isLoading)

        // confirm the profiles were inserted into the cache
        cachedProfiles = cache.searchProfiles("")
        assert(cachedProfiles.size == 10)

        // confirm second emission is a list of data and they are post profiles
        assert(emissions[1].data?.size == 10)
        assert(emissions[1].data?.get(0) != null)
        assert(emissions[1].data?.get(0) is Profile)

        // loading done
        assert(!emissions[1].isLoading)
    }

    @Test
    fun success_emptyList() = runBlocking {
        // condition the response
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody("[]")
        )

        // Confirm there are no profiles in the cache
        var cachedProfiles = cache.searchProfiles("")
        assert(cachedProfiles.isEmpty())

        // Execute the use case
        val emissions = searchProfiles.execute(
            search = "",
        ).toList()

        // first emission should be `loading`
        assert(emissions[0].isLoading)

        // confirm the profiles were inserted into the cache
        cachedProfiles = cache.searchProfiles("")
        assert(cachedProfiles.isEmpty())

        // confirm second emission is empty list
        assert(emissions[1].data?.size == 0)

        // loading done
        assert(!emissions[1].isLoading)
    }

    @Test
    fun failure_serverError() = runBlocking {
        // condition the response
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR)
        )

        // Confirm there are no profiles in the cache
        var cachedProfiles = cache.searchProfiles("")
        assert(cachedProfiles.isEmpty())

        // Execute the use case
        val emissions = searchProfiles.execute(
            search = "",
        ).toList()

        // first emission should be `loading`
        assert(emissions[0].isLoading)

        // Confirm there are no profiles in the cache
        cachedProfiles = cache.searchProfiles("")
        assert(cachedProfiles.isEmpty())

        // confirm second emission is a error dialog
        assert(emissions[1].stateMessage?.response?.message == ErrorHandling.SERVER_ERROR)
        assert(emissions[1].stateMessage?.response?.uiComponentType is UIComponentType.Dialog)
        assert(emissions[1].stateMessage?.response?.messageType is MessageType.Error)

        // loading done
        assert(!emissions[1].isLoading)
    }

    @Test
    fun failure_malformedData() = runBlocking {
        // condition the response
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody("{}")
        )

        // Confirm there are no profiles in the cache
        var cachedProfiles = cache.searchProfiles("")
        assert(cachedProfiles.isEmpty())

        // Execute the use case
        val emissions = searchProfiles.execute(
            search = "",
        ).toList()

        // first emission should be `loading`
        assert(emissions[0].isLoading)

        // Confirm there are no profiles in the cache
        cachedProfiles = cache.searchProfiles("")
        assert(cachedProfiles.isEmpty())

        // confirm second emission is a error dialog
        // don't care what the error is, just show it
        assert(emissions[1].stateMessage?.response?.message != null)
        assert(emissions[1].stateMessage?.response?.uiComponentType is UIComponentType.Dialog)
        assert(emissions[1].stateMessage?.response?.messageType is MessageType.Error)

        // loading done
        assert(!emissions[1].isLoading)
    }


    @AfterEach
    fun tearDown() {
        mockWebServer.shutdown()
    }
}
