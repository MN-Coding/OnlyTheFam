package com.example.onlythefam

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class InviteResponse(
    val invite_id: String,
    val event_id: String? = null,
    val todo_id: String? = null,
    val sender_user_id: String,
    val receiver_user_id: String,
    val status: String,
    val sender_user_name: String,
    val event: EventDetails? = null,
    val todo: TodoDetails? = null
)

@Serializable
data class TodoDetails(
    val name: String,
    val description: String?,
    val price: Int
)

@Serializable
data class EventDetails(
    val event_name: String,
    val event_details: String,
    val location: String,
    val event_start_date: String,
    val event_end_date: String
)
suspend fun getAllInvites(): List<InviteResponse> {
    val getInvitesEndpoint = "http://${GlobalVariables.localIP}:5050/invites/${GlobalVariables.userId?.trim('"')}"

    println(getInvitesEndpoint)


    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

    try {
        val response: HttpResponse = client.get(getInvitesEndpoint) {
            contentType(ContentType.Application.Json)
        }
        val rawJson: String = response.bodyAsText() // Read the raw JSON response
        println("Raw JSON response: $rawJson") // Print the raw JSON response

        val invites: List<InviteResponse> = response.body()
        client.close()
        return invites
    } catch (e: Exception) {
        Log.e("GetAllEvents", "Exception during fetching all events", e)
        return emptyList()
    }
}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Inbox(navController: NavController) {

    val coroutineScope = rememberCoroutineScope()

    var invitations by remember { mutableStateOf(listOf<InviteResponse>()) }

    LaunchedEffect(key1 = Unit) {
        coroutineScope.launch {
            try {
                invitations = getAllInvites()
            } catch (e: Exception) {
                Log.e("GetPendingInvitations", "Exception during fetching all invitations", e)
            }
        }
    }



    LazyColumn(
        modifier = Modifier
            .padding(30.dp, 45.dp)
            .fillMaxSize()
    ) {
        items(invitations) { invitation ->
            InvitationCard(invitation)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun InvitationCard(invitation: InviteResponse) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth() // This makes the card as wide as the screen
            .padding(16.dp),
        border = BorderStroke(1.dp, Color.Black),
        shape = RoundedCornerShape(8.dp),
        backgroundColor = Color.White
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(invitation.event?.event_name ?: invitation.todo?.name ?: "", fontSize = 20.sp)
                IconButton(onClick = { isExpanded = !isExpanded }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Expand")
                }
            }
            if (isExpanded) {
                if (invitation.event != null) {
                    Text("Event Details: ${invitation.event.event_details}")
                    Text("Event Start Date: ${invitation.event.event_start_date}")
                    Text("Location: ${invitation.event.location}")
                } else if (invitation.todo != null) {
                    Text("Todo Description: ${invitation.todo.description}")
                    Text("Price: ${invitation.todo.price}")
                }

                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = { /* Handle Accept */ },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Accept", tint = Color.Green)
                    }
                    IconButton(
                        onClick = { /* Handle Reject */ },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Reject", tint = Color.Red)
                    }
                }
            }
        }
    }
}