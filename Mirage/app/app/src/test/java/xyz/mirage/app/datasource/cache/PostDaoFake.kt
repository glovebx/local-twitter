package xyz.mirage.app.datasource.cache

import xyz.mirage.app.business.datasources.cache.post.PostAuthor
import xyz.mirage.app.business.datasources.cache.post.PostDao
import xyz.mirage.app.business.datasources.cache.post.PostEntity
import xyz.mirage.app.business.datasources.cache.post.ProfileEntity


class PostDaoFake(
    private val db: AppDatabaseFake
) : PostDao {

    override suspend fun insertPost(post: PostEntity): Long {
        db.posts.add(post)
        return 1 // return success
    }

    override suspend fun insertPosts(posts: List<PostEntity>): LongArray {
        db.posts.addAll(posts)
        return longArrayOf(1) // return success
    }

    override suspend fun getPostById(id: String): PostAuthor? {
        val post = db.posts.find { it.id == id } ?: return null
        val author = db.profiles.find { it.id == post.authorId } ?: return null
        return PostAuthor(post, author)
    }

    override suspend fun updateRetweets(retweets: Int, retweeted: Boolean, id: String) {
        for (post in db.posts) {
            if (post.id == id) {
                db.posts.remove(post)
                val updated = post.copy(retweets = retweets, retweeted = retweeted)
                db.posts.add(updated)
                break
            }
        }
    }

    override suspend fun updateLikes(likes: Int, liked: Boolean, id: String) {
        for (post in db.posts) {
            if (post.id == id) {
                db.posts.remove(post)
                val updated = post.copy(likes = likes, liked = liked)
                db.posts.add(updated)
                break
            }
        }
    }

    override suspend fun deletePosts(ids: List<String>): Int {
        db.posts.removeIf { it.id in ids }
        return 1 // return success
    }

    override suspend fun deleteAllPosts() {
        db.posts.clear()
    }

    override suspend fun deletePost(primaryKey: String): Int {
        db.posts.removeIf { it.id == primaryKey }
        return 1 // return success
    }

    override suspend fun getFeed(page: Int, pageSize: Int): List<PostAuthor> {
        return db.posts.map { post ->
            PostAuthor(post, db.profiles.find { profile ->
                profile.id == post.authorId
            }!!)
        }
    }

    override suspend fun restoreAllPosts(page: Int, pageSize: Int): List<PostAuthor> {
        return db.posts.map { post ->
            PostAuthor(post, db.profiles.find { profile ->
                profile.id == post.authorId
            }!!)
        }
    }

    override suspend fun searchPosts(search: String, page: Int, pageSize: Int): List<PostAuthor> {
        return db.posts.map { post ->
            PostAuthor(post, db.profiles.find { profile ->
                profile.id == post.authorId
            }!!)
        }
    }

    override suspend fun insertAuthor(profile: ProfileEntity): Long {
        db.profiles.add(profile)
        return 1
    }
}