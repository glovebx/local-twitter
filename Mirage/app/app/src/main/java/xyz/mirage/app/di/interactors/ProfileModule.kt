package xyz.mirage.app.di.interactors

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import xyz.mirage.app.business.datasources.cache.post.PostDao
import xyz.mirage.app.business.datasources.cache.profile.ProfileDao
import xyz.mirage.app.business.datasources.network.main.profile.ProfileService
import xyz.mirage.app.business.interactors.profile.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ProfileModule {

    @Singleton
    @Provides
    fun provideToggleFollow(
        service: ProfileService,
        cache: ProfileDao
    ): ToggleFollow {
        return ToggleFollow(
            service = service,
            cache = cache,
        )
    }

    @Singleton
    @Provides
    fun provideGetProfile(
        cache: ProfileDao
    ): GetProfile {
        return GetProfile(
            cache = cache,
        )
    }

    @Singleton
    @Provides
    fun provideSearchProfiles(
        service: ProfileService,
        cache: ProfileDao,
    ): SearchProfiles {
        return SearchProfiles(
            service = service,
            cache = cache
        )
    }

    @Singleton
    @Provides
    fun provideGetProfileLikes(
        service: ProfileService,
    ): GetProfileLikes {
        return GetProfileLikes(
            service = service,
        )
    }

    @Singleton
    @Provides
    fun provideGetProfilePosts(
        service: ProfileService,
        postDao: PostDao,
        cache: ProfileDao,
    ): GetProfilePosts {
        return GetProfilePosts(
            cache = cache,
            postDao = postDao,
            service = service,
        )
    }

    @Singleton
    @Provides
    fun provideGetProfileMedia(
        service: ProfileService,
        postDao: PostDao,
        cache: ProfileDao,
    ): GetProfileMedia {
        return GetProfileMedia(
            cache = cache,
            postDao = postDao,
            service = service,
        )
    }
}