package xyz.mirage.app.business.interactors.profile

import kotlinx.coroutines.flow.Flow
import xyz.mirage.app.business.domain.core.DataState
import xyz.mirage.app.business.domain.models.Post

abstract class AbstractProfilePosts {
    abstract fun execute(
        cursor: String?,
        username: String,
    ): Flow<DataState<List<Post>>>
}