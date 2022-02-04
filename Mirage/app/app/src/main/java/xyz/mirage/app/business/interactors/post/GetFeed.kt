package xyz.mirage.app.business.interactors.post

import java.io.File
import android.app.Application
import androidx.core.content.FileProvider
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import xyz.mirage.app.BuildConfig
import xyz.mirage.app.business.datasources.cache.post.PostDao
import xyz.mirage.app.business.datasources.cache.post.toEntity
import xyz.mirage.app.business.datasources.cache.post.toPostList
import xyz.mirage.app.business.datasources.network.core.handleUseCaseException
import xyz.mirage.app.business.datasources.network.main.post.PostService
import xyz.mirage.app.business.datasources.network.main.post.dto.PostDto
import xyz.mirage.app.business.datasources.network.main.post.dto.toPostList
import xyz.mirage.app.business.domain.core.DataState
import xyz.mirage.app.business.domain.models.Post
import xyz.mirage.app.presentation.core.util.FileUtils
import java.lang.reflect.ParameterizedType

val moshi: Moshi = Moshi.Builder().build()
val jsonListType: ParameterizedType = Types.newParameterizedType(List::class.java, PostDto::class.java)
val filesAdapter: JsonAdapter<List<PostDto>> = moshi.adapter(jsonListType)
//val fileAdapter: JsonAdapter<PostDto> = moshi.adapter(PostDto::class.java)

class GetFeed(
    private val application: Application,
    private val cache: PostDao,
    private val service: PostService,
) {

    fun execute(
        page: Int,
        cursor: String?,
        isNetworkAvailable: Boolean,
    ): Flow<DataState<List<Post>>> = flow {
        emit(DataState.loading())

//        if (isNetworkAvailable) {
//            val posts = service.feed(cursor = cursor).posts.toPostList()
//            // insert into cache
//
//            for (post in posts) {
//                cache.insertPost(post.toEntity())
//                cache.insertAuthor(post.profile.toEntity())
//            }

            val pathRoot = FileUtils.getExternalStorageDir(application)
            val posts = FileUtils.getTweetsJsonStringList(application).asSequence().mapNotNull {
                print(it)
                filesAdapter.fromJson(it)
            }.onEach {
                it.onEach { postDto ->
                    if (postDto.textZh != null && postDto.textZh.isNotEmpty()) {
                        postDto.text = "${postDto.textZh}\n${postDto.text}"
                    }
                }
            }.map {
                it.toPostList()
            }.flatten().onEach { post ->
                post.files?.onEach { file ->
                    file.url = FileProvider.getUriForFile(application,
                        "${BuildConfig.APPLICATION_ID}.provider",
                        File(File(pathRoot, post.profile.username), file.url)).toString()
                }
                post.files?.firstOrNull()?.let {
                    post.file = it
                }
            }.toList()

            for (post in posts) {
                cache.insertPost(post.toEntity())
                cache.insertAuthor(post.profile.toEntity())
            }
//        }

        // query the cache
        val cacheResult = cache.getFeed(
            page = page
        )

        // emit List<Recipe> from cache
        val list = cacheResult.toPostList()

        emit(DataState.data(response = null, data = list))
    }.catch { e ->
        emit(handleUseCaseException(e))
    }
}