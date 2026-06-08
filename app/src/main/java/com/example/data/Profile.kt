package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "profiles")
data class Profile(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val gender: String, // "Groom" (वर) or "Bride" (वधू)
    val age: Int,
    val dateOfBirth: String,
    val height: String, // e.g., "5'5\""
    val nativePlace: String, // Village/Paternal town
    val state: String, // e.g., "Rajasthan"
    val city: String, // e.g., "Jaipur"
    val gotraSelf: String, // Father's gotra (Self)
    val gotraMother: String, // Mother's gotra (Nihal)
    val gotraDadi: String, // Father's Mother's gotra
    val gotraNani: String, // Mother's Mother's gotra
    val education: String, // e.g., "B.Tech", "M.A.", "B.Sc", "RAS"
    val occupation: String, // e.g., "Govt Teacher", "Police Constable", "Software Engineer"
    val annualIncome: String, // e.g., "6,00,000" or "Govt Job"
    val fatherName: String,
    val fatherOccupation: String,
    val motherName: String,
    val phone: String,
    val email: String,
    val aboutMe: String, // Brief intro
    val isShortlisted: Boolean = false,
    val isUserCreated: Boolean = false,
    val avatarId: Int = 1 // Index for profile avatar illustrations (1 to 8)
) : Serializable
