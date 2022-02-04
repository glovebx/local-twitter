package xyz.mirage.app.business.interactors.post

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import xyz.mirage.app.business.datasources.cache.post.PostDao
import xyz.mirage.app.business.datasources.cache.post.toEntity
import xyz.mirage.app.business.datasources.cache.post.toPostList
import xyz.mirage.app.business.datasources.network.core.handleUseCaseException
import xyz.mirage.app.business.datasources.network.main.post.PostService
import xyz.mirage.app.business.datasources.network.main.post.dto.toPostList
import xyz.mirage.app.business.domain.core.DataState
import xyz.mirage.app.business.domain.models.Post

class SearchPosts(
    private val service: PostService,
    private val cache: PostDao,
) {

    fun execute(
        cursor: String?,
        page: Int,
        search: String,
    ): Flow<DataState<List<Post>>> = flow {
        emit(DataState.loading())

        val query = if (search.startsWith("#")) search else "#$search"

        // get posts from network
        val posts = service.searchPosts(
            search = query,
            cursor = cursor,
        ).posts.toPostList()

        // Insert into cache
        for (post in posts) {
            cache.insertPost(post.toEntity(isFeed = false))
            cache.insertAuthor(post.profile.toEntity())
        }

        // emit from cache
        val cachedPosts = cache.searchPosts(
            page = page,
            search = query
        ).toPostList()

        emit(DataState.data(response = null, data = cachedPosts))
    }.catch { e ->
        emit(handleUseCaseException(e))
    }
}
