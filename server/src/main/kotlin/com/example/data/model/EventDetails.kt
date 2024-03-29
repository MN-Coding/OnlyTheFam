package com.example.data.model

import kotlinx.serialization.Serializable

@Serializable
data class EventDetails(
    val event_name: String,
    val event_details: String,
    val location: String,
    val event_start_date: String,
    val event_end_date: String
)