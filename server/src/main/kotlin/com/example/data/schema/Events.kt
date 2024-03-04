package com.example.data.schema

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object Events : Table() {
    val event_id = text("event_id")
    val name = text("name")
    val description = text("description").nullable()
    val start_datetime = datetime("start_datetime")
    val end_datetime = datetime("end_datetime")

    override val primaryKey = PrimaryKey(event_id)
}

object EventParticipants : Table() {
    val event_id = reference("event_id", Events.event_id)
    val user_id = text("user_id")
    val cost_percentage = integer("cost_percentage").nullable()

    override val primaryKey = PrimaryKey(event_id, user_id)
}

