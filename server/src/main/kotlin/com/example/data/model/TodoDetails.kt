package com.example.data.model

import kotlinx.serialization.Serializable

@Serializable
data class TodoDetails(
    val name: String,
    val description: String?,
    val price: Int
)