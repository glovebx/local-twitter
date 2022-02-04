package xyz.mirage.app.business.datasources.cache.profile

import androidx.room.*
import xyz.mirage.app.business.datasources.cache.post.ProfileEntity

@Dao
interface ProfileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ProfileEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertProfiles(profiles: List<ProfileEntity>): LongArray

    @Transaction
    @Query("SELECT * FROM authors WHERE username = :username")
    suspend fun getProfileByUsername(username: String): ProfileEntity?

    @Query("UPDATE authors SET followers = :followers, followee = :followee, following = :following WHERE id = :id")
    suspend fun updateFollow(followers: Int, followee: Int, following: Boolean, id: String)

    @Query("DELETE FROM authors WHERE id IN (:ids)")
    suspend fun deleteProfiles(ids: List<String>): Int

    @Query("DELETE FROM authors")
    suspend fun deleteAllProfiles()

    @Transaction
    @Query(
        """
        SELECT * FROM authors
        WHERE username LIKE '%' || :search || '%'
        OR displayName LIKE '%' || :search || '%'
        LIMIT :pageSize
        """
    )
    suspend fun searchProfiles(
        search: String,
        pageSize: Int = 20
    ): List<ProfileEntity>
}