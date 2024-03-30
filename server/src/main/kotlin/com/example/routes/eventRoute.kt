package com.example.routes

import com.example.data.model.AddEventReq
import com.example.data.model.Event
import com.example.data.model.UserPersonal
import com.example.data.schema.Allergies
import com.example.data.schema.Event_Participants
import com.example.data.schema.Events
import com.example.data.schema.Invites
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

fun Route.eventRoutes() {

    get("/getallevents") {
        val eventsList = transaction {
            Events.selectAll()
                .map { Event(it[Events.event_id], it[Events.name], it[Events.description], it[Events.start_datetime].toString(), it[Events.end_datetime].toString(), it[Events.location], it[Events.creator_id]) }
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
                event[name] = eventData.name
                event[description] = eventData.description
                event[start_datetime] = eventData.startDatetime
                event[end_datetime] = eventData.endDatetime
                event[location] = eventData.location
                event[creator_id] = eventData.creatorID.trim('"')
            }

            // Add the creator to the event_participants
            Event_Participants.insert {
                it[event_id] = eventData.eventID
                it[user_id] = eventData.creatorID.trim('"')
                it[cost_percentage] = null // or any default value
            }

            print("ADDING EVENT ---------------------")
            print(eventData)

            val participantsNames = eventData.participants

            // Get the creator's name
            val creatorName = Users.select { Users.userID eq eventData.creatorID.trim('"') }
                .map { it[Users.name] }
                .firstOrNull()

            // obtain each participant's user_id and add them to the event if exists
            for (participantName in participantsNames) {
                // Skip the iteration if the participantName is the creator
                if (participantName == creatorName) {
                    continue
                }

                val user_id = Users.select { Users.name eq participantName }
                    .map { it[Users.userID] }
                    .firstOrNull()

                if (user_id == null) {
                    continue
                }

                println("checking creator -------------- ${eventData.creatorID.trim('"')}" )
                println("checking creator 2 -------------- ${eventData.creatorID}" )

                Invites.insert{
                    it[invite_id] = "invite"+java.util.UUID.randomUUID().toString()
                    it[event_id] = eventData.eventID
                    it[sender_user_id] = eventData.creatorID.trim('"')
                    it[receiver_user_id] = user_id
                    it[status] = "pending"
                }
            }
        }
        call.respond(HttpStatusCode.Created)
    }

    // make route to create event for single userID
    post("/createEvent") {
        val req = call.receive<AddEventReq>()
        val eventUUID = java.util.UUID.randomUUID().toString()
        transaction {
            Events.insert {
                it[event_id] = eventUUID
                it[name] = req.name
                it[description] = req.description
                it[start_datetime] = req.startDatetime
                it[end_datetime] = req.endDatetime
                it[location] = req.location
                it[creator_id] = req.creatorID.trim('"')
            }
            Event_Participants.insert {
                it[Event_Participants.event_id] = eventUUID
                it[Event_Participants.user_id] = req.creatorID.trim('"')
            }
        }
        call.respondText("Event created", status = HttpStatusCode.Created)
    }

    get("/getParticipantAllergies"){
        val id = call.parameters["eventID"]

        val allAllergies = transaction {
            val users = Event_Participants.select { (Event_Participants.event_id eq id!!) }
                .map { it[Event_Participants.user_id]}

            if (users.isEmpty()){
                return@transaction emptyList<String>()
            }
            else{
                Allergies.select {Allergies.userID inList users}
                    .map {it[Allergies.allergy]}
                    .distinct()
            }
        }

        call.respondText(Json.encodeToString(allAllergies), ContentType.Application.Json, status = HttpStatusCode.Accepted)
        //call.respond(HttpStatusCode.OK, allAllergies)

    }

    put("/updateEvent"){
        val eventInfo = call.receive<Event>()
        val id = eventInfo.eventID
        val newDescription = eventInfo.description
        val newLocation = eventInfo.location
        val newStartTime = eventInfo.startDatetime
        val newEndTime = eventInfo.endDatetime

        val updatedEntries = transaction {
            Events.update({ Events.event_id eq id }){
                it[description] = newDescription
                it[location] = newLocation
                it[start_datetime] = newStartTime
                it[end_datetime] = newEndTime
            }
        }

        if (updatedEntries > 0){
            call.respond(HttpStatusCode.OK, "event description updated successfully")
        }
        else{
            call.respond(HttpStatusCode.NotFound, "Event not found")
        }
    }

}