package com.example.data.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class AddTodoReq(
    val event_name : String,
    val name : String,
    val description : String?,
    val price : Int,
    val assigned_user_name : String,
    val creator_id : String
)