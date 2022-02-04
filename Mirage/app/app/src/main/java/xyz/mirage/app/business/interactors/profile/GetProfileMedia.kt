package xyz.mirage.app.business.interactors.profile

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import xyz.mirage.app.business.datasources.cache.post.PostDao
import xyz.mirage.app.business.datasources.cache.post.toPostList
import xyz.mirage.app.business.datasources.cache.profile.ProfileDao
import xyz.mirage.app.business.datasources.network.core.handleUseCaseException
import xyz.mirage.app.business.datasources.network.main.post.dto.toPostList
import xyz.mirage.app.business.datasources.network.main.profile.ProfileService
import xyz.mirage.app.business.domain.core.DataState
import xyz.mirage.app.business.domain.models.Post

class GetProfileMedia(
    private val cache: ProfileDao,
    private val postDao: PostDao,
    private val service: ProfileService,
) : AbstractProfilePosts() {

    override fun execute(
        cursor: String?,
        username: String,
    ): Flow<DataState<List<Post>>> = flow {
        emit(DataState.loading())

//        val posts = service.getProfileMedia(username = username, cursor = cursor).posts.toPostList()
        val profile = cache.getProfileByUsername(username)?.toProfile()
        val posts = postDao.getFeed(
            profile!!.id,
            createdAt = cursor.orEmpty()
        ).toPostList()

        emit(DataState.data(response = null, data = posts))
    }.catch { e ->
        emit(handleUseCaseException(e))
    }
}