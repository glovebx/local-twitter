package xyz.mirage.app.interactors.account

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import xyz.mirage.app.business.datasources.network.main.account.AccountService
import xyz.mirage.app.business.interactors.account.GetAccount
import xyz.mirage.app.datasource.cache.AccountDaoFake
import xyz.mirage.app.datasource.cache.AppDatabaseFake
import xyz.mirage.app.datasource.network.accountResponse
import java.net.HttpURLConnection

/**
 * 1. Retrieve Account details from network and cache
 */
class AccountTest {

    private val appDatabase = AppDatabaseFake()
    private lateinit var mockWebServer: MockWebServer
    private lateinit var baseUrl: HttpUrl

    // system in test
    private lateinit var getAccount: GetAccount

    // dependencies
    private lateinit var service: AccountService
    private lateinit var cache: AccountDaoFake

    @BeforeEach
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        baseUrl = mockWebServer.url("/v1/accounts/")
        service = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(AccountService::class.java)

        cache = AccountDaoFake(appDatabase)

        // instantiate the system in test
        getAccount = GetAccount(
            service = service,
            cache = cache,
        )
    }

    @Test
    fun getAccountSuccess() = runBlocking {
        // condition the response
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(accountResponse)
        )

        // User Information
        val id = "1410270110072967168"
        val username = "sentrionic"
        val email = "sen@example.com"
        val image = "https://gravatar.com/avatar/b8abd16496a669abb69ad275a0b0103c?d=identicon"

        // confirm no Account is stored in cache
        var cachedAccount = cache.searchByUsername(email)
        assert(cachedAccount == null)

        val emissions = getAccount.execute().toList()

        // first emission should be `loading`
        assert(emissions[0].isLoading)

        // confirm Account is cached
        cachedAccount = cache.searchById(id)
        assert(cachedAccount?.email == email)
        assert(cachedAccount?.username == username)
        assert(cachedAccount?.id == id)
        assert(cachedAccount?.image == image)

        // confirm second emission is the cached Account
        assert(emissions[1].data?.id == id)
        assert(emissions[1].data?.email == email)
        assert(emissions[1].data?.username == username)

        // loading done
        assert(!emissions[1].isLoading)
    }
}
