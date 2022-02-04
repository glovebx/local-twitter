package xyz.mirage.app.business.interactors.post

import android.app.Application
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import xyz.mirage.app.business.datasources.cache.post.PostDao
import xyz.mirage.app.business.datasources.network.main.post.PostService
import xyz.mirage.app.business.domain.core.*
import xyz.mirage.app.business.domain.models.Post
import xyz.mirage.app.presentation.core.util.ShareUtils
import java.net.HttpURLConnection

class ToggleRetweet(
    private val application: Application,
    private val service: PostService,
    private val cache: PostDao,
) {

    fun execute(
        id: String
    ): Flow<DataState<Post>> = flow {
        emit(DataState.loading())

//        // get posts from network
//        val response = service.toggleRetweet(id)
//
//        // Update cache
//        cache.updateRetweets(
//            retweeted = response.retweeted,
//            retweets = response.retweets,
//            id = response.id
//        )

        val post = cache.getPostById(id)!!.toPost()
        // Update cache
        cache.updateRetweets(
            retweeted = true,
            retweets = post.retweets + 1,
            id = id
        )

//        ShareUtils.share(application, post)

        emit(
            DataState.data(
                data = cache.getPostById(id)!!.toPost(), //response.toPost(),
                response = null
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
                            message = SuccessHandling.SUCCESS_POST_DELETED,
                            uiComponentType = UIComponentType.None(),
                            messageType = MessageType.Success()
                        ),
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