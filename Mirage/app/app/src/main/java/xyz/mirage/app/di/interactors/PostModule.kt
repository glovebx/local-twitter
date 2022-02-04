package xyz.mirage.app.di.interactors

import android.app.Application
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import xyz.mirage.app.business.datasources.cache.post.PostDao
import xyz.mirage.app.business.datasources.network.main.post.PostService
import xyz.mirage.app.business.interactors.post.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PostModule {

    @Singleton
    @Provides
    fun provideGetFeed(
        application: Application,
        postService: PostService,
        postDao: PostDao
    ): GetFeed {
        return GetFeed(
            application = application,
            service = postService,
            cache = postDao,
        )
    }

    @Singleton
    @Provides
    fun provideRestorePosts(
        postDao: PostDao
    ): RestorePosts {
        return RestorePosts(
            postDao = postDao,
        )
    }

    @Singleton
    @Provides
    fun provideGetPost(
        cache: PostDao,
        service: PostService,
    ): GetPost {
        return GetPost(
            cache = cache,
            service = service
        )
    }

    @Singleton
    @Provides
    fun provideCreatePost(
        postService: PostService,
        postDao: PostDao
    ): CreatePost {
        return CreatePost(
            service = postService,
            cache = postDao,
        )
    }

    @Singleton
    @Provides
    fun provideDeletePost(
        postService: PostService,
        postDao: PostDao
    ): DeletePost {
        return DeletePost(
            service = postService,
            cache = postDao,
        )
    }

    @Singleton
    @Provides
    fun provideSearchPosts(
        postService: PostService,
        postDao: PostDao
    ): SearchPosts {
        return SearchPosts(
            service = postService,
            cache = postDao,
        )
    }

    @Singleton
    @Provides
    fun provideToggleLike(
        postService: PostService,
        postDao: PostDao
    ): ToggleLike {
        return ToggleLike(
            service = postService,
            cache = postDao,
        )
    }

    @Singleton
    @Provides
    fun provideToggleRetweet(
        application: Application,
        postService: PostService,
        postDao: PostDao
    ): ToggleRetweet {
        return ToggleRetweet(
            application = application,
            service = postService,
            cache = postDao,
        )
    }
}