package com.example.data.schema

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime


object Events : Table() {
    val event_id = text("event_id")
    val name = text("name")
    val description = text("description").nullable()
    val start_datetime = text("start_datetime")
    val end_datetime = text("end_datetime")
    val location = text("location")

    override val primaryKey = PrimaryKey(event_id)
}
