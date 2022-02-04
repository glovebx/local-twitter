package xyz.mirage.app.interactors.post

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import xyz.mirage.app.business.datasources.cache.post.PostDao
import xyz.mirage.app.business.datasources.network.main.post.PostService
import xyz.mirage.app.business.domain.core.SuccessHandling
import xyz.mirage.app.business.interactors.post.CreatePost
import xyz.mirage.app.datasource.cache.AppDatabaseFake
import xyz.mirage.app.datasource.cache.PostDaoFake
import xyz.mirage.app.datasource.network.*
import xyz.mirage.app.business.domain.core.MessageType
import xyz.mirage.app.business.domain.core.UIComponentType
import java.net.HttpURLConnection

/**
 * 1. Create success
 * 2. Bad Request (Text must be less than 280 chars)
 * 3. Bad Request (Either file or text are needed)
 */
class CreatePostTest {

    private val appDatabase = AppDatabaseFake()
    private lateinit var mockWebServer: MockWebServer
    private lateinit var baseUrl: HttpUrl

    // system in test
    private lateinit var createPost: CreatePost

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
        createPost = CreatePost(
            service = service,
            cache = cache,
        )
    }

    @Test
    fun createSuccess() = runBlocking {
        // condition the response
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_CREATED)
                .setBody(postWithId)
        )

        // Post
        val post = mockPost

        // Confirm the post does not exist in the cache
        var cachedPost = cache.getPostById(post.id)
        assert(cachedPost == null)

        // publish the post
        val text = post.text!!.toRequestBody("text/plain".toMediaTypeOrNull())
        val multipartBody: MultipartBody.Part? = null // can be null since just a test
        val emissions = createPost.execute(
            text = text,
            file = multipartBody
        ).toList()

        // first emission should be `loading`
        assert(emissions[0].isLoading)

        // confirm it was inserted into the cache
        cachedPost = cache.getPostById(post.id)
        assert(cachedPost?.toPost() == mockPost)

        // confirm second emission is a success message
        assert(emissions[1].stateMessage?.response?.message == SuccessHandling.SUCCESS_POST_CREATED)
        assert(emissions[1].stateMessage?.response?.uiComponentType is UIComponentType.Snackbar)
        assert(emissions[1].stateMessage?.response?.messageType is MessageType.Success)

        // loading done
        assert(!emissions[1].isLoading)
    }

    @Test
    fun createFail_titleMustBeLessThan281Chars() = runBlocking {
        // condition the response
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST)
                .setBody(errorTextTooLong)
        )

        // Post
        val post = mockPost

        // Confirm the post does not exist in the cache
        var cachedPost = cache.getPostById(post.id)
        assert(cachedPost == null)

        // publish the post
        val text = post.text!!.toRequestBody("text/plain".toMediaTypeOrNull())
        val multipartBody: MultipartBody.Part? = null // can be null since just a test
        val emissions = createPost.execute(
            text = text,
            file = multipartBody
        ).toList()

        // first emission should be `loading`
        assert(emissions[0].isLoading)

        // confirm it was NOT inserted into the cache
        cachedPost = cache.getPostById(post.id)
        assert(cachedPost == null)

        // confirm second emission is an error dialog
        assert(emissions[1].stateMessage?.response?.message == "Text the length must be between 1 and 280.")
        assert(emissions[1].stateMessage?.response?.uiComponentType is UIComponentType.Dialog)
        assert(emissions[1].stateMessage?.response?.messageType is MessageType.Error)

        // loading done
        assert(!emissions[1].isLoading)
    }

    @Test
    fun createFail_eitherFileOrTextAreRequired() = runBlocking {
        // condition the response
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST)
                .setBody(eitherTextOrFileRequired)
        )

        // Post
        val post = mockPost

        // Confirm the post does not exist in the cache
        var cachedPost = cache.getPostById(post.id)
        assert(cachedPost == null)

        // publish the post
        val text = post.text!!.toRequestBody("text/plain".toMediaTypeOrNull())
        val multipartBody: MultipartBody.Part? = null // can be null since just a test
        val emissions = createPost.execute(
            text = text,
            file = multipartBody
        ).toList()

        // first emission should be `loading`
        assert(emissions[0].isLoading)

        // confirm it was NOT inserted into the cache
        cachedPost = cache.getPostById(post.id)
        assert(cachedPost == null)

        // confirm second emission is an error dialog
        assert(emissions[1].stateMessage?.response?.message == "Text text is required if no files are provided.")
        assert(emissions[1].stateMessage?.response?.uiComponentType is UIComponentType.Dialog)
        assert(emissions[1].stateMessage?.response?.messageType is MessageType.Error)

        // loading done
        assert(!emissions[1].isLoading)
    }

    @Test
    fun createFail_imageTooLarge() = runBlocking {
        // condition the response
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST)
                .setBody(fileTooBigError)
        )

        // Post
        val post = mockPost

        // Confirm the post does not exist in the cache
        var cachedPost = cache.getPostById(post.id)
        assert(cachedPost == null)

        // publish the post
        val text = post.text!!.toRequestBody("text/plain".toMediaTypeOrNull())
        val multipartBody: MultipartBody.Part? = null // can be null since just a test
        val emissions = createPost.execute(
            text = text,
            file = multipartBody
        ).toList()

        // first emission should be `loading`
        assert(emissions[0].isLoading)

        // confirm it was NOT inserted into the cache
        cachedPost = cache.getPostById(post.id)
        assert(cachedPost == null)

        // confirm second emission is an error dialog
        assert(emissions[1].stateMessage?.response?.message == "File the size must be below 4MB.")
        assert(emissions[1].stateMessage?.response?.uiComponentType is UIComponentType.Dialog)
        assert(emissions[1].stateMessage?.response?.messageType is MessageType.Error)

        // loading done
        assert(!emissions[1].isLoading)
    }

}