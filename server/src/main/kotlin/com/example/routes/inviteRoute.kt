package com.example.routes

import com.example.data.model.AddEventReq
import com.example.data.model.Event
import com.example.data.model.EventDetails
import com.example.data.model.InviteResponse
import com.example.data.model.TodoDetails
import com.example.data.schema.Event_Participants
import com.example.data.schema.Events
import com.example.data.schema.Invites
import com.example.data.schema.Invites.status
import com.example.data.schema.Todos
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
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.update

fun Route.inviteRoutes() {

    // Route to create an invite
    post("/invite") {
        val req = call.receive<InviteResponse>()
        transaction {
            Invites.insert {
                it[event_id] = req.event_id
                it[todo_id] = req.todo_id
                it[sender_user_id] = req.sender_user_id
                it[receiver_user_id] = req.receiver_user_id
                it[status] = req.status
                it[invite_id] = java.util.UUID.randomUUID().toString()
            }
        }
        call.respond(HttpStatusCode.OK, "Invite created")
    }

    // Route to get all pending invites for a user by user ID
    get("/invites/{user_id}") {
        val userId = call.parameters["user_id"]
        if (userId != null) {
            val pendingInvites = transaction {
                Invites
                    .join(Users, JoinType.INNER, additionalConstraint = { Invites.sender_user_id eq Users.userID })
                    .join(Events, JoinType.LEFT, additionalConstraint = { Invites.event_id eq Events.event_id })
                    .join(Todos, JoinType.LEFT, additionalConstraint = { Invites.todo_id eq Todos.todo_id })
                    .select {
                        (Invites.receiver_user_id eq userId) and (Invites.status eq "pending")
                    }.map {
                        val event = it[Invites.event_id]?.let { eventId ->
                            transaction {
                                Events.select { Events.event_id eq eventId }.singleOrNull()?.let { row ->
                                    EventDetails(
                                        event_name = row[Events.name],
                                        event_details = row[Events.description] ?: "",
                                        location = row[Events.location],
                                        event_start_date = row[Events.start_datetime],
                                        event_end_date = row[Events.end_datetime],
                                    )
                                }
                            }
                        }

                        val todo = it[Invites.todo_id]?.let { todoId ->
                            transaction {
                                Todos.select { Todos.todo_id eq todoId }.singleOrNull()?.let { row ->
                                    TodoDetails(
                                        name = row[Todos.name],
                                        description = row[Todos.description],
                                        price = row[Todos.price]
                                    )
                                }
                            }
                        }
                        InviteResponse(
                            invite_id = it[Invites.invite_id],
                            event_id = it[Invites.event_id],
                            todo_id = it[Invites.todo_id],
                            sender_user_id = it[Invites.sender_user_id],
                            receiver_user_id = it[Invites.receiver_user_id],
                            status = it[Invites.status],
                            sender_user_name = it[Users.name],
                            event = event,
                            todo = todo
                        )
                    }
            }
            call.respond(HttpStatusCode.OK, pendingInvites)
        } else {
            call.respond(HttpStatusCode.BadRequest, "User ID must be provided")
        }
    }

    // Route to accept an invite
    post("/acceptInvite") {
        val req = call.receive<InviteResponse>()
        transaction {
            Invites.update({ Invites.invite_id eq req.invite_id }) {
                it[status] = "accepted"
            }
        }

        // now add the user to the event if event_id is not null
        if (req.event_id != null) {
            transaction {
                Event_Participants.insert {
                    it[Event_Participants.event_id] = req.event_id
                    it[Event_Participants.user_id] = req.receiver_user_id
                }
            }
        }

        // now add the user to the todo if todo_id is not null
        if (req.todo_id != null) {
            transaction {
                Todos.update({ Todos.todo_id eq req.todo_id }) {
                    it[assigned_user_id] = req.receiver_user_id
                }
            }
        }

        call.respond(HttpStatusCode.OK, "Invite accepted")
    }

    // Route to decline an invite
    post("/declineInvite") {
        val req = call.receive<InviteResponse>()
        transaction {
            Invites.update({ Invites.invite_id eq req.invite_id }) {
                it[status] = "declined"
            }
        }
        call.respond(HttpStatusCode.OK, "Invite declined")
    }


}