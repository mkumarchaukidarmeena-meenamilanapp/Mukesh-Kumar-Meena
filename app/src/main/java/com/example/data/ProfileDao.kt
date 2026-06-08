package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profiles ORDER BY id DESC")
    fun getAllProfiles(): Flow<List<Profile>>

    @Query("SELECT * FROM profiles WHERE gender = :gender ORDER BY id DESC")
    fun getProfilesByGender(gender: String): Flow<List<Profile>>

    @Query("SELECT * FROM profiles WHERE id = :id LIMIT 1")
    fun getProfileById(id: Long): Flow<Profile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: Profile): Long

    @Update
    suspend fun updateProfile(profile: Profile)

    @Delete
    suspend fun deleteProfile(profile: Profile)

    @Query("UPDATE profiles SET isShortlisted = :isShortlisted WHERE id = :id")
    suspend fun updateShortlistStatus(id: Long, isShortlisted: Boolean)

    @Query("SELECT * FROM profiles WHERE isShortlisted = 1 ORDER BY id DESC")
    fun getShortlistedProfiles(): Flow<List<Profile>>

    @Query("SELECT COUNT(*) FROM profiles")
    suspend fun getCount(): Int
}
