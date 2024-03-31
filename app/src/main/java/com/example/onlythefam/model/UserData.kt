package com.example.onlythefam.model

import kotlinx.serialization.Serializable

@Serializable
data class UserInfo(
    val name: String,
    val email: String,
    val bloodType: String,
    val dob: String,
    val familyID: String
)

@Serializable
data class UserPersonal(val userID: String, val name: String, val dobstr: String)

@Serializable
data class UserAllergies(val userID: String, val allergies: List<String>)

@Serializable
data class UserBloodType(val userID: String, val bloodType: String)