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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddEvent() {
    val scrollState = rememberScrollState()
    Scaffold (
        topBar = { Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                "Add Event",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                style = TextStyle(fontSize = 36.sp)
            )
        }
        }
    ) {
        var blank by remember { mutableStateOf("") }
        var eventName by remember { mutableStateOf("Enter Event Name") }
        var location by remember { mutableStateOf("Enter Location") }
        Column(
            modifier = Modifier
                .padding(20.dp, 10.dp)
                .verticalScroll(scrollState, enabled = true),
        ) {
            Text("Event Name:", fontWeight= FontWeight.Bold)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = eventName, onValueChange = { eventName = it },
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
            Spacer(Modifier.height(10.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text("Start:", fontWeight= FontWeight.Bold)
                Spacer(modifier = Modifier.width(16.dp))
                Text("End:", fontWeight= FontWeight.Bold)
            }
            Spacer(Modifier.height(10.dp))

            Text("Location:", fontWeight= FontWeight.Bold)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = location, onValueChange = { location = it },
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
            Spacer(Modifier.height(10.dp))

            Text("Share with:", fontWeight= FontWeight.Bold)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = blank, onValueChange = { blank = it },
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
            Spacer(Modifier.height(10.dp))

            Text("Description:", fontWeight= FontWeight.Bold)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = blank, onValueChange = { blank = it },
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
            Spacer(Modifier.height(10.dp))

            Text("Tasks:", fontWeight= FontWeight.Bold)
            Spacer(Modifier.height(10.dp))

            Text("Cost Split:", fontWeight= FontWeight.Bold)
            Spacer(Modifier.height(10.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Button(onClick = { /*TODO*/ }) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(onClick = { /*TODO*/ }) {
                    Text("Create")
                }
            }

        }
    }
}

/*
fun SettingsPage(onGoBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier
                    .padding(10.dp, 10.dp),
                elevation = 0.dp,
                title = {
                    Text(
                        text = "Profile Settings",
                        style = MaterialTheme.typography.h2,
                        fontSize = 24.sp,
                        color = Color.Black,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onGoBack){
                        Icon(
                            Icons.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = Blue500,
                            modifier = Modifier
                                .size(30.dp)
                                .aspectRatio(1f)
                        )
                    }
                },
                backgroundColor = Color(0x00FFFFFF)
            )
        }
    ){
        var name by remember { mutableStateOf("John") }
        val email by remember { mutableStateOf("johnjames@gmail.com") }
        var dob by remember { mutableStateOf("November 4th, 2001") }
        var bloodType by remember { mutableStateOf("O-") }
        var allergies by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            EditableUserProfileField(fieldName = "Name", fieldVal = name, onChange = {updatedName -> name = updatedName})
            EditableUserProfileField(fieldName = "Date of Birth", fieldVal = dob, onChange = {updatedDob -> dob = updatedDob})
            StaticUserProfileField(fieldName = "Email", fieldVal = email)
            Text("Health", fontSize = 21.sp)
            Spacer(modifier = Modifier.height(16.dp))
            EditableUserProfileField(fieldName = "Blood Type", fieldVal = bloodType, onChange = {updatedBT -> bloodType = updatedBT})
            EditableUserProfileField(fieldName = "Allergies", fieldVal = allergies, onChange = {updatedAllergies -> allergies = updatedAllergies})

        }
    }
}

@Composable
fun EditableUserProfileField(fieldName: String, fieldVal: String, onChange: (String) -> Unit){
    var inEditMode by remember { mutableStateOf(false)}
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (inEditMode) {
            OutlinedTextField(
                value = fieldVal,
                onValueChange = onChange,
                label = { Text("Edit $fieldName") },
                singleLine = true
            )
        } else {
            Column {
                Text(fieldName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                OutlinedTextField(
                    value = fieldVal,
                    onValueChange = {},
                    readOnly = true,
                    singleLine = true
                )
            }
        }
        IconButton(onClick = { inEditMode = !inEditMode }) {
            Icon(
                imageVector = if (inEditMode) Icons.Filled.Check else Icons.Filled.Edit,
                contentDescription = if (inEditMode) "Save" else "Edit"
            )
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
fun StaticUserProfileField(fieldName: String, fieldVal: String){
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Text(fieldName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            OutlinedTextField(
                value = fieldVal,
                onValueChange = {},
                readOnly = true,
                singleLine = true
            )
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
}
 */