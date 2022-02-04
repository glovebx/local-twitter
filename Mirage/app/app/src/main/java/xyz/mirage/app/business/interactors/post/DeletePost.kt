package xyz.mirage.app.business.interactors.post

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import xyz.mirage.app.business.datasources.cache.post.PostDao
import xyz.mirage.app.business.datasources.network.main.post.PostService
import xyz.mirage.app.business.domain.core.*
import xyz.mirage.app.business.domain.core.SuccessHandling.Companion.SUCCESS_POST_DELETED
import xyz.mirage.app.business.domain.models.Post
import java.net.HttpURLConnection

class DeletePost(
    private val service: PostService,
    private val cache: PostDao,
) {
    fun execute(id: String): Flow<DataState<Post>> = flow {
        emit(DataState.loading())

//        // attempt delete from network
//        val response = service.deletePost(id)

//        cache.deletePost(response.id)
        val post = cache.getPostById(id)
        cache.deletePost(id)

        // Tell the UI it was successful
        emit(
            DataState.data(
                data = post?.toPost(),
                response = Response(
                    message = SUCCESS_POST_DELETED,
                    uiComponentType = UIComponentType.Snackbar(),
                    messageType = MessageType.Success()
                )
            )
        )
    }.catch { e ->
        if (e is HttpException) {
            if (e.code() == HttpURLConnection.HTTP_NOT_FOUND) {
                cache.deletePost(id)
                // Tell the UI it was successful
                emit(
                    DataState.data(
                        data = null,
                        response = Response(
                            message = SUCCESS_POST_DELETED,
                            uiComponentType = UIComponentType.None(),
                            messageType = MessageType.Success()
                        )
                    )
                )
            } else {

                val message =
                    if (e.code() == HttpURLConnection.HTTP_UNAUTHORIZED) ErrorHandling.UNAUTHORIZED_ERROR else ErrorHandling.SERVER_ERROR

                emit(
                    DataState.error(
                        response = Response(
                            message = message,
                            uiComponentType = UIComponentType.Dialog(),
                            messageType = MessageType.Error()
                        )
                    )
                )
            }
        }
    }
}