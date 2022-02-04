package xyz.mirage.app.di

import android.app.Application
import com.franmontiel.persistentcookiejar.ClearableCookieJar
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import xyz.mirage.app.business.datasources.network.auth.AuthService
import xyz.mirage.app.business.datasources.network.main.account.AccountService
import xyz.mirage.app.business.datasources.network.main.post.PostService
import xyz.mirage.app.business.datasources.network.main.profile.ProfileService
import xyz.mirage.app.business.domain.core.Constants.Companion.BASE_URL
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Singleton
    @Provides
    fun provideLogging(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            setLevel(HttpLoggingInterceptor.Level.BODY)
        }
    }

    @Singleton
    @Provides
    fun provideCookieJar(application: Application): ClearableCookieJar {
        return PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(application))
    }

    @Singleton
    @Provides
    fun provideClient(
        logger: HttpLoggingInterceptor,
        cookieJar: ClearableCookieJar,
    ): OkHttpClient {
        return OkHttpClient
            .Builder()
            .cookieJar(cookieJar)
            .addInterceptor(logger)
            .build()
    }

    @Singleton
    @Provides
    fun provideMoshiBuilder(): MoshiConverterFactory {
        return MoshiConverterFactory.create()
    }

    @Singleton
    @Provides
    fun providePostService(
        moshi: MoshiConverterFactory,
        client: OkHttpClient
    ): PostService {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(moshi)
            .client(client)
            .build()
            .create(PostService::class.java)
    }

    @Singleton
    @Provides
    fun provideAuthService(
        moshi: MoshiConverterFactory,
        client: OkHttpClient
    ): AuthService {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(moshi)
            .client(client)
            .build()
            .create(AuthService::class.java)
    }

    @Singleton
    @Provides
    fun provideAccountService(
        moshi: MoshiConverterFactory,
        client: OkHttpClient
    ): AccountService {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(moshi)
            .client(client)
            .build()
            .create(AccountService::class.java)
    }

    @Singleton
    @Provides
    fun provideProfileService(
        moshi: MoshiConverterFactory,
        client: OkHttpClient
    ): ProfileService {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(moshi)
            .client(client)
            .build()
            .create(ProfileService::class.java)
    }
}