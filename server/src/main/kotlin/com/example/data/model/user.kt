package com.example.data.model

import kotlinx.serialization.Serializable

@Serializable
data class User(val userID: String, val email: String, val password: String, val name: String)

@Serializable
data class UserAllergies(val userID: String, val allergies: List<String>)

@Serializable
data class UserBloodType(val userID: String, val bloodType: String)