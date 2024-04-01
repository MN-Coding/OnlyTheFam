package com.example.onlythefam

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.net.URLEncoder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EventDetails(navController: NavController, eventId: String) {
    var loading by remember { mutableStateOf(true) }
    val event = remember { mutableStateOf<EventResponse?>(null) }
    var inEditMode by remember { mutableStateOf(false) }
    val editedDescription = remember { mutableStateOf("") }
    val editedLocation = remember { mutableStateOf("") }
    val editedStartTime = remember { mutableStateOf("") }
    val editedEndTime = remember { mutableStateOf("") }
    val allergies = remember { mutableStateOf<List<String>>(listOf()) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(eventId) {
        event.value = getEventById(eventId)
        editedDescription.value = event.value?.description ?: ""
        editedLocation.value = event.value?.location ?: ""
        editedStartTime.value = event.value?.startDatetime ?: ""
        editedEndTime.value = event.value?.endDatetime ?: ""
        coroutineScope.launch {
            val allergiesDelegate = async{getParticipantAllergies(eventId)}
            allergies.value = allergiesDelegate.await()
            loading = false
        }
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
            if (!loading){
            Column(modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)) {
                if (!inEditMode) {
                    StaticEventDetails(eventDetails, allergies)
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { inEditMode = !inEditMode },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    {
                        Text("Edit Event Details")
                    }
                } else {
                    EditEventDetails(eventDetails, editedDescription, editedLocation, editedStartTime, editedEndTime, allergies)
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { inEditMode = !inEditMode;
                            updateEvent(eventDetails, editedDescription.value, editedLocation.value, editedStartTime.value, editedEndTime.value, coroutineScope);
                                  navController.navigate("events")},
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    {
                        Text("Update Event Details")
                    }
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { inEditMode = !inEditMode },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    {
                        Text("Cancel")
                    }
                }
            }
            } else{
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

private fun updateEvent(
    eventDetails: EventResponse,
    newDescription: String,
    newLocation: String,
    newStartTime: String,
    newEndTime: String,
    coroutineScope: CoroutineScope
) {
    coroutineScope.launch {
        val userEndpoint = "http://${GlobalVariables.localIP}:5050/updateEvent"
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json()
            }
        }
        try {
            client.put(userEndpoint) {
                contentType(ContentType.Application.Json)
                setBody(Event(eventDetails.eventID, "", newDescription,
                    newStartTime, newEndTime, newLocation, ""))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            client.close()
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StaticEventDetails(
    eventDetails: EventResponse,
    allergies: MutableState<List<String>>
) {
    Text(text = eventDetails.description, style = MaterialTheme.typography.body1)
    val formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy h:mma")
    Spacer(Modifier.height(12.dp))
    Row(
        modifier = Modifier.padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.AccessTime, contentDescription = "Time", modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(4.dp))
        Text(
            text = "Start: ",
            style = MaterialTheme.typography.body1,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = LocalDateTime.parse(
                eventDetails.startDatetime,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            ).format(
                formatter
            ),
            style = MaterialTheme.typography.body1
        )
    }
    Spacer(Modifier.height(12.dp))
    Row(
        modifier = Modifier.padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.AccessTime, contentDescription = "Time", modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(4.dp))
        Text(
            text = "End: ",
            style = MaterialTheme.typography.body1,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = LocalDateTime.parse(
                eventDetails.endDatetime,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            ).format(
                formatter
            ),
            style = MaterialTheme.typography.body1
        )
    }
    Spacer(Modifier.height(12.dp))
    Row(
        modifier = Modifier.padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.LocationOn, contentDescription = "Location", modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(4.dp))
        Text(text = "Location: ", style = MaterialTheme.typography.body1, fontWeight = FontWeight.Bold)
        Text(text = eventDetails.location, style = MaterialTheme.typography.body1)
    }
    Spacer(Modifier.height(12.dp))
    Row(
        modifier = Modifier.padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = "Participants: ",
            style = MaterialTheme.typography.body1,
            fontWeight = FontWeight.Bold
        )
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = eventDetails.participants.joinToString(", "),
            style = MaterialTheme.typography.body1
        )
    }
    Spacer(Modifier.height(12.dp))
    Row(
        modifier = Modifier.padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = "Allergies: ",
            style = MaterialTheme.typography.body1,
            fontWeight = FontWeight.Bold
        )
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = allergies.value.joinToString(", "),
            style = MaterialTheme.typography.body1
        )
    }
    Spacer(Modifier.height(12.dp))
    val address = eventDetails.location
    val encodedAddress = URLEncoder.encode(address, "UTF-8")
    val mapUrl =
        "https://maps.googleapis.com/maps/api/staticmap?center=$encodedAddress&zoom=15&size=600x300&maptype=roadmap&markers=color:red%7C$encodedAddress&key=AIzaSyCg28OjKgjh8mYsAlrtDhtXF-0L2QMH1_Q"
    Image(
        painter = rememberAsyncImagePainter(mapUrl),
        contentDescription = "Event Location Map",
        modifier = Modifier
            .padding(top = 16.dp)
            .fillMaxWidth(),
        contentScale = ContentScale.Crop
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EditEventDetails(
    eventDetails: EventResponse,
    editedDescription: MutableState<String>,
    editedLocation: MutableState<String>,
    editedStartTime: MutableState<String>,
    editedEndTime: MutableState<String>,
    allergies: MutableState<List<String>>
) {
    var startTime by remember { mutableStateOf(LocalDateTime.now()) }
    var endTime by remember { mutableStateOf(LocalDateTime.now().plusHours(1)) }
    val context = LocalContext.current
    fun updateStartTime(year: Int, month: Int, day: Int, hour: Int, minute: Int) {
        startTime = LocalDateTime.of(year, month + 1, day, hour, minute)
    }

    // Show day + time picker
    fun showDateTimePicker() {
        val currentDateTime = Calendar.getInstance()
        val startYear = currentDateTime.get(Calendar.YEAR)
        val startMonth = currentDateTime.get(Calendar.MONTH)
        val startDay = currentDateTime.get(Calendar.DAY_OF_MONTH)
        val startHour = currentDateTime.get(Calendar.HOUR_OF_DAY)
        val startMinute = currentDateTime.get(Calendar.MINUTE)

        DatePickerDialog(context, R.style.DialogTheme, { _, year, monthOfYear, dayOfMonth ->
            TimePickerDialog(context, R.style.DialogTheme, { _, hourOfDay, minute ->
                updateStartTime(year, monthOfYear, dayOfMonth, hourOfDay, minute)
            }, startHour, startMinute, false).show()
        }, startYear, startMonth, startDay).show()
    }
    Text(text = "Description:", style = MaterialTheme.typography.body1, fontWeight = FontWeight.Bold)
    TextField(
        value = editedDescription.value,
        onValueChange = { editedDescription.value = it }
    )
    Spacer(Modifier.height(12.dp))
    val formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy h:mma")
    Text("Start Time:", fontWeight = FontWeight.Bold)
    OutlinedTextField(
        value = startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
        onValueChange = { },
        modifier = Modifier.fillMaxWidth(),
        readOnly = true,
        trailingIcon = {
            IconButton(onClick = { showDateTimePicker() }) {
                Icon(Icons.Filled.DateRange, contentDescription = "Select Start Time")
            }
        }
    )
    Spacer(Modifier.height(12.dp))
    Text("End Time:", fontWeight = FontWeight.Bold)
    OutlinedTextField(
        value = endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
        onValueChange = { },
        modifier = Modifier.fillMaxWidth(),
        readOnly = true,
        trailingIcon = {
            IconButton(onClick = { showDateTimePicker() }) {
                Icon(Icons.Filled.DateRange, contentDescription = "Select End Time")
            }
        }
    )
    editedStartTime.value = startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    editedEndTime.value = endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    Spacer(Modifier.height(12.dp))
    // Initialize Places if not already done
    if (!Places.isInitialized()) {
        Places.initialize(context, context.getString(R.string.google_maps_key))
    }

    val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS)

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val place = Autocomplete.getPlaceFromIntent(result.data!!)
            editedLocation.value = place.address ?: ""
        }
    }
    // Open Google Places Autocomplete
    fun openPlacesAutocomplete() {
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
            .build(context)
        launcher.launch(intent)
    }
    Text("Location:", fontWeight = FontWeight.Bold)
    OutlinedTextField(
        value = editedLocation.value,
        onValueChange = { editedLocation.value = it },
        modifier = Modifier.fillMaxWidth(),
        readOnly = true,  // Location is read-only. To edit have to click magnifying glass
        trailingIcon = {
            IconButton(onClick = { openPlacesAutocomplete() }) {
                Icon(Icons.Filled.Search, contentDescription = "Search Location")
            }
        }
    )
    Spacer(Modifier.height(12.dp))
    Row(
        modifier = Modifier.padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = "Participants: ",
            style = MaterialTheme.typography.body1,
            fontWeight = FontWeight.Bold
        )
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = eventDetails.participants.joinToString(", "),
            style = MaterialTheme.typography.body1
        )
    }
    Spacer(Modifier.height(12.dp))
    Row(
        modifier = Modifier.padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = "Allergies: ",
            style = MaterialTheme.typography.body1,
            fontWeight = FontWeight.Bold
        )
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = allergies.value.joinToString(", "),
            style = MaterialTheme.typography.body1
        )
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

private suspend fun getParticipantAllergies(eventId: String): List<String> {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

    Log.d("EventDetails", "getParticipantAllergies called with eventID: $eventId")

    return try {
        val userEndpoint = "http://${GlobalVariables.localIP}:5050/getParticipantAllergies?eventID=$eventId"
        val response: HttpResponse = withContext(Dispatchers.IO) {
            client.get(userEndpoint) {
                contentType(ContentType.Application.Json)
            }
        }
        if (response.status == HttpStatusCode.OK || response.status == HttpStatusCode.Accepted) {
            Log.d("EventDetails", "Successfully retrieved participant allergies")
            val jsonArrayString = response.bodyAsText()
            Json.decodeFromString<List<String>>(jsonArrayString)
        }
        else{
            Log.d("EventDetails", "Failed to retrieve participant allergies: ${response.bodyAsText()} ${response.status}")
            listOf()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        listOf()
    } finally {
        client.close()
    }
}