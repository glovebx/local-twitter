package xyz.mirage.app.datasource.cache

import xyz.mirage.app.business.datasources.cache.account.AccountEntity
import xyz.mirage.app.business.datasources.cache.post.PostEntity
import xyz.mirage.app.business.datasources.cache.post.ProfileEntity

class AppDatabaseFake {

    // fake for post table in local db
    val posts = mutableListOf<PostEntity>()
    val accounts = mutableListOf<AccountEntity>()
    val profiles = mutableListOf<ProfileEntity>()
}