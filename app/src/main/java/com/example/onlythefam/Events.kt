package com.example.onlythefam

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import com.example.onlythefam.ui.theme.Blue700

@Serializable
data class EventResponse(
    @SerialName("event_id") val eventID: String,
    val name: String,
    val description: String,
    @SerialName("start_datetime") val startDatetime: String,
    @SerialName("end_datetime") val endDatetime: String,
    val location: String,
    val participants: List<String>
)

// For UI state management
data class EventUiModel(
    val eventResponse: EventResponse,
) {
    var expanded by mutableStateOf(false)
}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EventsPage(navController: NavController) {

    // log the user id
    Log.d("EventsPage", "GlobalVariables.userId: ${GlobalVariables.userId}")

    val uid = GlobalVariables.userId?.replace("\"", "") ?: ""
    val username = remember { GlobalVariables.username }
    val eventsUiModel = remember { mutableStateListOf<EventUiModel>() }

    LaunchedEffect(uid) {
        if (uid.isNotEmpty()) {
            val events = getEventsByUserId(uid)
            eventsUiModel.addAll(events.map { EventUiModel(it) })
        }
    }

    Scaffold {
        Column {
            Text(text = "${username}'s Events", style = MaterialTheme.typography.h4, modifier = Modifier.padding(16.dp))
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                eventsUiModel.forEach { eventUiModel ->
                    EventCard(eventUiModel = eventUiModel, navController = navController)
                }
            }
        }
    }
}

@Composable
fun EventCard(eventUiModel: EventUiModel, navController: NavController) {
    Card(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
            .clickable { navController.navigate("eventDetails/${eventUiModel.eventResponse.eventID}") },
        backgroundColor = Blue700,
        shape = RoundedCornerShape(16.dp),
        elevation = 8.dp,
        border = BorderStroke(1.dp, Color.Gray),
        contentColor = Color.White,
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = eventUiModel.eventResponse.name,
                style = MaterialTheme.typography.body1,
                modifier = Modifier.padding(bottom = 8.dp),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private suspend fun getEventsByUserId(userId: String): List<EventResponse> {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

    Log.d("EventsPage", "getEventsByUserId called with userID: $userId")

    return try {
        val eventsEndpoint = "http://${GlobalVariables.localIP}:5050/geteventsbyuserid?userID=$userId"
        Log.d("EventsPage", "Requesting events from: $eventsEndpoint")
        val response: HttpResponse = withContext(Dispatchers.IO) {
            client.get(eventsEndpoint) {
                contentType(ContentType.Application.Json)
            }
        }
        Log.d("EventsPage", "Response status: ${response.status}")

        if (response.status == HttpStatusCode.OK) {
            val events = Json.decodeFromString<List<EventResponse>>(response.bodyAsText())
            Log.d("EventsPage", "Events retrieved successfully: ${events.size}")
            events
        } else {
            Log.d("EventsPage", "Failed to retrieve events")
            emptyList()
        }
    } catch (e: Exception) {
        Log.e("EventsPage", "Error fetching events", e)
        emptyList()
    } finally {
        client.close()
    }
}
