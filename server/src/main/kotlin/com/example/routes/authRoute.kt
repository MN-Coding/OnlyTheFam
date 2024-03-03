package com.example.routes

import com.example.data.model.User
import com.example.data.model.UserLogin
import com.example.data.schema.Users
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.authRoutes() {

    post("/login") {
        val loginInfo = call.receive<UserLogin>()
        val email = loginInfo.email
        val password = loginInfo.password

        val foundUser = transaction {
            // Check if the username and password match in the Users table
            Users.select { (Users.email eq email) and (Users.password eq password) }
                .map { User(it[Users.userID], it[Users.email], it[Users.password]) }
                .singleOrNull()
        }

        if (foundUser != null) {
            call.respondText(Json.encodeToString(foundUser.userID), ContentType.Application.Json, status = HttpStatusCode.Accepted)
        } else {
            call.respondText("Invalid username or password", status = HttpStatusCode.BadRequest)
        }
    }
}