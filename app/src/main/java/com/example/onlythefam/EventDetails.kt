package com.example.onlythefam

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.navigation.NavController
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberAsyncImagePainter
import java.net.URLEncoder
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.ui.Alignment
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EventDetails(navController: NavController, eventId: String) {
    val event = remember { mutableStateOf<EventResponse?>(null) }

    LaunchedEffect(eventId) {
        event.value = getEventById(eventId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = event.value?.name ?: "Loading...") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                backgroundColor = MaterialTheme.colors.primary
            )
        }
    ) { innerPadding ->
        event.value?.let { eventDetails ->
            Column(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
                Text(text = eventDetails.description, style = MaterialTheme.typography.body1)
                val formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy h:mma")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.AccessTime, contentDescription = "Time")
                    Text(
                        text = "Start: ${
                            LocalDateTime.parse(eventDetails.startDatetime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).format(
                                formatter
                            )
                        }",
                        style = MaterialTheme.typography.body1
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.AccessTime, contentDescription = "Time")
                    Text(
                        text = "End: ${
                            LocalDateTime.parse(eventDetails.endDatetime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).format(
                                formatter
                            )
                        }",
                        style = MaterialTheme.typography.body1
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.LocationOn, contentDescription = "Location")
                    Text(text = "Location: ${eventDetails.location}", style = MaterialTheme.typography.body1)
                }
                Text(text = "Participants: ${eventDetails.participants.joinToString(", ")}", style = MaterialTheme.typography.body1)
                val address = eventDetails.location
                val encodedAddress = URLEncoder.encode(address, "UTF-8")
                val mapUrl = "https://maps.googleapis.com/maps/api/staticmap?center=$encodedAddress&zoom=15&size=600x300&maptype=roadmap&markers=color:red%7C$encodedAddress&key=AIzaSyCg28OjKgjh8mYsAlrtDhtXF-0L2QMH1_Q"
                Image(
                    painter = rememberAsyncImagePainter(mapUrl),
                    contentDescription = "Event Location Map",
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth(),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

private suspend fun getEventById(eventId: String): EventResponse? {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

    Log.d("EventDetails", "getEventById called with eventID: $eventId")

    try {
        val eventEndpoint = "http://${GlobalVariables.localIP}:5050/geteventbyid?eventId=$eventId"
        Log.d("EventDetails", "Requesting event from: $eventEndpoint")

        val response: HttpResponse = withContext(Dispatchers.IO) {
            client.get(eventEndpoint) {
                contentType(ContentType.Application.Json)
            }
        }

        Log.d("EventDetails", "Response status: ${response.status}")

        if (response.status == HttpStatusCode.OK) {
            return Json.decodeFromString(EventResponse.serializer(), response.bodyAsText())
        } else {
            Log.d("EventDetails", "Failed to retrieve the event")
            return null
        }
    } catch (e: Exception) {
        Log.e("EventDetails", "Error fetching the event", e)
        return null
    } finally {
        client.close()
    }
}