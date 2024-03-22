package com.example.routes

import com.example.data.model.AddEventReq
import com.example.data.model.AddTodoReq
import com.example.data.model.Todo
import com.example.data.schema.Event_Participants
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
import org.jetbrains.exposed.sql.select

fun Route.todoRoutes() {

    // make route to get all todos
    get("/getalltodos") {
        val todosList = transaction {
            Todos.selectAll()
                .map { Todo(it[Todos.todo_id], it[Todos.event_id], it[Todos.name], it[Todos.description], it[Todos.price], it[Todos.assigned_user_id]) }
        }

        call.respondText(Json.encodeToString(todosList), ContentType.Application.Json, status = HttpStatusCode.OK)
    }

    // make route to get todo by id
    get("/gettodobyid") {
        val todo_id = call.parameters["todoID"]
        if (todo_id != null) {
            val result = withContext(Dispatchers.IO) {
                // Fetch a single todo by its ID
                val query = """
                SELECT 
                    T.todo_id,
                    T.event_id,
                    T.name,
                    T.description,
                    T.price,
                    T.assigned_user_id
                FROM 
                    Todos T
                WHERE 
                    T.todo_id = '%s'
                """.trimIndent().format(todo_id)
                val resultSet = Todos.select { Todos.todo_id eq todo_id }
                resultSet.map { Todo(it[Todos.todo_id], it[Todos.event_id], it[Todos.name], it[Todos.description], it[Todos.price], it[Todos.assigned_user_id]) }
            }
            call.respondText(Json.encodeToString(result), ContentType.Application.Json, status = HttpStatusCode.OK)
        } else {
            call.respondText("Invalid request", status = HttpStatusCode.BadRequest)
        }
    }

    // make route to add a todo
    post("/addtodo") {
        val addTodoReq = call.receive<AddTodoReq>()
        val event_id = addTodoReq.event_id
        val name = addTodoReq.name
        val description = addTodoReq.description
        val price = addTodoReq.price
        val assigned_user_id = addTodoReq.assigned_user_id

        transaction {
            Todos.insert {
                it[Todos.todo_id] = java.util.UUID.randomUUID().toString()
                it[Todos.event_id] = event_id
                it[Todos.name] = name
                it[Todos.description] = description
                it[Todos.price] = price
                it[Todos.assigned_user_id] = assigned_user_id
            }
        }

        call.respondText("Todo added successfully", status = HttpStatusCode.OK)
    }


}