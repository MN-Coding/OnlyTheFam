package com.example.data.model

import kotlinx.serialization.Serializable

@Serializable
data class InviteResponse(
    val invite_id: String,
    val event_id: String?,
    val todo_id: String?,
    val sender_user_id: String,
    val receiver_user_id: String,
    val status: String,
    val sender_user_name: String,
    val event: EventDetails?,
    val todo: TodoDetails?
)