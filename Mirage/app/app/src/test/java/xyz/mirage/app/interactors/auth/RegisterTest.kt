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
import xyz.mirage.app.business.interactors.auth.Register
import xyz.mirage.app.datasource.cache.AccountDaoFake
import xyz.mirage.app.datasource.cache.AppDatabaseFake
import xyz.mirage.app.datasource.datastore.AppDataStoreManagerFake
import xyz.mirage.app.datasource.network.accountResponse
import xyz.mirage.app.datasource.network.emailInUseResponse
import xyz.mirage.app.datasource.network.missingFieldResponse
import xyz.mirage.app.business.domain.core.DataStoreKeys
import xyz.mirage.app.business.domain.core.MessageType
import xyz.mirage.app.business.domain.core.UIComponentType
import xyz.mirage.app.datasource.network.usernameInUseResponse
import java.net.HttpURLConnection

/**
 * 1. Register Success
 * 2. Register Failure (Email already in use)
 * 3. Register Failure (Username already in use)
 * 4. Register Failure (Passwords must match)
 * 5. Register Failure (Missing Email)
 * 5. Register Failure (Missing Username)
 * 5. Register Failure (Missing password)
 * 5. Register Failure (Missing password2)
 */
class RegisterTest {

    private val appDatabase = AppDatabaseFake()
    private lateinit var mockWebServer: MockWebServer
    private lateinit var baseUrl: HttpUrl
    private lateinit var logger: HttpLoggingInterceptor
    private lateinit var client: OkHttpClient

    // system in test
    private lateinit var register: Register

    // dependencies
    private lateinit var service: AuthService
    private lateinit var accountDao: AccountDaoFake
    private lateinit var dataStore: AppDataStoreManagerFake

    @BeforeEach
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        baseUrl = mockWebServer.url("/api/accounts/register/")

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
        register = Register(
            service = service,
            accountDao = accountDao,
            appDataStoreManager = dataStore
        )
    }

    @Test
    fun registerSuccess() = runBlocking {
        // condition the response
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_CREATED)
                .setBody(accountResponse)
        )

        // User Information
        val id = "1410270110072967168"
        val username = "sentrionic"
        val displayName = "Sense"
        val email = "sen@example.com"
        val password = "password"

        // confirm no account is stored in cache
        var cachedAccount = accountDao.searchByUsername(username)
        assert(cachedAccount == null)

        // confirm no username is stored in DataStore
        var storedUsername = dataStore.readValue(DataStoreKeys.PREVIOUS_AUTH_USER)
        assert(storedUsername == null)

        val emissions = register.execute(
            email = email,
            displayName = displayName,
            username = username,
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
    fun registerFail_emailInUse() = runBlocking {
        // condition the response
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_CONFLICT)
                .setBody(emailInUseResponse)
        )

        // User Information
        val username = "sentrionic"
        val displayName = "Sense"
        val email = "sen@example.com"
        val password = "password"

        // confirm no account is stored in cache
        var cachedAccount = accountDao.searchByUsername(username)
        assert(cachedAccount == null)

        // confirm no username is stored in DataStore
        var storedUsername = dataStore.readValue(DataStoreKeys.PREVIOUS_AUTH_USER)
        assert(storedUsername == null)

        val emissions = register.execute(
            email = email,
            displayName = displayName,
            username = username,
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
        assert(emissions[1].stateMessage?.response?.message == ErrorHandling.ERROR_EMAIL_IN_USE)
        assert(emissions[1].stateMessage?.response?.uiComponentType is UIComponentType.Dialog)
        assert(emissions[1].stateMessage?.response?.messageType is MessageType.Error)

        // loading done
        assert(!emissions[1].isLoading)
    }

    @Test
    fun registerFail_usernameInUse() = runBlocking {
        // condition the response
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_CONFLICT)
                .setBody(usernameInUseResponse)
        )

        // User Information
        val username = "sentrionic"
        val displayName = "Sense"
        val email = "sen@example.com"
        val password = "password"

        // confirm no account is stored in cache
        var cachedAccount = accountDao.searchByUsername(username)
        assert(cachedAccount == null)

        // confirm no username is stored in DataStore
        var storedUsername = dataStore.readValue(DataStoreKeys.PREVIOUS_AUTH_USER)
        assert(storedUsername == null)

        val emissions = register.execute(
            email = email,
            displayName = displayName,
            username = username,
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
        assert(emissions[1].stateMessage?.response?.message == ErrorHandling.ERROR_USERNAME_IN_USE)
        assert(emissions[1].stateMessage?.response?.uiComponentType is UIComponentType.Dialog)
        assert(emissions[1].stateMessage?.response?.messageType is MessageType.Error)

        // loading done
        assert(!emissions[1].isLoading)
    }

    @Test
    fun registerFail_emailMissing() = runBlocking {
        // condition the response
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST)
                .setBody(missingFieldResponse("email"))
        )

        // User Information
        val username = "sentrionic"
        val displayName = "Sense"
        val email = "sen@example.com"
        val password = "password"

        // confirm no account is stored in cache
        var cachedAccount = accountDao.searchByUsername(username)
        assert(cachedAccount == null)

        // confirm no username is stored in DataStore
        var storedUsername = dataStore.readValue(DataStoreKeys.PREVIOUS_AUTH_USER)
        assert(storedUsername == null)

        val emissions = register.execute(
            email = email,
            displayName = displayName,
            username = username,
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
        assert(emissions[1].stateMessage?.response?.message == ErrorHandling.ERROR_EMAIL_BLANK_FIELD)
        assert(emissions[1].stateMessage?.response?.uiComponentType is UIComponentType.Dialog)
        assert(emissions[1].stateMessage?.response?.messageType is MessageType.Error)

        // loading done
        assert(!emissions[1].isLoading)
    }

    @Test
    fun registerFail_usernameMissing() = runBlocking {
        // condition the response
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST)
                .setBody(missingFieldResponse("username"))
        )

        // User Information
        val username = "sentrionic"
        val displayName = "Sense"
        val email = "sen@example.com"
        val password = "password"

        // confirm no account is stored in cache
        var cachedAccount = accountDao.searchByUsername(username)
        assert(cachedAccount == null)

        // confirm no username is stored in DataStore
        var storedUsername = dataStore.readValue(DataStoreKeys.PREVIOUS_AUTH_USER)
        assert(storedUsername == null)

        val emissions = register.execute(
            email = email,
            displayName = displayName,
            username = username,
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
        assert(emissions[1].stateMessage?.response?.message == ErrorHandling.ERROR_USERNAME_BLANK_FIELD)
        assert(emissions[1].stateMessage?.response?.uiComponentType is UIComponentType.Dialog)
        assert(emissions[1].stateMessage?.response?.messageType is MessageType.Error)

        // loading done
        assert(!emissions[1].isLoading)
    }

    @Test
    fun registerFail_passwordMissing() = runBlocking {
        // condition the response
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST)
                .setBody(missingFieldResponse("password"))
        )

        // User Information
        val username = "sentrionic"
        val displayName = "Sense"
        val email = "sen@example.com"
        val password = "password"

        // confirm no account is stored in cache
        var cachedAccount = accountDao.searchByUsername(username)
        assert(cachedAccount == null)

        // confirm no username is stored in DataStore
        var storedUsername = dataStore.readValue(DataStoreKeys.PREVIOUS_AUTH_USER)
        assert(storedUsername == null)

        val emissions = register.execute(
            email = email,
            displayName = displayName,
            username = username,
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
        assert(emissions[1].stateMessage?.response?.message == ErrorHandling.ERROR_PASSWORD_BLANK_FIELD)
        assert(emissions[1].stateMessage?.response?.uiComponentType is UIComponentType.Dialog)
        assert(emissions[1].stateMessage?.response?.messageType is MessageType.Error)

        // loading done
        assert(!emissions[1].isLoading)
    }

    @Test
    fun registerFail_displayNameMissing() = runBlocking {
        // condition the response
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST)
                .setBody(missingFieldResponse("displayName"))
        )

        // User Information
        val username = "sentrionic"
        val displayName = "Sense"
        val email = "sen@example.com"
        val password = "password"

        // confirm no account is stored in cache
        var cachedAccount = accountDao.searchByUsername(username)
        assert(cachedAccount == null)

        // confirm no username is stored in DataStore
        var storedUsername = dataStore.readValue(DataStoreKeys.PREVIOUS_AUTH_USER)
        assert(storedUsername == null)

        val emissions = register.execute(
            email = email,
            displayName = displayName,
            username = username,
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
        assert(emissions[1].stateMessage?.response?.message == ErrorHandling.ERROR_DISPLAYNAME_BLANK_FIELD)
        assert(emissions[1].stateMessage?.response?.uiComponentType is UIComponentType.Dialog)
        assert(emissions[1].stateMessage?.response?.messageType is MessageType.Error)

        // loading done
        assert(!emissions[1].isLoading)
    }
}
