package com.example.onlythefam

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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


@Serializable
data class EventResponse(
    @SerialName("event_id") val eventID: String,
    val name: String,
    val description: String?,
    @SerialName("start_datetime") val startDatetime: String,  // TODO: change these later if we change dates pack to type TIMESTAMP?
    @SerialName("end_datetime") val endDatetime: String,
    val participants: List<String>
)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EventsPage() {
    val uid = GlobalVariables.userId?.replace("\"", "") ?: ""
    val events = remember { mutableStateListOf<EventResponse>() }

    LaunchedEffect(uid) {
        Log.d("EventsPage", "Fetching events for user ID: $uid")
        if (uid.isNotEmpty()) {
            events.addAll(getEventsByUserId(uid))
            Log.d("EventsPage", "Events fetched: ${events.size}")
        }
    }

    Scaffold {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            events.forEach { event ->
                Log.d("EventsPage", "Displaying event: ${event.name}")
                Card(
                    modifier = Modifier
                        .padding(10.dp)
                        .fillMaxWidth(),
                    backgroundColor = MaterialTheme.colors.surface,
                    shape = RoundedCornerShape(16.dp),
                    elevation = 8.dp,
                    border = BorderStroke(1.dp, Color.Gray)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = event.name,
                            style = MaterialTheme.typography.body1,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = event.description ?: "",
                                style = MaterialTheme.typography.caption
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = event.startDatetime,
                                style = MaterialTheme.typography.caption
                            )
                        }
                    }
                }
            }
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
