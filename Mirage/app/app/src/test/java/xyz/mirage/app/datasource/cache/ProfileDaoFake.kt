package xyz.mirage.app.datasource.cache

import xyz.mirage.app.business.datasources.cache.post.ProfileEntity
import xyz.mirage.app.business.datasources.cache.profile.ProfileDao

class ProfileDaoFake(
    private val db: AppDatabaseFake
) : ProfileDao {

    override suspend fun insertProfile(profile: ProfileEntity): Long {
        db.profiles.add(profile)
        return 1
    }

    override suspend fun insertProfiles(profiles: List<ProfileEntity>): LongArray {
        db.profiles.addAll(profiles)
        return longArrayOf(1)
    }

    override suspend fun getProfileByUsername(username: String): ProfileEntity? {
        return db.profiles.find { it.username == username }
    }

    override suspend fun updateFollow(
        followers: Int,
        followee: Int,
        following: Boolean,
        id: String
    ) {
        for (profile in db.profiles) {
            if (profile.id == id) {
                db.profiles.remove(profile)
                val updated =
                    profile.copy(
                        followers = followers,
                        followee = followee,
                        following = following
                    )
                db.profiles.add(updated)
                break
            }
        }
    }

    override suspend fun deleteProfiles(ids: List<String>): Int {
        db.profiles.removeIf { it.id in ids }
        return 1 // return success
    }

    override suspend fun deleteAllProfiles() {
        db.profiles.clear()
    }

    override suspend fun searchProfiles(search: String, pageSize: Int): List<ProfileEntity> {
        return db.profiles
    }
}