package com.example.routes

import com.example.data.model.AddTodoReq
import com.example.data.model.Todo
import com.example.data.schema.Todos
import com.example.data.schema.Events
import com.example.data.schema.Invites
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
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select

fun Route.todoRoutes() {

    // make route to get all todos
    get("/getAllTodos") {
        val todosList = transaction {
            Todos.selectAll()
                .map { Todo(it[Todos.todo_id], it[Todos.event_id], it[Todos.name], it[Todos.description], it[Todos.price], it[Todos.assigned_user_id], it[Todos.creator_id]) }
        }

        call.respondText(Json.encodeToString(todosList), ContentType.Application.Json, status = HttpStatusCode.OK)
    }

    // make route to get todo by id
    get("/getTodoByID") {
        val todo_id = call.parameters["todoID"]
        if (todo_id != null) {
            val result = withContext(Dispatchers.IO) {
                val resultSet = Todos.select { Todos.todo_id eq todo_id }
                resultSet.map { Todo(it[Todos.todo_id], it[Todos.event_id], it[Todos.name], it[Todos.description], it[Todos.price], it[Todos.assigned_user_id], it[Todos.creator_id]) }
            }
            call.respondText(Json.encodeToString(result), ContentType.Application.Json, status = HttpStatusCode.OK)
        } else {
            call.respondText("Invalid request", status = HttpStatusCode.BadRequest)
        }
    }

    // make route to add a todo but the request takes event name instead of event id
    // first get event id by matching event name from Events table
    // get assigned user id by matching assigned user name from Users table
    // make the todo id todoUUID
    // add the todo to table

    post("/addTodo") {
        val req = call.receive<AddTodoReq>()
        val event_id_res = transaction {
            Events.select { Events.name eq req.event_name }
                .map { it[Events.event_id] }
        }
        val user_id_res = transaction {
            Users.select { Users.name eq req.assigned_user_name }
                .map { it[Users.userID] }
        }
        if (event_id_res.isNotEmpty() && user_id_res.isNotEmpty()) {
            val todoUUID = "todo"+java.util.UUID.randomUUID().toString()
            println("req ------------------" + req)
            println("creator id trimmed ------------------" + req.creator_id.trim('"'))
            println("creator id ------------------" + req.creator_id)
            transaction {
                Todos.insert {
                    it[todo_id] = todoUUID
                    it[event_id] = event_id_res[0]
                    it[name] = req.name
                    it[description] = req.description
                    it[price] = req.price
                    it[creator_id] = req.creator_id.trim('"')
                }
                Invites.insert {
                    it[Invites.invite_id] = java.util.UUID.randomUUID().toString()
                    it[Invites.todo_id] = todoUUID
                    it[Invites.sender_user_id] = req.creator_id.trim('"')
                    it[Invites.receiver_user_id] = user_id_res[0]
                    it[Invites.status] = "pending"
                }
            }
            call.respondText("Todo added", status = HttpStatusCode.OK)
        } else {
            call.respondText("Invalid request", status = HttpStatusCode.BadRequest)
        }
    }

    // make route to get all todos by user id
    get("/getTodosByUserID") {
        val user_id = call.parameters["userID"]
        if (user_id != null) {
            val result = runBlocking {
                withContext(Dispatchers.IO) {
                    transaction {
                        val resultSet = Todos.select { Todos.assigned_user_id eq user_id }
                        resultSet.map { Todo(it[Todos.todo_id], it[Todos.event_id], it[Todos.name], it[Todos.description], it[Todos.price], it[Todos.assigned_user_id], it[Todos.creator_id]) }
                    }
                }
            }
            call.respondText(Json.encodeToString(result), ContentType.Application.Json, status = HttpStatusCode.OK)
        } else {
            call.respondText("Invalid request", status = HttpStatusCode.BadRequest)
        }
    }

    delete("/deleteTodo") {
        val todo_id = call.parameters["todo_id"]
        if (todo_id != null) {
            transaction {
                // First delete the invite with the todo_id
                Invites.deleteWhere { Invites.todo_id eq todo_id }

                // Then delete the todo
                Todos.deleteWhere { Todos.todo_id eq todo_id }
            }
            call.respondText("Todo and related invite deleted", status = HttpStatusCode.OK)
        } else {
            call.respondText("Invalid request", status = HttpStatusCode.BadRequest)
        }
    }
}