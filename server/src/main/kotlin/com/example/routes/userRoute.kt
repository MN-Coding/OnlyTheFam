package com.example.routes

import com.example.data.model.UserAllergies
import com.example.data.model.UserBloodType
import com.example.data.model.UserPersonal
import com.example.data.model.Username
import com.example.data.schema.Allergies
import com.example.data.schema.Users
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.LocalDate

@Serializable
data class UserInfo(
    val name: String,
    val email: String,
    val bloodType: String,
    val dob: String? = null,
    val familyID: String
)

@Serializable
data class FamilyInfo(
    val name: String,
    val email: String,
    val bloodType: String,
    val dob: String,
    val familyID: String,
    val otherHealthInformation: String
)

fun Route.userRoutes() {

    put("/updateBloodType"){
        val userInfo = call.receive<UserBloodType>()
        val id = userInfo.userID
        val newBloodType = userInfo.bloodType

        val updatedEntries = transaction {
            Users.update({ Users.userID eq id }){
                it[bloodType] = newBloodType
            }
        }

        if (updatedEntries > 0){
            call.respond(HttpStatusCode.OK, "Blood type updated successfully")
        }
        else{
            call.respond(HttpStatusCode.NotFound, "User not found")
        }
    }

    put("/updateName"){
        val userInfo = call.receive<UserPersonal>()
        val id = userInfo.userID
        val newName = userInfo.name

        val updatedEntries = transaction {
            Users.update({ Users.userID eq id }){
                it[name] = newName
            }
        }

        if (updatedEntries > 0){
            call.respond(HttpStatusCode.OK, "name updated successfully")
        }
        else{
            call.respond(HttpStatusCode.NotFound, "User not found")
        }
    }

    put("/updateDob"){
        val userInfo = call.receive<UserPersonal>()
        val id = userInfo.userID
        val newDob = LocalDate.parse(userInfo.dobstr)

        val updatedEntries = transaction {
            Users.update({ Users.userID eq id }){
                it[dob] = newDob
            }
        }

        if (updatedEntries > 0){
            call.respond(HttpStatusCode.OK, "birthdate updated successfully")
        }
        else{
            call.respond(HttpStatusCode.NotFound, "User not found")
        }

    }

    post("/addAllergies"){
        val userInfo = call.receive<UserAllergies>()
        val id = userInfo.userID
        val newAllergies = userInfo.allergies

        for (newAllergy in newAllergies) {
            transaction {
                Allergies.insert {allergyEntry ->
                    allergyEntry[userID] = id
                    allergyEntry[allergy] = newAllergy
                }
            }
        }
        call.respond(HttpStatusCode.Created)
    }

    get("/getAllergies"){
        val id = call.parameters["userID"]

        if (id != null){
            val allergies = transaction{
                Allergies.select { (Allergies.userID eq id) }
                    .map { it[Allergies.allergy] }
            }

            call.respondText(Json.encodeToString(allergies), ContentType.Application.Json, status = HttpStatusCode.Accepted)
        }
        else{
            call.respondText("User not found", status = HttpStatusCode.BadRequest)
        }

    }

    get("/getHealthInfo"){
        val id = call.parameters["userID"]

        if (id != null){
            val healthInfo = transaction{
                Users.select { (Users.userID eq id) }
                    .map { it[Users.otherHealth] }
            }

            call.respondText(Json.encodeToString(healthInfo), ContentType.Application.Json, status = HttpStatusCode.Accepted)
        }
        else{
            call.respondText("User not found", status = HttpStatusCode.BadRequest)
        }

    }

    get("/getUserInfo"){
        val id = call.parameters["userID"]
        if (id != null){
            val userInfo = transaction{
                Users.select { (Users.userID eq id) }
                    .map {row ->
                        UserInfo(
                            name = row[Users.name],
                            email = row[Users.email],
                            bloodType = row[Users.bloodType],
                            dob = row[Users.dob].toString(),
                            familyID = row[Users.familyId]
                        )
                    }.singleOrNull()
            }

            if (userInfo != null) {
                call.respond(userInfo)
            }
            else{
                call.respondText("User not found", status = HttpStatusCode.BadRequest)
            }
        }
        else{
            call.respondText("Missing userID", status = HttpStatusCode.BadRequest)
        }
    }

    get("/emailExists"){
        val email = call.parameters["email"]
        if (email != null){
            val userInfo = transaction{
                Users.select { (Users.email eq email) }
                    .map {row ->
                        UserInfo(
                            name = row[Users.name],
                            email = row[Users.email],
                            bloodType = row[Users.bloodType],
                            dob = row[Users.dob].toString(),
                            familyID = row[Users.familyId]
                        )
                    }.singleOrNull()
            }

            if (userInfo != null) {
                call.respond(userInfo)
            }
            else{
                call.respondText("User not found", status = HttpStatusCode.NotFound)
            }
        }
        else{
            call.respondText("Missing email", status = HttpStatusCode.BadRequest)
        }
    }

    get("/familyExists"){
        val familyId = call.parameters["familyId"]
        if (familyId != null){
            val familyExists = transaction{
                Users.select { (Users.familyId eq familyId) }
                    .map {row ->
                        UserInfo(
                            name = row[Users.name],
                            email = row[Users.email],
                            bloodType = row[Users.bloodType],
                            familyID = row[Users.familyId]
                        )
                    }.isNotEmpty()
            }

            if (familyExists) {
                call.respond(status = HttpStatusCode.OK, "Exists")
            }
            else{
                call.respondText("Family ID not found", status = HttpStatusCode.NotFound)
            }
        }
        else{
            call.respondText("Missing familyId", status = HttpStatusCode.BadRequest)
        }
    }

    get("/getFamilyMembers") {
        val userId = call.parameters["userID"]
        if (userId != null) {
            val familyMembers = transaction {
                val familyId = Users.slice(Users.familyId)
                    .select { Users.userID eq userId }
                    .map { it[Users.familyId] }
                    .singleOrNull()

                if (familyId != null) {
                    Users.selectAll().where { Users.familyId eq familyId }
                        .map { row ->
                            FamilyInfo(
                                name = row[Users.name],
                                email = row[Users.email],
                                bloodType = row[Users.bloodType],
                                dob = row[Users.dob].toString(),
                                familyID = row[Users.familyId],
                                otherHealthInformation = row[Users.otherHealth]
                            )
                        }
                } else {
                    null
                }
            }
            if (familyMembers != null) {
                call.respond(familyMembers)
            } else {
                call.respondText("Family not found or User ID invalid", status = HttpStatusCode.NotFound)
            }
        } else {
            call.respondText("Missing userId", status = HttpStatusCode.BadRequest)
        }
    }

    // get all names of users
    get("/getallusernames") {
        val usersList = transaction {
            Users.selectAll()
                .map { Username(it[Users.name]) }
        }

        call.respondText(Json.encodeToString(usersList), ContentType.Application.Json, status = HttpStatusCode.OK)
    }
}