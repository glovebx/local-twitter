package xyz.mirage.app.business.interactors.post

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import xyz.mirage.app.business.datasources.cache.post.PostDao
import xyz.mirage.app.business.datasources.cache.post.toPostList
import xyz.mirage.app.business.datasources.network.core.handleUseCaseException
import xyz.mirage.app.business.domain.core.DataState
import xyz.mirage.app.business.domain.models.Post

class RestorePosts(
    private val postDao: PostDao,
) {
    fun execute(
        page: Int,
    ): Flow<DataState<List<Post>>> = flow {
        emit(DataState.loading())

        // query the cache
        val cacheResult = postDao.restoreAllPosts(
            page = page
        )

        val list = cacheResult.toPostList()

        emit(DataState.data(response = null, data = list))
    }.catch { e ->
        emit(handleUseCaseException(e))
    }
}