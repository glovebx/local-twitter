package xyz.mirage.app.business.datasources.cache.account

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AccountDao {

    @Query("SELECT * FROM accounts WHERE username = :username")
    suspend fun searchByUsername(username: String): AccountEntity?

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun searchById(id: String): AccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAndReplace(account: AccountEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnore(account: AccountEntity): Long

    @Query(
        """
        UPDATE accounts 
        SET displayName = :displayName, 
        image = :image, 
        username = :username,
        email = :email,
        image = :image,
        banner = :banner,
        bio = :bio
        WHERE id = :id
        """
    )
    suspend fun updateAccount(
        id: String,
        displayName: String,
        username: String,
        image: String,
        bio: String?,
        banner: String?,
        email: String,
    )

    @Query("DELETE FROM accounts")
    suspend fun clear()
}
