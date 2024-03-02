package com.example.onlythefam

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
import androidx.compose.foundation.layout.*
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddEvent() {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var eventName by remember { mutableStateOf("Enter Event Name") }
    var location by remember { mutableStateOf("Enter Location") }
    var startTime by remember { mutableStateOf(LocalDateTime.now()) }
    var endTime by remember { mutableStateOf(LocalDateTime.now().plusHours(1)) }
    var description by remember { mutableStateOf("") }
    var shareWith by remember { mutableStateOf("") }

    fun updateStartTime(year: Int, month: Int, day: Int, hour: Int, minute: Int) {
        startTime = LocalDateTime.of(year, month + 1, day, hour, minute)
    }

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
            Text("Event Name:", fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = eventName,
                onValueChange = { eventName = it },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(5.dp))

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
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(5.dp))

            Text("Share with:", fontWeight= FontWeight.Bold)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = shareWith, onValueChange = { shareWith = it },
                    modifier = Modifier.fillMaxSize().weight(5f)
                )
                Spacer(modifier = Modifier.width(5.dp))
                IconButton(onClick = { /*TODO*/ },
                    modifier = Modifier.fillMaxSize().weight(1f)) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = "Edit"
                    )
                }
            }

            Spacer(Modifier.height(5.dp))

            Text("Description:", fontWeight= FontWeight.Bold)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = description, onValueChange = { description = it },
                    modifier = Modifier.fillMaxSize().weight(5f)
                )
                Spacer(modifier = Modifier.width(5.dp))
                IconButton(onClick = { /*TODO*/ },
                    modifier = Modifier.fillMaxSize().weight(1f)) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = "Edit"
                    )
                }
            }
            Spacer(Modifier.height(5.dp))

            Text("Tasks:", fontWeight= FontWeight.Bold)
            Spacer(Modifier.height(5.dp))

            Text("Cost Split:", fontWeight= FontWeight.Bold)
            Spacer(Modifier.height(5.dp))

            Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                Button(onClick = { /*TODO: Implement cancel logic*/ }) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(onClick = { /*TODO: Implement create event logic*/ }) {
                    Text("Create")
                }
            }
        }
    }
}
