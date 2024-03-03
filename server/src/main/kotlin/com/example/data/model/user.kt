package com.example.data.model

import kotlinx.serialization.Serializable

@Serializable
data class User(val userID: Int, val email: String, val password: String)