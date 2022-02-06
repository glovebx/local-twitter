package xyz.mirage.app.business.datasources.cache.post

import androidx.room.*

@Dao
interface PostDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: PostEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPosts(posts: List<PostEntity>): LongArray

    @Transaction
    @Query("SELECT * FROM posts WHERE id = :id")
    suspend fun getPostById(id: String): PostAuthor?

    @Query("UPDATE posts SET retweets = :retweets, retweeted = :retweeted WHERE id = :id")
    suspend fun updateRetweets(retweets: Int, retweeted: Boolean, id: String)

    @Query("UPDATE posts SET likes = :likes, liked = :liked WHERE id = :id")
    suspend fun updateLikes(likes: Int, liked: Boolean, id: String)

    @Query("DELETE FROM posts WHERE id IN (:ids)")
    suspend fun deletePosts(ids: List<String>): Int

    @Query("DELETE FROM posts")
    suspend fun deleteAllPosts()

//    @Query("DELETE FROM posts WHERE id = :primaryKey")
    @Query("UPDATE posts SET isFeed = 0 WHERE id = :primaryKey")
    suspend fun deletePost(primaryKey: String): Int

    /**
     * Retrieve posts for a particular page.
     * Ex: page = 2 retrieves posts from 30-60.
     * Ex: page = 3 retrieves posts from 60-90
     */
    @Transaction
    @Query(
        """
        SELECT * FROM posts
        WHERE isFeed == 1
        ORDER BY created_at DESC LIMIT :pageSize OFFSET ((:page - 1) * :pageSize)
        """
    )
    suspend fun getFeed(
        page: Int,
        pageSize: Int = 20
    ): List<PostAuthor>

    @Transaction
    @Query(
        """
        SELECT * FROM posts
        WHERE authorId = :authorId AND created_at < :createdAt AND isFeed == 1
        ORDER BY created_at DESC LIMIT :pageSize
        """
    )
    suspend fun getFeed(
        authorId: String,
        createdAt: String,
        pageSize: Int = 20
    ): List<PostAuthor>

    /**
     * Same as 'restorePosts' function, but no query.
     */
    @Transaction
    @Query(
        """
        SELECT * FROM posts 
        ORDER BY created_at DESC LIMIT (:page * :pageSize)
    """
    )
    suspend fun restoreAllPosts(
        page: Int,
        pageSize: Int = 20
    ): List<PostAuthor>

    @Transaction
    @Query(
        """
        SELECT * FROM posts
        WHERE text LIKE '%' || :search || '%'
        ORDER BY created_at DESC LIMIT :pageSize OFFSET ((:page - 1) * :pageSize)
        """
    )
    suspend fun searchPosts(
        search: String,
        page: Int,
        pageSize: Int = 20
    ): List<PostAuthor>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuthor(profile: ProfileEntity): Long
}