package com.example.routes

import com.example.data.model.Event
import com.example.data.schema.EventParticipants
import com.example.data.schema.Events
import com.example.data.schema.Users
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
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.selectAll

fun Route.eventRoutes() {

    get("/getallevents") {
        val eventsList = transaction {
            Events.selectAll()
                .map { Event(it[Events.event_id], it[Events.name], it[Events.description], it[Events.start_datetime].toString(), it[Events.end_datetime].toString(), it[Events.location]) }
        }

        call.respondText(Json.encodeToString(eventsList), ContentType.Application.Json, status = HttpStatusCode.OK)
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
        val eventData = call.receive<Event>()
        transaction {
            Events.insert { event ->
                event[event_id] = eventData.eventID
                event[name] = eventData .name
                event[description] = eventData.description
                event[start_datetime] = eventData.startDatetime
                event[end_datetime] = eventData.endDatetime
                event[location] = eventData.location
            }
        }
        call.respond(HttpStatusCode.Created)
    }
}