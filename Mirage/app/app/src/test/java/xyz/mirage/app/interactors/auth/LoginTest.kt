package xyz.mirage.app.interactors.auth

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import xyz.mirage.app.business.datasources.network.auth.AuthService
import xyz.mirage.app.business.domain.core.ErrorHandling
import xyz.mirage.app.business.interactors.auth.Login
import xyz.mirage.app.datasource.cache.AccountDaoFake
import xyz.mirage.app.datasource.cache.AppDatabaseFake
import xyz.mirage.app.datasource.datastore.AppDataStoreManagerFake
import xyz.mirage.app.datasource.network.accountResponse
import xyz.mirage.app.datasource.network.invalidCredentialsResponse
import xyz.mirage.app.business.domain.core.DataStoreKeys
import xyz.mirage.app.business.domain.core.MessageType
import xyz.mirage.app.business.domain.core.UIComponentType
import java.net.HttpURLConnection

/**
 * 1. Login Success
 * 2. Login Failure (Invalid credentials)
 */
class LoginTest {

    private val appDatabase = AppDatabaseFake()
    private lateinit var mockWebServer: MockWebServer
    private lateinit var baseUrl: HttpUrl
    private lateinit var logger: HttpLoggingInterceptor
    private lateinit var client: OkHttpClient

    // system in test
    private lateinit var login: Login

    // dependencies
    private lateinit var service: AuthService
    private lateinit var accountDao: AccountDaoFake
    private lateinit var dataStore: AppDataStoreManagerFake


    @BeforeEach
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        baseUrl = mockWebServer.url("/api/accounts/login/")

        logger = HttpLoggingInterceptor().apply {
            setLevel(HttpLoggingInterceptor.Level.BODY)
        }

        client = OkHttpClient
            .Builder()
            .addInterceptor(logger)
            .build()

        service = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(MoshiConverterFactory.create())
            .client(client)
            .build()
            .create(AuthService::class.java)

        accountDao = AccountDaoFake(appDatabase)
        dataStore = AppDataStoreManagerFake()

        // instantiate the system in test
        login = Login(
            service = service,
            accountDao = accountDao,
            appDataStoreManager = dataStore
        )
    }

    @Test
    fun loginSuccess() = runBlocking {
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
        val password = "password"

        // confirm no account is stored in cache
        var cachedAccount = accountDao.searchByUsername(username)
        assert(cachedAccount == null)

        // confirm no username is stored in DataStore
        var storedUsername = dataStore.readValue(DataStoreKeys.PREVIOUS_AUTH_USER)
        assert(storedUsername == null)

        val emissions = login.execute(
            email = email,
            password = password
        ).toList()

        // first emission should be `loading`
        assert(emissions[0].isLoading)

        // confirm Account is cached
        cachedAccount = accountDao.searchByUsername(username)
        assert(cachedAccount?.username == username)
        assert(cachedAccount?.id == id)

        // confirm email is saved to DataStore
        storedUsername = dataStore.readValue(DataStoreKeys.PREVIOUS_AUTH_USER)
        assert(storedUsername == username)

        // confirm second emission is the cached AuthToken
        assert(emissions[1].data?.id == id)

        // loading done
        assert(!emissions[1].isLoading)
    }

    @Test
    fun loginFailure_invalidCredentials_Email() = runBlocking {
        // condition the response
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_UNAUTHORIZED)
                .setBody(invalidCredentialsResponse)
        )

        // User Information
        val username = "test"
        val email = "test@example.com"
        val password = "password"

        // confirm no account is stored in cache
        var cachedAccount = accountDao.searchByUsername(username)
        assert(cachedAccount == null)

        // confirm no username is stored in DataStore
        var storedUsername = dataStore.readValue(DataStoreKeys.PREVIOUS_AUTH_USER)
        assert(storedUsername == null)

        val emissions = login.execute(
            email = email,
            password = password
        ).toList()

        // first emission should be `loading`
        assert(emissions[0].isLoading)

        // confirm account is NOT cached
        cachedAccount = accountDao.searchByUsername(username)
        assert(cachedAccount == null)

        // confirm email is NOT saved to DataStore
        storedUsername = dataStore.readValue(DataStoreKeys.PREVIOUS_AUTH_USER)
        assert(storedUsername == null)

        // confirm second emission is an error dialog
        assert(emissions[1].data == null)
        assert(emissions[1].stateMessage?.response?.message == ErrorHandling.INVALID_CREDENTIALS)
        assert(emissions[1].stateMessage?.response?.uiComponentType is UIComponentType.Dialog)
        assert(emissions[1].stateMessage?.response?.messageType is MessageType.Error)

        // loading done
        assert(!emissions[1].isLoading)
    }
}
