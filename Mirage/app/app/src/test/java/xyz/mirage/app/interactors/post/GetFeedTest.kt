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
import xyz.mirage.app.business.interactors.post.GetFeed
import xyz.mirage.app.datasource.cache.AppDatabaseFake
import xyz.mirage.app.datasource.cache.PostDaoFake
import xyz.mirage.app.datasource.network.postListResponse
import xyz.mirage.app.datasource.network.shortListResponse
import xyz.mirage.app.business.domain.core.MessageType
import xyz.mirage.app.business.domain.core.UIComponentType
import java.net.HttpURLConnection

class GetFeedTest {

    private val appDatabase = AppDatabaseFake()
    private lateinit var mockWebServer: MockWebServer
    private lateinit var baseUrl: HttpUrl

    // system in test
    private lateinit var getFeed: GetFeed

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
        getFeed = GetFeed(
            service = service,
            cache = cache,
        )
    }

    /**
     * 1. Are the posts retrieved from the network?
     * 2. Are the posts inserted into the cache?
     * 3. Are the posts then emitted as a flow from the cache?
     */
    @Test
    fun getPostsFromNetwork_emitPostsFromCache(): Unit = runBlocking {

        // condition the response
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(postListResponse)
        )

        // confirm the cache is empty to start
        assert(cache.getFeed(1).isEmpty())

        val flowItems = getFeed.execute(1, null, true).toList()

        // confirm the cache is no longer empty
        assert(cache.getFeed(1).isNotEmpty())

        // first emission should be `loading`
        assert(flowItems[0].isLoading)

        // Second emission should be the list of posts
        val posts = flowItems[1].data
        assert(posts?.size ?: 0 > 0)

        // confirm they are actually Post objects
        assert(posts?.get(index = 0) is Post)

        assert(!flowItems[1].isLoading) // loading should be false now
    }


    /**
     * Simulate a bad request
     */
    @Test
    fun getPostsFromNetwork_emitHttpError(): Unit = runBlocking {

        // condition the response
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST)
                .setBody("{}")
        )

        val flowItems = getFeed.execute(1, "", true).toList()

        // first emission should be `loading`
        assert(flowItems[0].isLoading)

        // Second emission should be the exception
        val error = flowItems[1].stateMessage
        assert(error != null)

        assert(!flowItems[1].isLoading) // loading should be false now
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
        var cachedPosts = cache.getFeed(1, 20)
        assert(cachedPosts.isEmpty())

        // Execute the use case
        val emissions = getFeed.execute(
            page = 1,
            cursor = "",
            true,
        ).toList()

        // first emission should be `loading`
        assert(emissions[0].isLoading)

        // confirm the posts were inserted into the cache
        cachedPosts = cache.getFeed(1, 20)
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
        val emissions = getFeed.execute(
            page = 1,
            cursor = "",
            true,
        ).toList()

        // first emission should be `loading`
        assert(emissions[0].isLoading)

        // confirm cache is still empty
        cachedPosts = cache.getFeed(1, 20)
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
        val emissions = getFeed.execute(
            page = 1,
            cursor = "",
            true,
        ).toList()

        // first emission should be `loading`
        assert(emissions[0].isLoading)

        // confirm cache is still empty
        cachedPosts = cache.getFeed(1, 20)
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
        val emissions = getFeed.execute(
            page = 1,
            cursor = "",
            true,
        ).toList()

        // first emission should be `loading`
        assert(emissions[0].isLoading)

        // confirm cache is still empty
        cachedPosts = cache.getFeed(1, 20)
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
        val emissions = getFeed.execute(
            page = 1,
            cursor = "",
            true,
        ).toList()


        // first emission should be `loading`
        assert(emissions[0].isLoading)

        // confirm cache is still empty
        cachedPosts = cache.getFeed(1, 20)
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
