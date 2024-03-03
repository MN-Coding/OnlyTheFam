package com.example.data.schema

import org.jetbrains.exposed.sql.Table

object Users : Table() {
    val userID = integer("user_id").autoIncrement()
    val email = varchar("Email", length = 100)
    val password = varchar("Password", length = 100)

    override val primaryKey = PrimaryKey(userID)
}