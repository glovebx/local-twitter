package xyz.mirage.app.interactors.post

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import xyz.mirage.app.business.datasources.cache.post.toEntity
import xyz.mirage.app.business.datasources.network.main.post.PostService
import xyz.mirage.app.business.domain.core.ErrorHandling
import xyz.mirage.app.business.interactors.post.GetPost
import xyz.mirage.app.datasource.cache.AppDatabaseFake
import xyz.mirage.app.datasource.cache.PostDaoFake
import xyz.mirage.app.datasource.network.mockPost
import xyz.mirage.app.business.domain.core.MessageType
import xyz.mirage.app.business.domain.core.UIComponentType
import xyz.mirage.app.datasource.network.notFoundResponse
import xyz.mirage.app.datasource.network.postWithId
import java.net.HttpURLConnection

/**
 * 1. Get success
 * 2. Get success (Did not exist in cache but fetched from server)
 * 3. Get failure (Does not exist in cache nor server)
 */
class GetPostTest {

    private val appDatabase = AppDatabaseFake()
    private lateinit var mockWebServer: MockWebServer
    private lateinit var baseUrl: HttpUrl

    // system in test
    private lateinit var getPost: GetPost

    // dependencies
    private lateinit var cache: PostDaoFake
    private lateinit var service: PostService

    @BeforeEach
    fun setup() {
        cache = PostDaoFake(appDatabase)
        mockWebServer = MockWebServer()
        mockWebServer.start()
        baseUrl = mockWebServer.url("/v1/posts/feed/")
        service = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(PostService::class.java)

        // instantiate the system in test
        getPost = GetPost(
            cache = cache,
            service = service
        )
    }

    @Test
    fun getPostFromCacheSuccess() = runBlocking {
        // Post
        val post = mockPost

        // Make sure the post is in the cache
        cache.insertPost(post.toEntity())
        cache.insertAuthor(post.profile.toEntity())

        // Confirm the post is in the cache
        val cachedPost = cache.getPostById(post.id)
        assert(cachedPost?.toPost() == post)

        // execute use case
        val emissions = getPost.execute(post.id, true).toList()

        // first emission should be `loading`
        assert(emissions[0].isLoading)

        // confirm second emission is the cached PostPost
        assert(emissions[1].data == post)

        // loading done
        assert(!emissions[1].isLoading)
    }

    @Test
    fun attemptGetNullPostFromCache_getPostById(): Unit = runBlocking {
        // condition the response
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(postWithId)
        )

        val post = mockPost

        // Confirm the post is not in the cache
        val cachedPost = cache.getPostById(post.id)
        assert(cachedPost == null)

        // execute use case
        val emissions = getPost.execute(post.id, true).toList()

        // first emission should be `loading`
        assert(emissions[0].isLoading)

        // confirm second emission is the cached PostPost
        assert(emissions[1].data == post)

        // loading done
        assert(!emissions[1].isLoading)
    }

    @Test
    fun getPostFail_doesNotExistInCacheNorNetwork() = runBlocking {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_NOT_FOUND)
                .setBody(notFoundResponse)
        )

        val post = mockPost

        // Confirm the post is not in the cache
        val cachedPost = cache.getPostById(post.id)
        assert(cachedPost == null)

        // execute use case
        val emissions = getPost.execute(post.id, true).toList()

        // first emission should be `loading`
        assert(emissions[0].isLoading)

        // confirm second emission is an error dialog
        assert(emissions[1].data == null)
        assert(emissions[1].stateMessage?.response?.message == ErrorHandling.ERROR_POST_UNABLE_TO_RETRIEVE)
        assert(emissions[1].stateMessage?.response?.uiComponentType is UIComponentType.Dialog)
        assert(emissions[1].stateMessage?.response?.messageType is MessageType.Error)

        // loading done
        assert(!emissions[1].isLoading)
    }
}