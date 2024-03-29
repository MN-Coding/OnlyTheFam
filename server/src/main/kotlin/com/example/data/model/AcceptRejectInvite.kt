package com.example.data.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class AcceptRejectInvite(
    val invite_id: String,
    val event_id: String?,
    val todo_id: String?,
    val receiver_user_id: String,
)