package com.example.data.schema

import org.jetbrains.exposed.sql.Table

object Allergies : Table() {
    val userID = varchar("user_id", length = 100)
    var allergy = varchar("allergy", length = 255)

    override val primaryKey = PrimaryKey(userID)
}