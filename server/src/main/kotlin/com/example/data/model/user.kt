package com.example.data.model

import kotlinx.serialization.Serializable

@Serializable
data class User(val userID: String, val email: String, val password: String)

@Serializable
data class UserPersonal(val userID: String, val name: String, val dobstr: String)

@Serializable
data class UserAllergies(val userID: String, val allergies: List<String>)

@Serializable
data class UserBloodType(val userID: String, val bloodType: String)