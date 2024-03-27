package com.example.onlythefam

import android.annotation.SuppressLint
import android.app.Activity
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
import androidx.compose.material.*
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import java.time.format.DateTimeFormatter
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.navigation.NavController
import java.util.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
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
data class SubmitEventRequest(val eventID: String, val name: String, val description: String, val startDatetime: String, val endDatetime: String, val location: String, val participants: List<String>)

suspend fun submitEvent(eventName: String, description: String, startDateTime: String, endDateTime: String, location: String, participantString: String): Boolean {

    // split participantString into a list of participants
    val participants = participantString.split(",").map { it.trim() }

    val submitEventEndpoint = "http://${GlobalVariables.localIP}:5050/addevent"
    Log.d("SubmitEvent", "Endpoint: $submitEventEndpoint")
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

    val eventID = "event" + UUID.randomUUID()
    Log.d("SubmitEvent", "Generated Event ID: $eventID")

    try {
        Log.d("SubmitEvent", "Attempting to submit event: $eventName")
        val response: HttpResponse = client.post(submitEventEndpoint) {
            contentType(ContentType.Application.Json)
            setBody(SubmitEventRequest(eventID, eventName, description, startDateTime, endDateTime, location, participants))
        }
        Log.d("SubmitEvent", "Response Status: ${response.status}")

        // Close the client after the request
        client.close()
        val isSuccess = response.status.value in 200..299
        if (isSuccess) {
            Log.d("SubmitEvent", "Event submission successful")
        } else {
            Log.d("SubmitEvent", "Event submission failed with status: ${response.status}")
        }
        return isSuccess
    } catch (e: Exception) {
        Log.e("SubmitEvent", "Exception during event submission", e)
        return false
    }
}

@Composable
fun MultiSelectDropdown(
    options: List<String>,
    selectedOptions: List<String>,
    onSelectionChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedText = if (selectedOptions.isEmpty()) {
        "Select Usernames"
    } else {
        selectedOptions.joinToString()
    }

    Box {
        OutlinedTextField(
            value = selectedText,
            onValueChange = { },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = "Select Usernames")
                }
            }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(onClick = {
                    onSelectionChange(option)
                    expanded = false
                }) {
                    Text(option)
                }
            }
        }
    }
}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter", "UnrememberedMutableState")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddEvent(navController: NavController) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var eventName by remember { mutableStateOf("Enter Event Name") }
    var location by remember { mutableStateOf("Enter Location") }
    var startTime by remember { mutableStateOf(LocalDateTime.now()) }
    var endTime by remember { mutableStateOf(LocalDateTime.now().plusHours(1)) }
    var description by remember { mutableStateOf("") }
    var shareWith by remember { mutableStateOf("") }
    val shareWithList by derivedStateOf { shareWith.split(",").map { it.trim() } }
    val onShareWithChange: (String) -> Unit = { selected ->
        val updatedList = if (selected in shareWithList) {
            shareWithList - selected
        } else if (shareWith.isNotEmpty()) {
            shareWithList + selected
        } else {
            listOf(selected)
        }
        shareWith = updatedList.joinToString(", ")
    }

    val coroutineScope = rememberCoroutineScope()
    var usernameOptions by remember { mutableStateOf(listOf<String>()) }

    LaunchedEffect(key1 = Unit) {
        coroutineScope.launch {
            usernameOptions = getAllUsernames()
        }
    }

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
            EditableTextField(fieldName = "Event Name", fieldVal = eventName, onChange = { updated -> eventName = updated })

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

            Text("Share With: (Select one or more)", fontWeight = FontWeight.Bold)
            MultiSelectDropdown(
                options = usernameOptions,
                selectedOptions = shareWithList,
                onSelectionChange = onShareWithChange
            )

            EditableTextField(fieldName = "Description", fieldVal = description, onChange = { updated -> description = updated })

            Text("Tasks:", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(5.dp))

            Text("Cost Split:", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(5.dp))

            Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                Button(onClick = { navController.navigate("todo_event_screen") }) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(onClick = {

                    println("making post request")

                    coroutineScope.launch {
                        val startTimeFormatted = startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                        val endTimeFormatted = endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                        val success = submitEvent(eventName, description, startTimeFormatted, endTimeFormatted, location, shareWith)
                        if (success) {
                            println("[SUCCESSFUL] SUBMITTING EVENT")
                            navController.navigate("events")

                        } else {
                            println("[FAILED] SUBMITTING EVENT")

                        }
                    }
                }) {
                    Text("Create")
                }
            }
        }
    }
}
