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
