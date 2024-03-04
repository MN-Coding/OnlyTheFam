package com.example.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Event(val eventID: String, val name: String, val description: String, val startDatetime: String, val endDatetime: String)
