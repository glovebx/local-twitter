package xyz.mirage.app.datasource.cache

import xyz.mirage.app.business.datasources.cache.account.AccountDao
import xyz.mirage.app.business.datasources.cache.account.AccountEntity

class AccountDaoFake(
    private val db: AppDatabaseFake
) : AccountDao {

    override suspend fun searchByUsername(username: String): AccountEntity? {
        for (account in db.accounts) {
            if (account.username == username) {
                return account
            }
        }
        return null
    }

    override suspend fun searchById(id: String): AccountEntity? {
        for (account in db.accounts) {
            if (account.id == id) {
                return account
            }
        }
        return null
    }

    override suspend fun insertAndReplace(account: AccountEntity): Long {
        db.accounts.removeIf {
            it.id == account.id
        }
        db.accounts.add(account)
        return 1 // always return success
    }

    override suspend fun insertOrIgnore(account: AccountEntity): Long {
        if (!db.accounts.contains(account)) {
            db.accounts.add(account)
        }
        return 1 // always return success
    }

    override suspend fun updateAccount(
        id: String,
        displayName: String,
        username: String,
        image: String,
        bio: String?,
        banner: String?,
        email: String
    ) {
        for (account in db.accounts) {
            if (account.id == id) {
                val updated =
                    account.copy(
                        displayName = displayName,
                        image = image,
                        username = username,
                        banner = banner,
                        bio = bio,
                        email = email
                    )
                db.accounts.remove(account)
                db.accounts.add(updated)
                break
            }
        }
    }

    override suspend fun clear() {
        db.accounts.clear()
    }

}