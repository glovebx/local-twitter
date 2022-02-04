package xyz.mirage.app.interactors.post

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
import xyz.mirage.app.business.datasources.cache.post.PostDao
import xyz.mirage.app.business.datasources.network.main.post.PostService
import xyz.mirage.app.business.domain.core.ErrorHandling
import xyz.mirage.app.business.domain.models.Post
import xyz.mirage.app.business.interactors.post.SearchPosts
import xyz.mirage.app.datasource.cache.AppDatabaseFake
import xyz.mirage.app.datasource.cache.PostDaoFake
import xyz.mirage.app.datasource.network.postListResponse
import xyz.mirage.app.datasource.network.shortListResponse
import xyz.mirage.app.business.domain.core.MessageType
import xyz.mirage.app.business.domain.core.UIComponentType
import java.net.HttpURLConnection

class SearchPostsTest {

    private val appDatabase = AppDatabaseFake()
    private lateinit var mockWebServer: MockWebServer
    private lateinit var baseUrl: HttpUrl

    // system in test
    private lateinit var searchPosts: SearchPosts

    // Dependencies
    private lateinit var service: PostService
    private lateinit var cache: PostDao

    @BeforeEach
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        baseUrl = mockWebServer.url("/v1/posts/feed/")
        service = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(PostService::class.java)

        cache = PostDaoFake(db = appDatabase)

        // instantiate the system in test
        searchPosts = SearchPosts(
            service = service,
            cache = cache,
        )
    }

    @Test
    fun mockWebServerSetup() {
        // condition the response
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(postListResponse)
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

        // Confirm there are no posts in the cache
        var cachedPosts = cache.searchPosts("", 1, 20)
        assert(cachedPosts.isEmpty())

        // Execute the use case
        val emissions = searchPosts.execute(
            page = 1,
            cursor = "",
            search = "",
        ).toList()

        // first emission should be `loading`
        assert(emissions[0].isLoading)

        // confirm the posts were inserted into the cache
        cachedPosts = cache.searchPosts("", 1, 20)
        assert(cachedPosts.size == 20)

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

        // Confirm there are no posts in the cache
        var cachedPosts = cache.searchPosts("", 1, 10)
        assert(cachedPosts.isEmpty())

        // Execute the use case
        val emissions = searchPosts.execute(
            page = 1,
            cursor = "",
            search = "",
        ).toList()

        // first emission should be `loading`
        assert(emissions[0].isLoading)

        // confirm the posts were inserted into the cache
        cachedPosts = cache.searchPosts("", 1, 20)
        assert(cachedPosts.size == 10)

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

        // Confirm there are no posts in the cache
        var cachedPosts = cache.getFeed(1, 20)
        assert(cachedPosts.isEmpty())

        // Execute the use case
        val emissions = searchPosts.execute(
            page = 1,
            cursor = "",
            search = "",
        ).toList()

        // first emission should be `loading`
        assert(emissions[0].isLoading)

        // confirm cache is still empty
        cachedPosts = cache.searchPosts("", 1, 20)
        assert(cachedPosts.isEmpty())

        // confirm second emission is empty list
        assert(emissions[1].data?.size == 0)

        // loading done
        assert(!emissions[1].isLoading)
    }

    @Test
    fun failure_unauthorized() = runBlocking {

        // condition the response
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_UNAUTHORIZED)
                .setBody("""{"error": {}}""")
        )

        // Confirm there are no posts in the cache
        var cachedPosts = cache.getFeed(1, 20)
        assert(cachedPosts.isEmpty())

        // Execute the use case
        val emissions = searchPosts.execute(
            page = 1,
            cursor = "",
            search = "",
        ).toList()

        // first emission should be `loading`
        assert(emissions[0].isLoading)

        // confirm cache is still empty
        cachedPosts = cache.searchPosts("", 1, 20)
        assert(cachedPosts.isEmpty())

        // confirm second emission is a error dialog
        assert(emissions[1].stateMessage?.response?.message == ErrorHandling.UNAUTHORIZED_ERROR)
        assert(emissions[1].stateMessage?.response?.uiComponentType is UIComponentType.Dialog)
        assert(emissions[1].stateMessage?.response?.messageType is MessageType.Error)

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

        // Confirm there are no posts in the cache
        var cachedPosts = cache.getFeed(1, 20)
        assert(cachedPosts.isEmpty())

        // Execute the use case
        val emissions = searchPosts.execute(
            page = 1,
            cursor = "",
            search = "",
        ).toList()

        // first emission should be `loading`
        assert(emissions[0].isLoading)

        // confirm cache is still empty
        cachedPosts = cache.searchPosts("", 1, 20)
        assert(cachedPosts.isEmpty())

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

        // Confirm there are no posts in the cache
        var cachedPosts = cache.getFeed(1, 20)
        assert(cachedPosts.isEmpty())

        // Execute the use case
        val emissions = searchPosts.execute(
            page = 1,
            cursor = "",
            search = "",
        ).toList()


        // first emission should be `loading`
        assert(emissions[0].isLoading)

        // confirm cache is still empty
        cachedPosts = cache.searchPosts("", 1, 20)
        assert(cachedPosts.isEmpty())

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
