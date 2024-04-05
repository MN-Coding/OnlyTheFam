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
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

// Observer interface
interface InviteObserver {
    fun onInviteReceived(invite: InviteResponse)
}

// Observable class
class InviteObservable {
    private val observers = mutableListOf<InviteObserver>()

    fun addObserver(observer: InviteObserver) {
        observers.add(observer)
    }

    fun removeObserver(observer: InviteObserver) {
        observers.remove(observer)
    }

    fun notifyInviteReceived(invite: InviteResponse) {
        observers.forEach { it.onInviteReceived(invite) }
    }
}

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

@Serializable
data class AcceptRejectInvite(
    val invite_id: String,
    val event_id: String?,
    val todo_id: String?,
    val receiver_user_id: String,
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

suspend fun acceptInvite(
    invite_id: String,
    event_id: String?,
    todo_id: String?,
    receiver_user_id: String
): Boolean {

    println(todo_id)

    val acceptInviteEndpoint = "http://${GlobalVariables.localIP}:5050/acceptInvite"

    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

    try {
        val response: HttpResponse = client.post(acceptInviteEndpoint) {
            contentType(ContentType.Application.Json)
            setBody(AcceptRejectInvite(invite_id, event_id, todo_id, receiver_user_id))
        }

        Log.d("Accept Invite", "Response Status: ${response.status}")

        // Close the client after the request
        client.close()
        val isSuccess = response.status.value in 200..299
        if (isSuccess) {
            Log.d("Accept Invite", "Accept Invite successful")
        } else {
            Log.d("Accept Invite", "Accept Invite failed with status: ${response.status}")
        }
        return isSuccess
    } catch (e: Exception) {
        Log.e("Accept Invite", "Exception during Accept Invite", e)
        return false
    }
}

suspend fun rejectInvite(
    invite_id: String,
    event_id: String?,
    todo_id: String?,
    receiver_user_id: String
): Boolean {

    println(todo_id)

    val rejectInviteEndpoint = "http://${GlobalVariables.localIP}:5050/declineInvite"

    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

    try {
        val response: HttpResponse = client.post(rejectInviteEndpoint) {
            contentType(ContentType.Application.Json)
            setBody(AcceptRejectInvite(invite_id, event_id, todo_id, receiver_user_id))
        }


        // Close the client after the request
        client.close()
        val isSuccess = response.status.value in 200..299
        if (isSuccess) {
            Log.d("Decline Invite", "Decline Invite successful")
        } else {
            Log.d("Decline Invite", "Decline Invite failed with status: ${response.status}")
        }
        return isSuccess
    } catch (e: Exception) {
        Log.e("Decline Invite", "Decline during Accept Invite", e)
        return false
    }
}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Inbox(navController: NavController) {

    val coroutineScope = rememberCoroutineScope()
    val username = remember { GlobalVariables.username }
    val invitations = remember { mutableStateOf(listOf<InviteResponse>()) }

    val inviteObservable = remember { InviteObservable() }
    val inviteObserver = object : InviteObserver {
        override fun onInviteReceived(invite: InviteResponse) {
            // Handle the new invite here
            coroutineScope.launch {
                invitations.value = listOf(invite) + invitations.value
            }
        }
    }
    inviteObservable.addObserver(inviteObserver)


    LaunchedEffect(key1 = Unit) {
        coroutineScope.launch {
            try {
                invitations.value = getAllInvites()
            } catch (e: Exception) {
                Log.e("GetPendingInvitations", "Exception during fetching all invitations", e)
            }
        }
    }

    Column {
        Text(text = "${username}'s Inbox", style = MaterialTheme.typography.h4, modifier = Modifier.padding(16.dp))
        if (invitations.value.isEmpty()) {
            Text(
                text = "No invites",
                style = MaterialTheme.typography.body1,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(30.dp, 45.dp)
                    .fillMaxSize()
            ) {
                items(invitations.value) { invitation ->
                    InvitationCard(invitation, coroutineScope = coroutineScope, invitations = invitations)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
@Composable
fun InvitationCard(invitation: InviteResponse, coroutineScope: CoroutineScope, invitations: MutableState<List<InviteResponse>>) {
    var isExpanded by remember { mutableStateOf(false) }

    var isEvent = invitation.event != null

    // Determine the border color based on whether it's an event or a task
    val borderColor = when {
        isEvent -> Color.Blue
        !isEvent -> Color.Green
        else -> Color.Black
    }

    Card(
        modifier = Modifier
            .fillMaxWidth() // This makes the card as wide as the screen
            .padding(16.dp),
        border = BorderStroke(1.dp, borderColor),
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
                Text(
                    if (isEvent) "Event: ${invitation.event?.event_name}" else "Invite: ${invitation.todo?.name}", fontSize = 20.sp)
                IconButton(onClick = { isExpanded = !isExpanded }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Expand")
                }
            }
            if (isExpanded) {
                Text("From: ${invitation.sender_user_name}")
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
                        onClick = { coroutineScope.launch { GlobalVariables.userId?.trim('"')?.let {
                            if (acceptInvite(invitation.invite_id, invitation.event_id, invitation.todo_id, it)) {
                                invitations.value = getAllInvites()
                            }
                        } } },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Accept", tint = Color.Green)
                    }
                    IconButton(
                        onClick = { coroutineScope.launch { GlobalVariables.userId?.trim('"')?.let {
                            if (rejectInvite(invitation.invite_id, invitation.event_id, invitation.todo_id, it)) {
                                invitations.value = getAllInvites()
                            }
                        } } },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Reject", tint = Color.Red)
                    }
                }
            }
        }
    }
}