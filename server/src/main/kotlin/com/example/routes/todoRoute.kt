package com.example.routes

import com.example.data.model.AddEventReq
import com.example.data.model.AddTodoReq
import com.example.data.model.Todo
import com.example.data.schema.Event_Participants
import com.example.data.schema.Todos
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

fun Route.todoRoutes() {

    // make route to get all todos
    get("/getAllTodos") {
        val todosList = transaction {
            Todos.selectAll()
                .map { Todo(it[Todos.todo_id], it[Todos.event_id], it[Todos.name], it[Todos.description], it[Todos.price], it[Todos.assigned_user_id]) }
        }

        call.respondText(Json.encodeToString(todosList), ContentType.Application.Json, status = HttpStatusCode.OK)
    }

    // make route to get todo by id
    get("/getTodoByID") {
        val todo_id = call.parameters["todoID"]
        if (todo_id != null) {
            val result = withContext(Dispatchers.IO) {
                val resultSet = Todos.select { Todos.todo_id eq todo_id }
                resultSet.map { Todo(it[Todos.todo_id], it[Todos.event_id], it[Todos.name], it[Todos.description], it[Todos.price], it[Todos.assigned_user_id]) }
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
            val todoUUID = java.util.UUID.randomUUID().toString()
            transaction {
                Todos.insert {
                    it[todo_id] = todoUUID
                    it[event_id] = event_id_res[0]
                    it[name] = req.name
                    it[description] = req.description
                    it[price] = req.price
                    it[assigned_user_id] = user_id_res[0]
                }
            }
            call.respondText("Todo added", status = HttpStatusCode.OK)
        } else {
            call.respondText("Invalid request", status = HttpStatusCode.BadRequest)
        }
    }
}