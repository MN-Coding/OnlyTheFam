package com.example.data.schema

import org.jetbrains.exposed.sql.Table


object Todos : Table() {
    val todo_id = text("todo_id")
    val event_id = text("event_id").references(Events.event_id)
    val name = text("name")
    val description = text("description").nullable()
    val price = integer("price")
    val assigned_user_id = text("assigned_user_id").references(Users.userID).nullable()
    val creator_id = text("creator_id").references(Users.userID)

    override val primaryKey = PrimaryKey(todo_id)
}
