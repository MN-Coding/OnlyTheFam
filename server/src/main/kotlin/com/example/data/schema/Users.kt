package com.example.data.schema

import org.jetbrains.exposed.sql.Table

object Users : Table() {
    val userID = varchar("user_id", length = 100)
    val email = varchar("email", length = 100)
    var name = varchar("name", length = 100)
    val password = varchar("password", length = 100)
    var bloodType = varchar("blood_type", length = 20)
    var otherHealth = varchar("other_health_facts", length = 100)
    var locationSharing = bool("share_location")

    override val primaryKey = PrimaryKey(userID)
}