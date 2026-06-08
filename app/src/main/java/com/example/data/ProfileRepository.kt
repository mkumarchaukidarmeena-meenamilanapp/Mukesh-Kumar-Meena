package com.example.data

import kotlinx.coroutines.flow.Flow

class ProfileRepository(private val profileDao: ProfileDao) {
    val allProfiles: Flow<List<Profile>> = profileDao.getAllProfiles()
    val shortlistedProfiles: Flow<List<Profile>> = profileDao.getShortlistedProfiles()

    fun getProfilesByGender(gender: String): Flow<List<Profile>> {
        return profileDao.getProfilesByGender(gender)
    }

    suspend fun insertProfile(profile: Profile): Long {
        return profileDao.insertProfile(profile)
    }

    suspend fun updateProfile(profile: Profile) {
        profileDao.updateProfile(profile)
    }

    suspend fun deleteProfile(profile: Profile) {
        profileDao.deleteProfile(profile)
    }

    suspend fun toggleShortlist(id: Long, isShortlisted: Boolean) {
        profileDao.updateShortlistStatus(id, isShortlisted)
    }

    suspend fun checkForPreload() {
        val count = profileDao.getCount()
        if (count == 0) {
            for (profile in DefaultProfiles.getList()) {
                profileDao.insertProfile(profile)
            }
        }
    }
}
