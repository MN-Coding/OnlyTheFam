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
                .map { Event(it[Events.event_id], it[Events.name], it[Events.description], it[Events.start_datetime], it[Events.end_datetime]) }
        }

        call.respondText(Json.encodeToString(eventsList), ContentType.Application.Json, status = HttpStatusCode.OK)
    }

    post("/addevent") {
        val eventData = call.receive<Event>()
        transaction {
            Events.insert { event ->
                event[event_id] = eventData.eventID
                event[name] = eventData.name
                event[description] = eventData.description
                event[start_datetime] = eventData.startDatetime
                event[end_datetime] = eventData.endDatetime
            }
        }
        call.respond(HttpStatusCode.Created)
    }
}