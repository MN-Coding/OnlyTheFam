package com.example.routes

import com.example.data.model.User
import com.example.data.model.UserLogin
import com.example.data.model.UserSignup
import com.example.data.schema.Allergies
import com.example.data.schema.Users
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate
import java.util.*

fun Route.authRoutes() {

    post("/login") {
        val loginInfo = call.receive<UserLogin>()
        val email = loginInfo.email
        val password = loginInfo.password

        val foundUser = transaction {
            // Check if the username and password match in the Users table
            Users.select { (Users.email eq email) and (Users.password eq password) }
                .map { User(it[Users.userID], it[Users.email], it[Users.password], it[Users.name]) }
                .singleOrNull()
        }

        if (foundUser != null) {
            val response = mapOf("userID" to foundUser.userID, "name" to foundUser.name)
            call.respondText(Json.encodeToString(response), ContentType.Application.Json, status = HttpStatusCode.Accepted)
        } else {
            call.respondText("Invalid username or password", status = HttpStatusCode.BadRequest)
        }
    }

    fun verifyParams(signupParams: UserSignup) {
        if (signupParams.name.isBlank() || signupParams.email.isBlank() || signupParams.password.isBlank()) {
            throw Exception("1 (or more) fields are empty.")
        }
        if (transaction {
                Users.select { Users.email eq signupParams.email }.count() > 0
            }) {
            throw Exception("An account with this email already exists.")
        }
    }

    post("/signup") {
//        val signupParams = call.receive<UserSignup>()
        val jsonBody = call.receive<String>()
        val signupParams = Json.decodeFromString<UserSignup>(jsonBody)
        val dob = LocalDate.parse(signupParams.dobstr)

        var userID: String
        var familyID: String

        if (signupParams.startNewFamily) {
            do {
                familyID = UUID.randomUUID().toString()
            } while (transaction {
                    Users.select { Users.familyId eq familyID }.count() > 0
                })
        } else {
            familyID = "pending"
            // send request to family
        }

        do {
            userID = UUID.randomUUID().toString()
        } while (transaction {
            Users.select { Users.userID eq userID }.count() > 0
        })

        try {
            verifyParams(signupParams)
            val user_insert = transaction {
                Users.insert { row ->
                    row[Users.userID] = userID
                    row[name] = signupParams.name
                    row[email] = signupParams.email
                    row[Users.dob] = dob
                    row[familyId] = familyID
                    row[password] = signupParams.password
                    row[bloodType] = signupParams.bloodType
                    row[otherHealth] = signupParams.otherHealth
                    row[locationSharing] = signupParams.locationSharing
                }
            }
            for (allergy in signupParams.allergies) {
                transaction { Allergies.insert { row ->
                    row[Allergies.userID] = userID
                    row[Allergies.allergy] = allergy
                }
                }
            }
            val response = mapOf("userID" to userID, "name" to signupParams.name)
            call.respondText(Json.encodeToString(response), ContentType.Application.Json, status = HttpStatusCode.OK)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, "Error: " + e.message)
        }
    }

}