package com.example.data.schema

import org.jetbrains.exposed.sql.Table

// Definition for the 'event_participants' table
object EventParticipants : Table() {
    val event_id = reference("event_id", Events.event_id)
    val user_id = reference("user_id", Users.userID)
    val cost_percentage = integer("cost_percentage").nullable()

    override val primaryKey = PrimaryKey(event_id, user_id)
}

