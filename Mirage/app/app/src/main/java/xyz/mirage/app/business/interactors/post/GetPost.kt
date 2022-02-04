package xyz.mirage.app.business.interactors.post

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import xyz.mirage.app.business.datasources.cache.post.PostDao
import xyz.mirage.app.business.datasources.network.core.handleUseCaseException
import xyz.mirage.app.business.datasources.network.main.post.PostService
import xyz.mirage.app.business.domain.core.DataState
import xyz.mirage.app.business.domain.core.ErrorHandling.Companion.ERROR_POST_UNABLE_TO_RETRIEVE
import xyz.mirage.app.business.domain.core.MessageType
import xyz.mirage.app.business.domain.core.Response
import xyz.mirage.app.business.domain.core.UIComponentType
import xyz.mirage.app.business.domain.models.Post

class GetPost(
    private val service: PostService,
    private val cache: PostDao,
) {

    fun execute(
        id: String,
        isNetworkAvailable: Boolean,
    ): Flow<DataState<Post>> = flow {
        emit(DataState.loading())

        val post = cache.getPostById(id)?.toPost()

        if (post != null) {
            emit(DataState.data(response = null, data = post))
        } else {
            if (isNetworkAvailable) {
                val response = service.get(id).toPost()
                emit(DataState.data(response = null, data = response))
            } else {
                emit(
                    DataState.error<Post>(
                        response = Response(
                            message = ERROR_POST_UNABLE_TO_RETRIEVE,
                            uiComponentType = UIComponentType.Dialog(),
                            messageType = MessageType.Error()
                        )
                    )
                )
            }
        }
    }.catch { e ->
        emit(handleUseCaseException(e))
    }
}