package com.example

import com.example.plugins.configureRouting
import com.example.plugins.configureSerialization
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.jetbrains.exposed.sql.Database

fun main() {
    embeddedServer(Netty, port = 5050, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {

    Database.connect(
        url = "jdbc:postgresql://onlythefam-do-user-9272876-0.c.db.ondigitalocean.com:25060/onlythefam",
        driver = "org.postgresql.Driver",
        user = "doadmin",
        password = "AVNS_vIBhnnTu7WwLLnJXOz5"
    )

    configureRouting()
    configureSerialization()
}
