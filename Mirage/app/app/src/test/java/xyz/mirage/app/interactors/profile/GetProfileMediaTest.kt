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
import xyz.mirage.app.business.datasources.network.main.profile.ProfileService
import xyz.mirage.app.business.domain.core.ErrorHandling
import xyz.mirage.app.business.domain.models.Post
import xyz.mirage.app.business.interactors.profile.GetProfileMedia
import xyz.mirage.app.datasource.network.mockAuthor
import xyz.mirage.app.datasource.network.postListResponse
import xyz.mirage.app.datasource.network.shortListResponse
import xyz.mirage.app.business.domain.core.MessageType
import xyz.mirage.app.business.domain.core.UIComponentType
import java.net.HttpURLConnection

class GetProfileMediaTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var baseUrl: HttpUrl

    // system in test
    private lateinit var getProfileMedia: GetProfileMedia

    // Dependencies
    private lateinit var service: ProfileService

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

        // instantiate the system in test
        getProfileMedia = GetProfileMedia(
            service = service,
        )
    }

    /**
     * 1. Success (Retrieve 20 posts)
     * 2. Success (Retrieve 10 posts)
     * 3. Success (Retrieve empty list)
     * 4. Failure (Unauthorized null)
     * 5. Failure (Random error)
     * 6. Failure (Malformed data)
     */

    @Test
    fun success_20Posts() = runBlocking {
        // condition the response
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(postListResponse)
        )

        val profile = mockAuthor

        // Execute the use case
        val emissions = getProfileMedia.execute(
            cursor = "",
            username = profile.username
        ).toList()

        // first emission should be `loading`
        assert(emissions[0].isLoading)

        // confirm second emission is a list of data and they are post posts
        assert(emissions[1].data?.size == 20)
        assert(emissions[1].data?.get(0) != null)
        assert(emissions[1].data?.get(0) is Post)

        // loading done
        assert(!emissions[1].isLoading)
    }

    @Test
    fun success_10Posts() = runBlocking {
        // condition the response
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(shortListResponse)
        )

        val profile = mockAuthor

        // Execute the use case
        val emissions = getProfileMedia.execute(
            cursor = "",
            username = profile.username
        ).toList()

        // first emission should be `loading`
        assert(emissions[0].isLoading)

        // confirm second emission is a list of data and they are post posts
        assert(emissions[1].data?.size == 10)
        assert(emissions[1].data?.get(0) != null)
        assert(emissions[1].data?.get(0) is Post)

        // loading done
        assert(!emissions[1].isLoading)
    }

    @Test
    fun success_emptyList() = runBlocking {
        // condition the response
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(
                    """{
                    "hasMore": false,
                    "posts": []
                }"""
                )
        )

        val profile = mockAuthor

        // Execute the use case
        val emissions = getProfileMedia.execute(
            cursor = "",
            username = profile.username
        ).toList()

        // first emission should be `loading`
        assert(emissions[0].isLoading)

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

        val profile = mockAuthor

        // Execute the use case
        val emissions = getProfileMedia.execute(
            cursor = "",
            username = profile.username
        ).toList()

        // first emission should be `loading`
        assert(emissions[0].isLoading)

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

        val profile = mockAuthor

        // Execute the use case
        val emissions = getProfileMedia.execute(
            cursor = "",
            username = profile.username
        ).toList()

        // first emission should be `loading`
        assert(emissions[0].isLoading)

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
