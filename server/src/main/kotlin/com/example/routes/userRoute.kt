package com.example.routes

import com.example.data.model.User
import com.example.data.model.Username
import com.example.data.model.UserLogin
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
import io.ktor.util.Identity.encode
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.reflect.jvm.internal.impl.resolve.scopes.MemberScope.Empty
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.selectAll

@Serializable
data class UserInfo(
    val name: String,
    val email: String,
    val bloodType: String
)

fun Route.userRoutes() {

    put("/updateInfo"){
        /* Pending */
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

    get("/getUserInfo"){
        val id = call.parameters["userID"]
        if (id != null){
            val userInfo = transaction{
                Users.select { (Users.userID eq id) }
                    .map {row ->
                        UserInfo(
                            name = row[Users.name],
                            email = row[Users.email],
                            bloodType = row[Users.bloodType]
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

    // get all names of users
    get("/getallusernames") {
        val usersList = transaction {
            Users.selectAll()
                .map { Username(it[Users.userID]) }
        }

        call.respondText(Json.encodeToString(usersList), ContentType.Application.Json, status = HttpStatusCode.OK)
    }
}