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
import xyz.mirage.app.business.interactors.post.GetFeed
import xyz.mirage.app.business.interactors.post.RestorePosts
import xyz.mirage.app.datasource.cache.AppDatabaseFake
import xyz.mirage.app.business.datasources.cache.post.PostDao
import xyz.mirage.app.datasource.cache.PostDaoFake
import xyz.mirage.app.business.domain.models.Post
import xyz.mirage.app.business.datasources.network.main.post.PostService
import xyz.mirage.app.datasource.network.postListResponse
import java.net.HttpURLConnection

class RestorePostsTest {

    private val appDatabase = AppDatabaseFake()
    private lateinit var mockWebServer: MockWebServer
    private lateinit var baseUrl: HttpUrl

    // system in test
    private lateinit var restorePosts: RestorePosts

    // Dependencies
    private lateinit var getFeed: GetFeed
    private lateinit var postService: PostService
    private lateinit var postDao: PostDao

    @BeforeEach
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        baseUrl = mockWebServer.url("/v1/posts/")
        postService = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(PostService::class.java)

        postDao = PostDaoFake(db = appDatabase)

        getFeed = GetFeed(
            service = postService,
            cache = postDao,
        )

        // instantiate system in test
        restorePosts = RestorePosts(
            postDao = postDao,
        )
    }

    /**
     * 1. Get some posts from the network and insert into cache
     * 2. Restore and show posts are retrieved from cache
     */
    @Test
    fun getPostsFromNetwork_restoreFromCache(): Unit = runBlocking {

        // condition the response
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(postListResponse)
        )

        // confirm the cache is empty to start
        assert(postDao.getFeed(1).isEmpty())

        // get posts from network and insert into cache
        val searchResult = getFeed.execute(1, "", true).toList()

        // confirm the cache is no longer empty
        assert(postDao.getFeed(1).isNotEmpty())

        // run use case
        val flowItems = restorePosts.execute(1).toList()

        // first emission should be `loading`
        assert(flowItems[0].isLoading)

        // Second emission should be the list of posts
        val posts = flowItems[1].data
        assert(posts?.size ?: 0 > 0)

        // confirm they are actually Post objects
        assert(value = posts?.get(index = 0) is Post)

        assert(!flowItems[1].isLoading) // loading should be false now
    }


    @AfterEach
    fun tearDown() {
        mockWebServer.shutdown()
    }
}