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
import xyz.mirage.app.business.datasources.cache.post.PostDao
import xyz.mirage.app.business.datasources.cache.post.toEntity
import xyz.mirage.app.business.datasources.network.main.post.PostService
import xyz.mirage.app.business.domain.core.ErrorHandling
import xyz.mirage.app.business.domain.core.SuccessHandling
import xyz.mirage.app.business.interactors.post.ToggleRetweet
import xyz.mirage.app.datasource.cache.AppDatabaseFake
import xyz.mirage.app.datasource.cache.PostDaoFake
import xyz.mirage.app.datasource.network.*
import xyz.mirage.app.business.domain.core.MessageType
import xyz.mirage.app.business.domain.core.UIComponentType
import java.net.HttpURLConnection


/**
 * 1. Successful change to retweeted
 * 2. Successful removed retweet
 * 2. Failure (Server Error)
 * 4. Failure (post does not exist on server but does exist in cache)
 */
class ToggleRetweetTest {

    private val appDatabase = AppDatabaseFake()
    private lateinit var mockWebServer: MockWebServer
    private lateinit var baseUrl: HttpUrl

    // system in test
    private lateinit var toggleRetweet: ToggleRetweet

    // dependencies
    private lateinit var service: PostService
    private lateinit var cache: PostDao

    @BeforeEach
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        baseUrl = mockWebServer.url("/v1/posts/")
        service = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(PostService::class.java)

        cache = PostDaoFake(appDatabase)

        // instantiate the system in test
        toggleRetweet = ToggleRetweet(
            service = service,
            cache = cache,
        )
    }

    @Test
    fun successful_retweet() = runBlocking {
        // condition the response
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(postRetweeted)
        )

        val post = mockPost

        // Insert into cache
        cache.insertPost(post = post.toEntity())
        cache.insertAuthor(post.profile.toEntity())

        // confirm it exists in the cache
        var cachedPost = cache.getPostById(post.id)
        assert(cachedPost?.toPost() == post)

        // toggle retweet
        val emissions = toggleRetweet.execute(
            id = post.id
        ).toList()
        post.retweets = post.retweets + 1
        post.retweeted = true

        // first emission should be `loading`
        assert(emissions[0].isLoading)

        // confirm it was deleted from the cache
        cachedPost = cache.getPostById(post.id)
        assert(cachedPost?.toPost() == post)

        // confirm second emission is a success message
        assert(emissions[1].data == post)

        // loading done
        assert(!emissions[1].isLoading)
    }

    @Test
    fun successful_removeRetweet() = runBlocking {
        // condition the response
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(postWithId)
        )

        val post = mockPost
        post.retweets = post.retweets + 1
        post.retweeted = true

        // Insert into cache
        cache.insertPost(post = post.toEntity())
        cache.insertAuthor(post.profile.toEntity())

        // confirm it exists in the cache
        var cachedPost = cache.getPostById(post.id)
        assert(cachedPost?.toPost() == post)

        // toggle retweet
        val emissions = toggleRetweet.execute(
            id = post.id
        ).toList()

        // first emission should be `loading`
        assert(emissions[0].isLoading)

        post.retweets = 0
        post.retweeted = false

        // confirm it was deleted from the cache
        cachedPost = cache.getPostById(post.id)
        assert(cachedPost?.toPost() == post)

        // confirm second emission is a success message
        assert(emissions[1].data == post)

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

        val post = mockPost

        // Insert into cache
        cache.insertPost(post = post.toEntity())
        cache.insertAuthor(post.profile.toEntity())

        // confirm it exists in the cache
        var cachedPost = cache.getPostById(post.id)
        assert(cachedPost?.toPost() == post)

        // toggle retweet
        val emissions = toggleRetweet.execute(
            id = post.id
        ).toList()

        // first emission should be `loading`
        assert(emissions[0].isLoading)

        // confirm the post did not update
        cachedPost = cache.getPostById(post.id)
        assert(cachedPost?.toPost() == post)

        // confirm second emission is a success message
        assert(emissions[1].stateMessage?.response?.message == ErrorHandling.SERVER_ERROR)
        assert(emissions[1].stateMessage?.response?.uiComponentType is UIComponentType.Dialog)
        assert(emissions[1].stateMessage?.response?.messageType is MessageType.Error)

        // loading done
        assert(!emissions[1].isLoading)
    }

    /**
     * Post exists in cache but does not exist on server.
     * We need to delete from cache.
     */
    @Test
    fun deleteFail_postDoesNotExist() = runBlocking {
        // condition the response
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_NOT_FOUND)
                .setBody(notFoundResponse)
        )

        val post = mockPost
        // Ensure the post exists in the cache before deleting
        cache.insertPost(post = post.toEntity())
        cache.insertAuthor(post.profile.toEntity())

        // confirm it exists in the cache
        var cachedPost = cache.getPostById(post.id)
        assert(cachedPost?.toPost() == post)

        // attempt to delete the post
        val emissions = toggleRetweet.execute(
            id = post.id
        ).toList()

        // first emission should be `loading`
        assert(emissions[0].isLoading)

        // confirm it was deleted from the cache
        cachedPost = cache.getPostById(post.id)
        assert(cachedPost == null)

        // confirm second emission is an error dialog
        assert(emissions[1].stateMessage?.response?.message == SuccessHandling.SUCCESS_POST_DELETED)
        assert(emissions[1].stateMessage?.response?.uiComponentType is UIComponentType.None)
        assert(emissions[1].stateMessage?.response?.messageType is MessageType.Success)

        // loading done
        assert(!emissions[1].isLoading)
    }
}