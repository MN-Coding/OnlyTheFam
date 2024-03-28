package com.example.data.schema

import org.jetbrains.exposed.sql.Table

object Invites : Table() {
    val invite_id = text("invite_id")
    val event_id = text("event_id").references(Events.event_id).nullable()
    val todo_id = text("todo_id").references(Todos.todo_id).nullable()
    val sender_user_id = varchar("sender_user_id", length = 100).references(Users.userID)
    val receiver_user_id = varchar("receiver_user_id", length = 100).references(Users.userID)
    val status = varchar("status", length = 20) // e.g., "pending", "accepted", "declined"

    override val primaryKey = PrimaryKey(invite_id)
}