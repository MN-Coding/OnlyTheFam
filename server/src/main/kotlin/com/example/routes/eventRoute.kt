package com.example.routes

import com.example.data.model.Event
import com.example.data.schema.Events
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.selectAll

fun Route.eventRoutes() {

    get("/getallevents") {
        val eventsList = transaction {
            Events.selectAll()
                .map { Event(it[Events.event_id], it[Events.name], it[Events.description], it[Events.start_datetime].toString(), it[Events.end_datetime].toString()) }
        }

        call.respondText(Json.encodeToString(eventsList), ContentType.Application.Json, status = HttpStatusCode.OK)
    }

    post("/addEvent") {
        val event = call.receive<Event>()
        val eventID = event.eventID
        val eventName = event.name
        val eventDescription = event.description
        val startDatetime = event.startDatetime
        val endDatetime = event.endDatetime

        transaction {
            // Check if the username and password match in the Users table
            Events.insert {
                it[event_id] = eventID
                it[name] = eventName
                it[description] = eventDescription
                //it[start_datetime] = startDatetime
                //it[end_datetime] = endDatetime
            }
        }

        call.respondText("Event created successfully")
    }

}