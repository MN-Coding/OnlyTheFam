package com.example.data.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class Event(
    val eventID: String,
    val name: String,
    val description: String?,
    @Contextual val startDatetime: LocalDateTime,
    @Contextual val endDatetime: LocalDateTime
)
