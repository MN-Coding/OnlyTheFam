package com.example.data.schema

import org.jetbrains.exposed.sql.Table

object Users : Table() {
    val userID = varchar("user_id", length = 100)
    val email = varchar("email", length = 100)
    val password = varchar("password", length = 100)

    override val primaryKey = PrimaryKey(userID)
}