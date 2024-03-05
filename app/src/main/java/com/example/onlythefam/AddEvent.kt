package com.example.onlythefam

import android.app.Activity
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.material.*
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import java.time.format.DateTimeFormatter
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.Search
import java.util.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddEvent() {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()

    var eventName by remember { mutableStateOf("Enter Event Name") }
    var location by remember { mutableStateOf("Enter Location") }
    var startTime by remember { mutableStateOf(LocalDateTime.now()) }
    var endTime by remember { mutableStateOf(LocalDateTime.now().plusHours(1)) }
    var description by remember { mutableStateOf("") }
    var shareWith by remember { mutableStateOf("") }

    // Initialize Places if not already done
    if (!Places.isInitialized()) {
        Places.initialize(context, context.getString(R.string.google_maps_key))
    }

    val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS)

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val place = Autocomplete.getPlaceFromIntent(result.data!!)
            location = place.address ?: ""
        }
    }

    // Open Google Places Autocomplete
    fun openPlacesAutocomplete() {
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
            .build(context)
        launcher.launch(intent)
    }

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

        DatePickerDialog(context, { _, year, monthOfYear, dayOfMonth ->
            TimePickerDialog(context, { _, hourOfDay, minute ->
                updateStartTime(year, monthOfYear, dayOfMonth, hourOfDay, minute)
            }, startHour, startMinute, false).show()
        }, startYear, startMonth, startDay).show()
    }

    Scaffold(
        topBar = {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Add Event",
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
            EditableTextField(fieldName = "Event Name", fieldVal = eventName, onChange = {updated -> eventName = updated})

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

            Spacer(Modifier.height(5.dp))

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

            Spacer(Modifier.height(5.dp))

            Text("Location:", fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,  // Location is read-only. To edit have to click magnifying glass
                trailingIcon = {
                    IconButton(onClick = { openPlacesAutocomplete() }) {
                        Icon(Icons.Filled.Search, contentDescription = "Search Location")
                    }
                }
            )

            Spacer(Modifier.height(5.dp))

            EditableTextField(fieldName = "Share With", fieldVal = shareWith, onChange = {updated -> shareWith = updated})

            EditableTextField(fieldName = "Description", fieldVal = description, onChange = {updated -> description = updated})

            Text("Tasks:", fontWeight= FontWeight.Bold)
            Spacer(Modifier.height(5.dp))

            Text("Cost Split:", fontWeight= FontWeight.Bold)
            Spacer(Modifier.height(5.dp))

            Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                Button(onClick = { /*TODO: Implement cancel logic*/ }) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(onClick = { addEvent(eventName, description, startTime.toString(),
                    endTime.toString(), scaffoldState, coroutineScope) }) {
                    Text("Create")
                }
            }
        }
    }
}

@Serializable
data class AddEventRequest(val name: String, val decription: String, val end_datetime: String, val start_datetime: String)

private fun addEvent(name: String, description: String,
                     start_datetime: String, end_datetime: String,
                     scaffoldState: ScaffoldState, coroutineScope: CoroutineScope) {
    coroutineScope.launch {
        val eventEndpoint = "http://${GlobalVariables.localIP}:5050/addEvent"
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json()
            }
        }

        try {
            val response: HttpResponse = client.post(eventEndpoint) {
                contentType(ContentType.Application.Json)
                setBody(AddEventRequest(name, description, start_datetime, end_datetime))
            }
            client.close()
            GlobalVariables.userId = response.bodyAsText()
            scaffoldState.snackbarHostState.showSnackbar("Successfully added event")
        } catch (e: Exception) {
            e.printStackTrace()
            scaffoldState.snackbarHostState.showSnackbar("Failed to create event: ${e.message}")
        }
    }
}