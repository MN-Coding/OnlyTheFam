package com.example.onlythefam

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
import java.util.*
import io.ktor.client.*
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
    val assigned_user_name: String
)

suspend fun submitTodo(
    eventName: String,
    name: String,
    description: String,
    price: Int,
    assigned_user_name: String
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
            setBody(SubmitTodoRequest(eventName, name, description, price, assigned_user_name))
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


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddTodo(navController: NavController) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var todoName by remember { mutableStateOf("Enter Todo Name") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var assignedUser by remember { mutableStateOf("") }


    val coroutineScope = rememberCoroutineScope()


    val eventOptions = listOf("Birthday Party", "Event 2", "Event 3")
    var eventIndex by remember { mutableStateOf(0) }
    var showDropdown by remember { mutableStateOf(false) }


    Scaffold(
        topBar = {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Add Todo",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    style = TextStyle(fontSize = 36.sp),
                    modifier = Modifier.align(alignment = Alignment.CenterVertically)
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
                fieldName = "Todo Name",
                fieldVal = todoName,
                onChange = { updated -> todoName = updated })

            Spacer(Modifier.height(5.dp))

            Text("Event:", fontWeight = FontWeight.Bold)
            Box {
                OutlinedTextField(
                    value = eventOptions[eventIndex],
                    onValueChange = { },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,  // Make the TextField read-only
                    trailingIcon = {
                        IconButton(onClick = { showDropdown = true }) {
                            Icon(Icons.Filled.ArrowDropDown, contentDescription = "Select Event")
                        }
                    }
                )
                DropdownMenu(
                    expanded = showDropdown,
                    onDismissRequest = { showDropdown = false }
                ) {
                    eventOptions.forEachIndexed { index, event ->
                        DropdownMenuItem(onClick = {
                            eventIndex = index
                            showDropdown = false
                        }) {
                            Text(event)
                        }
                    }
                }
            }

            Spacer(Modifier.height(5.dp))

            EditableTextField(
                fieldName = "Description",
                fieldVal = description,
                onChange = { updated -> description = updated })

            EditableTextField(
                fieldName = "Price",
                fieldVal = price,
                onChange = { updated -> price = updated })

            EditableTextField(
                fieldName = "Assigned User",
                fieldVal = assignedUser,
                onChange = { updated -> assignedUser = updated })

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
                            assignedUser
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
