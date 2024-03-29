package com.example.data.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class Event(
    val eventID: String,
    val name: String,
    val description: String?,
    val startDatetime: String,
    val endDatetime: String,
    val location: String,
    val creatorID: String
)
