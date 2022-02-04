package xyz.mirage.app.interactors.account

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import xyz.mirage.app.business.datasources.network.main.account.AccountService
import xyz.mirage.app.business.domain.core.ErrorHandling
import xyz.mirage.app.business.domain.core.MessageType
import xyz.mirage.app.business.domain.core.SuccessHandling
import xyz.mirage.app.business.domain.core.UIComponentType
import xyz.mirage.app.business.domain.models.Account
import xyz.mirage.app.business.interactors.account.UpdateAccount
import xyz.mirage.app.datasource.cache.AccountDaoFake
import xyz.mirage.app.datasource.cache.AppDatabaseFake
import xyz.mirage.app.datasource.network.accountResponse
import xyz.mirage.app.datasource.network.badRequestResponse
import xyz.mirage.app.datasource.network.emailInUseResponse
import xyz.mirage.app.datasource.network.usernameInUseResponse
import java.net.HttpURLConnection

/**
 * 1. Update account success
 * 2. Update account failure (email already in use)
 * 3. Update account failure (username already in use)
 * 4. Update account failure (Bad Request)
 * 5. Update account failure (Server Error)
 */
class UpdateAccountTest {

    private val appDatabase = AppDatabaseFake()
    private lateinit var mockWebServer: MockWebServer
    private lateinit var baseUrl: HttpUrl

    // system in test
    private lateinit var updateAccount: UpdateAccount

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
        updateAccount = UpdateAccount(
            service = service,
            cache = cache,
        )
    }

    @Test
    fun updateAccountSuccess() = runBlocking {
        // condition the response
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(accountResponse)
        )

        // User Information
        val id = "1410270110072967168"
        val username = "sentrionic"
        val displayName = "sentrionic"
        val email = "sen@example.com"
        val image = "https://gravatar.com/avatar/b8abd16496a669abb69ad275a0b0103c?d=identicon"

        // make sure an account already exists in the cache
        val initialEmail = "someEmail@gmail.com"
        val initialUsername = "someusername"
        val account = Account(
            id = id,
            email = initialEmail,
            username = initialUsername,
            image = image,
            displayName = displayName,
            banner = null,
            bio = null,
        )
        cache.insertAndReplace(account.toEntity())

        // confirm Account is cached
        var cachedAccount = cache.searchById(id)
        assert(cachedAccount?.email == initialEmail)
        assert(cachedAccount?.username == initialUsername)
        assert(cachedAccount?.id == id)

        val emissions = updateAccount.execute(
            email = email.toRequestBody("text/plain".toMediaTypeOrNull()),
            username = username.toRequestBody("text/plain".toMediaTypeOrNull()),
            displayName = displayName.toRequestBody("text/plain".toMediaTypeOrNull()),
            bio = null,
            banner = null,
            image = null,
        ).toList()

        // first emission should be `loading`
        assert(emissions[0].isLoading)

        // confirm Account is updated in the cache
        cachedAccount = cache.searchById(id)
        assert(cachedAccount?.email == email)
        assert(cachedAccount?.username == username)
        assert(cachedAccount?.id == id)

        // confirm second emission is a success response
        assert(emissions[1].stateMessage?.response?.message == SuccessHandling.SUCCESS_ACCOUNT_UPDATED)
        assert(emissions[1].stateMessage?.response?.uiComponentType is UIComponentType.Snackbar)
        assert(emissions[1].stateMessage?.response?.messageType is MessageType.Success)

        // loading done
        assert(!emissions[1].isLoading)
    }

    @Test
    fun updateAccountFail_emailInUse() = runBlocking {
        // condition the response
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_CONFLICT)
                .setBody(emailInUseResponse)
        )

        // User Information
        val id = "1410270110072967168"
        val username = "sentrionic"
        val displayName = "sentrionic"
        val email = "sen@example.com"
        val image = "https://gravatar.com/avatar/b8abd16496a669abb69ad275a0b0103c?d=identicon"

        // make sure an account already exists in the cache
        val initialEmail = "someEmail@gmail.com"
        val initialUsername = "someusername"
        val account = Account(
            id = id,
            email = initialEmail,
            username = initialUsername,
            image = image,
            displayName = displayName,
            banner = null,
            bio = null,
        )
        cache.insertAndReplace(account.toEntity())

        // confirm Account is cached
        var cachedAccount = cache.searchById(id)
        assert(cachedAccount?.email == initialEmail)
        assert(cachedAccount?.username == initialUsername)
        assert(cachedAccount?.id == id)

        val emissions = updateAccount.execute(
            email = email.toRequestBody("text/plain".toMediaTypeOrNull()),
            username = username.toRequestBody("text/plain".toMediaTypeOrNull()),
            displayName = displayName.toRequestBody("text/plain".toMediaTypeOrNull()),
            bio = null,
            banner = null,
            image = null,
        ).toList()

        // first emission should be `loading`
        assert(emissions[0].isLoading)

        // confirm Account is NOT updated in the cache
        cachedAccount = cache.searchById(id)
        assert(cachedAccount?.email == initialEmail)
        assert(cachedAccount?.username == initialUsername)
        assert(cachedAccount?.id == id)

        // confirm second emission is an error dialog
        assert(emissions[1].stateMessage?.response?.message == ErrorHandling.ERROR_EMAIL_IN_USE)
        assert(emissions[1].stateMessage?.response?.uiComponentType is UIComponentType.Dialog)
        assert(emissions[1].stateMessage?.response?.messageType is MessageType.Error)

        // loading done
        assert(!emissions[1].isLoading)
    }

    @Test
    fun updateAccountFail_usernameInUse() = runBlocking {
        // condition the response
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_CONFLICT)
                .setBody(usernameInUseResponse)
        )

        // User Information
        val id = "1410270110072967168"
        val username = "sentrionic"
        val displayName = "sentrionic"
        val email = "sen@example.com"
        val image = "https://gravatar.com/avatar/b8abd16496a669abb69ad275a0b0103c?d=identicon"

        // make sure an account already exists in the cache
        val initialEmail = "someEmail@gmail.com"
        val initialUsername = "someusername"
        val account = Account(
            id = id,
            email = initialEmail,
            username = initialUsername,
            image = image,
            displayName = displayName,
            banner = null,
            bio = null,
        )
        cache.insertAndReplace(account.toEntity())

        // confirm Account is cached
        var cachedAccount = cache.searchById(id)
        assert(cachedAccount?.email == initialEmail)
        assert(cachedAccount?.username == initialUsername)
        assert(cachedAccount?.id == id)

        val emissions = updateAccount.execute(
            email = email.toRequestBody("text/plain".toMediaTypeOrNull()),
            username = username.toRequestBody("text/plain".toMediaTypeOrNull()),
            displayName = displayName.toRequestBody("text/plain".toMediaTypeOrNull()),
            bio = null,
            banner = null,
            image = null,
        ).toList()

        // first emission should be `loading`
        assert(emissions[0].isLoading)

        // confirm Account is NOT updated in the cache
        cachedAccount = cache.searchById(id)
        assert(cachedAccount?.email == initialEmail)
        assert(cachedAccount?.username == initialUsername)
        assert(cachedAccount?.id == id)

        // confirm second emission is an error dialog
        assert(emissions[1].stateMessage?.response?.message == ErrorHandling.ERROR_USERNAME_IN_USE)
        assert(emissions[1].stateMessage?.response?.uiComponentType is UIComponentType.Dialog)
        assert(emissions[1].stateMessage?.response?.messageType is MessageType.Error)

        // loading done
        assert(!emissions[1].isLoading)
    }

    @Test
    fun updateAccountFail() = runBlocking {
        // condition the response
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST)
                .setBody(badRequestResponse)
        )

        // User Information
        val id = "1410270110072967168"
        val username = "sentrionic"
        val displayName = "sentrionic"
        val email = "sen@example.com"
        val image = "https://gravatar.com/avatar/b8abd16496a669abb69ad275a0b0103c?d=identicon"

        // make sure an account already exists in the cache
        val initialEmail = "someEmail@gmail.com"
        val initialUsername = "someusername"
        val account = Account(
            id = id,
            email = initialEmail,
            username = initialUsername,
            image = image,
            displayName = displayName,
            banner = null,
            bio = null,
        )
        cache.insertAndReplace(account.toEntity())

        // confirm Account is cached
        var cachedAccount = cache.searchById(id)
        assert(cachedAccount?.email == initialEmail)
        assert(cachedAccount?.username == initialUsername)
        assert(cachedAccount?.id == id)

        val emissions = updateAccount.execute(
            email = email.toRequestBody("text/plain".toMediaTypeOrNull()),
            username = username.toRequestBody("text/plain".toMediaTypeOrNull()),
            displayName = displayName.toRequestBody("text/plain".toMediaTypeOrNull()),
            bio = null,
            banner = null,
            image = null,
        ).toList()

        // first emission should be `loading`
        assert(emissions[0].isLoading)

        // confirm Account is NOT updated in the cache
        cachedAccount = cache.searchById(id)
        assert(cachedAccount?.email == initialEmail)
        assert(cachedAccount?.username == initialUsername)
        assert(cachedAccount?.id == id)

        // confirm second emission is an error dialog
        assert(emissions[1].stateMessage?.response?.message == "Username the length must be between 4 and 15.")
        assert(emissions[1].stateMessage?.response?.uiComponentType is UIComponentType.Dialog)
        assert(emissions[1].stateMessage?.response?.messageType is MessageType.Error)

        // loading done
        assert(!emissions[1].isLoading)
    }

    @Test
    fun updateAccountFail_ServerError() = runBlocking {
        // condition the response
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR)
        )

        // User Information
        val id = "1410270110072967168"
        val username = "sentrionic"
        val displayName = "sentrionic"
        val email = "sen@example.com"
        val image = "https://gravatar.com/avatar/b8abd16496a669abb69ad275a0b0103c?d=identicon"

        // make sure an account already exists in the cache
        val initialEmail = "someEmail@gmail.com"
        val initialUsername = "someusername"
        val account = Account(
            id = id,
            email = initialEmail,
            username = initialUsername,
            image = image,
            displayName = displayName,
            banner = null,
            bio = null,
        )
        cache.insertAndReplace(account.toEntity())

        // confirm Account is cached
        var cachedAccount = cache.searchById(id)
        assert(cachedAccount?.email == initialEmail)
        assert(cachedAccount?.username == initialUsername)
        assert(cachedAccount?.id == id)

        val emissions = updateAccount.execute(
            email = email.toRequestBody("text/plain".toMediaTypeOrNull()),
            username = username.toRequestBody("text/plain".toMediaTypeOrNull()),
            displayName = displayName.toRequestBody("text/plain".toMediaTypeOrNull()),
            bio = null,
            banner = null,
            image = null,
        ).toList()

        // first emission should be `loading`
        assert(emissions[0].isLoading)

        // confirm Account is NOT updated in the cache
        cachedAccount = cache.searchById(id)
        assert(cachedAccount?.email == initialEmail)
        assert(cachedAccount?.username == initialUsername)
        assert(cachedAccount?.id == id)

        // confirm second emission is an error dialog
        assert(emissions[1].stateMessage?.response?.message == ErrorHandling.SERVER_ERROR)
        assert(emissions[1].stateMessage?.response?.uiComponentType is UIComponentType.Dialog)
        assert(emissions[1].stateMessage?.response?.messageType is MessageType.Error)

        // loading done
        assert(!emissions[1].isLoading)
    }
}