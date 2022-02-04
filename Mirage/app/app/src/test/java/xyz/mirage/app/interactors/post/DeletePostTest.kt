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
import xyz.mirage.app.business.interactors.post.DeletePost
import xyz.mirage.app.datasource.cache.AppDatabaseFake
import xyz.mirage.app.datasource.cache.PostDaoFake
import xyz.mirage.app.datasource.network.mockPost
import xyz.mirage.app.datasource.network.notFoundResponse
import xyz.mirage.app.datasource.network.postWithId
import xyz.mirage.app.datasource.network.unauthorizedError
import xyz.mirage.app.business.domain.core.MessageType
import xyz.mirage.app.business.domain.core.UIComponentType
import java.net.HttpURLConnection


/**
 * 1. Delete success
 * 2. Delete failure (do not have permission to delete someone else's post)
 * 3. Delete failure (post does not exist on server but does exist in cache)
 */
class DeletePostTest {

    private val appDatabase = AppDatabaseFake()
    private lateinit var mockWebServer: MockWebServer
    private lateinit var baseUrl: HttpUrl

    // system in test
    private lateinit var deletePost: DeletePost

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
        deletePost = DeletePost(
            service = service,
            cache = cache,
        )
    }

    @Test
    fun deleteSuccess() = runBlocking {
        // condition the response
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(postWithId)
        )

        val post = mockPost
        // Ensure the post exists in the cache before deleting
        cache.insertPost(post = post.toEntity())
        cache.insertAuthor(post.profile.toEntity())

        // confirm it exists in the cache
        var cachedPost = cache.getPostById(post.id)
        assert(cachedPost?.toPost() == post)

        // delete the post
        val emissions = deletePost.execute(
            id = post.id
        ).toList()

        // first emission should be `loading`
        assert(emissions[0].isLoading)

        // confirm it was deleted from the cache
        cachedPost = cache.getPostById(post.id)
        assert(cachedPost == null)

        // confirm second emission is a success message
        assert(emissions[1].stateMessage?.response?.message == SuccessHandling.SUCCESS_POST_DELETED)
        assert(emissions[1].stateMessage?.response?.uiComponentType is UIComponentType.Snackbar)
        assert(emissions[1].stateMessage?.response?.messageType is MessageType.Success)

        // loading done
        assert(!emissions[1].isLoading)
    }

    @Test
    fun deleteFail_needPermission() = runBlocking {
        // condition the response
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_UNAUTHORIZED)
                .setBody(unauthorizedError)
        )

        val post = mockPost
        // Ensure the post exists in the cache before deleting
        cache.insertPost(post = post.toEntity())
        cache.insertAuthor(post.profile.toEntity())

        // confirm it exists in the cache
        var cachedPost = cache.getPostById(post.id)
        assert(cachedPost?.toPost() == post)

        // attempt to delete the post
        val emissions = deletePost.execute(
            id = post.id
        ).toList()

        // first emission should be `loading`
        assert(emissions[0].isLoading)

        // confirm it was NOT deleted from the cache
        cachedPost = cache.getPostById(post.id)
        assert(cachedPost?.toPost() == post)

        // confirm second emission is an error dialog
        assert(emissions[1].data == null)
        assert(emissions[1].stateMessage?.response?.message == ErrorHandling.UNAUTHORIZED_ERROR)
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
        val emissions = deletePost.execute(
            id = post.id
        ).toList()

        // first emission should be `loading`
        assert(emissions[0].isLoading)

        // confirm it was NOT deleted from the cache
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