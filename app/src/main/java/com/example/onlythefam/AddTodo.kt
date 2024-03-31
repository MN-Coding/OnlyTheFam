package com.example.onlythefam

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDateTime
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import java.time.format.DateTimeFormatter
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.navigation.NavController
import getFamilyMembers
import java.util.*
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.call.receive
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class SubmitTodoRequest(
    val event_name: String,
    val name: String,
    val description: String?,
    val price: Int,
    val assigned_user_name: String,
    val creator_id: String
)

suspend fun submitTodo(
    eventName: String,
    name: String,
    description: String,
    price: Int,
    assigned_user_name: String,
    creatorID: String = GlobalVariables.userId!!.toString()
): Boolean {

    val submitTodoEndpoint = "http://${GlobalVariables.localIP}:5050/addTodo"
    Log.d("SubmitTodo", "Endpoint: $submitTodoEndpoint")
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

    try {
        val response: HttpResponse = client.post(submitTodoEndpoint) {
            contentType(ContentType.Application.Json)
            setBody(SubmitTodoRequest(eventName, name, description, price, assigned_user_name, creatorID))
        }
        Log.d("SubmitTodo", "Response Status: ${response.status}")

        // Close the client after the request
        client.close()
        val isSuccess = response.status.value in 200..299
        if (isSuccess) {
            Log.d("SubmitTodo", "Todo submission successful")
        } else {
            Log.d("SubmitTodo", "Todo submission failed with status: ${response.status}")
        }
        return isSuccess
    } catch (e: Exception) {
        Log.e("SubmitTodo", "Exception during todo submission", e)
        return false
    }
}

@Serializable
data class Event(
    val eventID: String,
    val name: String,
    val description: String?,
    val startDatetime: String,
    val endDatetime: String,
    val location: String,
    val creatorID: String
)

@Serializable
data class Username(
    val username: String
)

suspend fun getAllEvents(): List<String> {
    val getAllEventsEndpoint = "http://${GlobalVariables.localIP}:5050/getallevents"
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

    try {
        val response: HttpResponse = client.get(getAllEventsEndpoint) {
            contentType(ContentType.Application.Json)
        }
        val rawJson: String = response.bodyAsText() // Read the raw JSON response
        println("Raw JSON response: $rawJson") // Print the raw JSON response

        val events: List<Event> = response.body()
        client.close()
        return events.map { it.name } // map the List<Event> to a List<String> containing only the names
    } catch (e: Exception) {
        Log.e("GetAllEvents", "Exception during fetching all events", e)
        return emptyList()
    }
}

suspend fun getAllUsernames(): List<String> {
    val getAllUsernamesEndpoint = "http://${GlobalVariables.localIP}:5050/getallusernames"
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

    try {
        val response: HttpResponse = client.get(getAllUsernamesEndpoint) {
            contentType(ContentType.Application.Json)
        }
        val rawJson: String = response.bodyAsText() // Read the raw JSON response
        println("Raw JSON response: $rawJson") // Print the raw JSON response

        val usernames: List<Username> = response.body()
        client.close()
        return usernames.map { it.username } // map the List<Username> to a List<String> containing only the names
    } catch (e: Exception) {
        Log.e("GetAllUsernames", "Exception during fetching all usernames", e)
        return emptyList()
    }
}


@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddTodo(navController: NavController) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var todoName by remember { mutableStateOf("Enter Todo Name") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }

    val uid = GlobalVariables.userId?.replace("\"", "") ?: ""


    val coroutineScope = rememberCoroutineScope()


    var eventOptions by remember { mutableStateOf(listOf<String>()) }
    var usernameOptions by remember { mutableStateOf(listOf<String>()) }
    var eventIndex by remember { mutableStateOf(0) }
    var usernameIndex by remember { mutableStateOf(0) }
    var showEventDropdown by remember { mutableStateOf(false) }
    var showUsernameDropdown by remember { mutableStateOf(false) }

    LaunchedEffect(uid) {
        if (uid.isNotEmpty()) {
            coroutineScope.launch {
                try {
                    val events = getEventsByUserId(uid)
                    eventOptions = events.map { it.name }
                    eventOptions += ""
                    val usernames = getFamilyMembers(uid)
                    if (usernames != null) {
                        usernameOptions = usernames.map { it.name }
                    }
                } catch (e: Exception) {
                    Log.e("AddTodo", "Exception during fetching all events", e)
                    eventOptions = listOf("Error fetching events")
                }
            }
        }
    }

    Scaffold(
        topBar = {
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Add Todo",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    style = TextStyle(fontSize = 36.sp)
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .verticalScroll(scrollState, enabled = true)
        ) {
            EditableTextField(
                fieldName = "Todo Name:",
                fieldVal = todoName,
                onChange = { updated -> todoName = updated })

            Spacer(Modifier.height(12.dp))

            Text("Event:", fontWeight = FontWeight.Bold)
            Box {
                OutlinedTextField(
                    value = if (eventOptions.isNotEmpty()) eventOptions[eventIndex] else "",
                    onValueChange = { },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,  // Make the TextField read-only
                    trailingIcon = {
                        IconButton(onClick = { showEventDropdown = true }) {
                            Icon(Icons.Filled.ArrowDropDown, contentDescription = "Select Event")
                        }
                    }
                )
                DropdownMenu(
                    expanded = showEventDropdown,
                    onDismissRequest = { showEventDropdown = false }
                ) {
                    eventOptions.forEachIndexed { index, event ->
                        DropdownMenuItem(onClick = {
                            eventIndex = index
                            showEventDropdown = false
                        }) {
                            Text(event)
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            EditableTextField(
                fieldName = "Description:",
                fieldVal = description,
                onChange = { updated -> description = updated })

            EditableTextField(
                fieldName = "Price:",
                fieldVal = price,
                onChange = { updated -> price = updated })

            Text("Assigned User:", fontWeight = FontWeight.Bold)
            Box {
                OutlinedTextField(
                    value = if (usernameOptions.isNotEmpty()) usernameOptions[usernameIndex] else "",
                    onValueChange = { },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,  // Make the TextField read-only
                    trailingIcon = {
                        IconButton(onClick = { showUsernameDropdown = true }) {
                            Icon(Icons.Filled.ArrowDropDown, contentDescription = "Select assigned user")
                        }
                    }
                )
                DropdownMenu(
                    expanded = showUsernameDropdown,
                    onDismissRequest = { showUsernameDropdown = false }
                ) {
                    usernameOptions.forEachIndexed { index, username ->
                        DropdownMenuItem(onClick = {
                            usernameIndex = index
                            showUsernameDropdown = false
                        }) {
                            Text(username)
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                Button(onClick = { navController.navigate("todo_event_screen") }) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(onClick = {

                    println("making post request")

                    coroutineScope.launch {
                        val isSuccess = submitTodo(
                            eventOptions[eventIndex],
                            todoName,
                            description,
                            price.toInt(),
                            usernameOptions[usernameIndex],
                            GlobalVariables.userId!!.toString()
                        )
                        if (isSuccess) {
                            navController.navigate("todo_event_screen")
                        }
                    }
                }) {
                    Text("Submit")
                }
            }
        }
    }
}
