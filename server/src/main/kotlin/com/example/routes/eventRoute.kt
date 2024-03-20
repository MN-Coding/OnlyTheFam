package com.example.routes

import com.example.data.model.AddEventReq
import com.example.data.model.Event
import com.example.data.schema.EventParticipants
import com.example.data.schema.Events
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.selectAll
import com.example.data.schema.Users
import org.jetbrains.exposed.sql.select

fun Route.eventRoutes() {

    get("/getallevents") {
        val eventsList = transaction {
            Events.selectAll()
                .map { Event(it[Events.event_id], it[Events.name], it[Events.description], it[Events.start_datetime].toString(), it[Events.end_datetime].toString(), it[Events.location]) }
        }

        call.respondText(Json.encodeToString(eventsList), ContentType.Application.Json, status = HttpStatusCode.OK)
    }

    get("/geteventbyid") {
        val event_id = call.parameters["eventID"]
        if (event_id != null) {
            val result = withContext(Dispatchers.IO) {
                // Fetch a single event by its ID
                val query = """
                SELECT 
                    E.event_id,
                    E.name,
                    E.description,
                    E.start_datetime,
                    E.end_datetime,
                    E.location,
                    STRING_AGG(U.name, ',') as participants
                FROM 
                    event_participants EP
                JOIN 
                    Events E ON EP.event_id = E.event_id
                JOIN 
                    Users U ON EP.user_id = U.user_id
                WHERE 
                    E.event_id = '%s'
                GROUP BY 
                    E.event_id,
                    E.name,
                    E.description,
                    E.start_datetime,
                    E.end_datetime,
                    E.location
            """.trimIndent().format(event_id)

                // Execute the query
                transaction {
                    exec(query) { rs ->
                        if (rs.next()) {
                            mapOf(
                                "event_id" to rs.getString("event_id"),
                                "name" to rs.getString("name"),
                                "description" to rs.getString("description"),
                                "start_datetime" to rs.getString("start_datetime"),
                                "end_datetime" to rs.getString("end_datetime"),
                                "location" to rs.getString("location"),
                                "participants" to rs.getString("participants").split(",")
                            )
                        } else null
                    }
                }
            }

            if (result != null) {
                val response = result.mapValues { entry ->
                    if (entry.key.endsWith("_datetime")) {
                        entry.value.toString()
                    } else {
                        entry.value
                    }
                }
                call.respond(response)
            } else {
                call.respond(HttpStatusCode.NoContent, "No event found for this eventID")
            }
        } else {
            call.respond(HttpStatusCode.BadRequest, "Invalid or missing eventID")
        }
    }

    get("/geteventsbyuserid") {
        val user_id = call.parameters["userID"]
        if (user_id != null) {
            val result = withContext(Dispatchers.IO) {
                // Define the SQL query
                val query = """
                SELECT 
                    E.event_id,
                    E.name,
                    E.description,
                    E.start_datetime,
                    E.end_datetime,
                    E.location,
                    STRING_AGG(U.name, ',') as participants
                FROM 
                    event_participants EP
                JOIN 
                    Events E ON EP.event_id = E.event_id
                JOIN 
                    Users U ON EP.user_id = U.user_id
                WHERE 
                    EP.user_id = '%s'
                GROUP BY 
                    E.event_id,
                    E.name,
                    E.description,
                    E.start_datetime,
                    E.end_datetime,
                    E.location
            """.trimIndent().format(user_id)


                // Execute the query
                transaction {
                    exec(query) { rs ->
                        generateSequence {
                            if (rs.next()) rs else null
                        }.map {
                            mapOf(
                                "event_id" to it.getString("event_id"),
                                "name" to it.getString("name"),
                                "description" to it.getString("description"),
                                "start_datetime" to it.getString("start_datetime"),
                                "end_datetime" to it.getString("end_datetime"),
                                "location" to it.getString("location"),
                                "participants" to it.getString("participants").split(",")
                            )
                        }.toList()
                    }
                }
            }

            // Check if result is null
            if (result != null) {
                // Convert date-time values to string and respond with the result
                val response = result.map { event ->
                    event.mapValues { entry ->
                        if (entry.key == "start_datetime" || entry.key == "end_datetime") {
                            entry.value.toString()
                        } else {
                            entry.value
                        }
                    }
                }
                call.respond(response)
            } else {
                call.respond(HttpStatusCode.NoContent, "No events found for this userID")
            }
        } else {
            call.respond(HttpStatusCode.BadRequest, "Invalid or missing userID")
        }
    }



    post("/addevent") {
        val eventData = call.receive<AddEventReq>()
        transaction {
            Events.insert { event ->
                event[event_id] = eventData.eventID
                event[name] = eventData .name
                event[description] = eventData.description
                event[start_datetime] = eventData.startDatetime
                event[end_datetime] = eventData.endDatetime
                event[location] = eventData.location
            }

            val participantsNames = eventData.participants

            for (name in participantsNames) {
                // get the user_id for the name
                val userID = Users.select { Users.name eq name }.singleOrNull()?.get(Users.userID)
                // if the user_id is not null, insert the event and user_id into the event_participants table
                if (userID != null) {
                    transaction {
                        EventParticipants.insert { participant ->
                            participant[event_id] = eventData.eventID
                            participant[user_id] = userID
                        }
                    }
                }
            }

        }
        call.respond(HttpStatusCode.Created)
    }
}