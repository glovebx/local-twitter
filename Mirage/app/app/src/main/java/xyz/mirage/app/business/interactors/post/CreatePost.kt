package xyz.mirage.app.business.interactors.post

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import okhttp3.MultipartBody
import okhttp3.RequestBody
import xyz.mirage.app.business.datasources.cache.post.PostDao
import xyz.mirage.app.business.datasources.cache.post.toEntity
import xyz.mirage.app.business.datasources.network.core.handleUseCaseException
import xyz.mirage.app.business.datasources.network.main.post.PostService
import xyz.mirage.app.business.domain.core.*
import xyz.mirage.app.business.domain.models.Post

class CreatePost(
    private val service: PostService,
    private val cache: PostDao,
) {
    fun execute(
        text: RequestBody?,
        file: MultipartBody.Part?,
    ): Flow<DataState<Post>> = flow {
        emit(DataState.loading())

        val response = service.createPost(text, file)

        val post = response.toPost()

        // insert the new post into the cache
        cache.insertPost(post.toEntity())
        cache.insertAuthor(post.profile.toEntity())

        // Tell the UI it was successful
        emit(
            DataState.data(
                data = post,
                response = Response(
                    message = SuccessHandling.SUCCESS_POST_CREATED,
                    uiComponentType = UIComponentType.Snackbar(),
                    messageType = MessageType.Success()
                ),
            )
        )
    }.catch { e ->
        emit(handleUseCaseException(e))
    }
}
