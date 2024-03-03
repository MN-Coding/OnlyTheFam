package com.example.data.model

import kotlinx.serialization.Serializable

@Serializable
data class User(val userID: String, val email: String, val password: String)