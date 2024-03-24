package com.example.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Todo(val todo_id : String, val event_id : String, val name : String, val description : String?, val price : Int, val assigned_user_id : String)