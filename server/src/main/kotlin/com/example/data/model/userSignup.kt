package com.example.data.model
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class UserSignup(
    val name: String,
    val email: String,
    val password: String,
    val dobstr: String,
    val startNewFamily: Boolean,
    val familyId: String,
    val locationSharing: Boolean,
    val allergies: List<String>,
    val bloodType: String,
    val otherHealth: String
)