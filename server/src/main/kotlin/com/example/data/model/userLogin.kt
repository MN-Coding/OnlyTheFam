package com.example.data.model
import kotlinx.serialization.Serializable

@Serializable
data class UserLogin(val email: String, val password: String)