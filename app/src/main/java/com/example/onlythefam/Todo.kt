package com.example.onlythefam

import android.R
import android.os.Build
import android.widget.Spinner
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
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
import androidx.compose.material.ButtonColors
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import com.example.onlythefam.ui.theme.Blue700


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TodoPage() {
    val scrollState = rememberScrollState()
    Scaffold (
        topBar = { Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                "Add New Task",
                color= Blue700,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                style = TextStyle(fontSize = 36.sp)
            )
        }
        }
    ) {
        var taskName by remember { mutableStateOf("Enter task description") }
        var budgetamount by remember { mutableStateOf("$ Enter amount") }
        var note by remember { mutableStateOf("Any additional comments") }
        var deadlinedaytemplate by remember { mutableStateOf("MM/DD/YYYY") }
        var deadlinetimetemplate by remember { mutableStateOf("HH:MM AM/PM") }


        Column(
            modifier = Modifier
                .padding(20.dp, 10.dp)
                .verticalScroll(scrollState, enabled = true),
        ) {
            Text(
                "* Task:",
                color= Color.Red,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                style = TextStyle(fontSize = 20.sp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = taskName, onValueChange = { taskName = it },
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(5f)
                )
                Spacer(modifier = Modifier.width(5.dp))
                IconButton(onClick = { /*TODO*/ },
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = "Edit"
                    )
                }
            }
            Spacer(Modifier.height(10.dp))

            Text(
                "* Assigned to:",
                color= Color.Red,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                style = TextStyle(fontSize = 20.sp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = "", onValueChange = { "" },
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(5f)
                )
                Spacer(modifier = Modifier.width(5.dp))
                IconButton(onClick = { /*TODO*/ },
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = "Edit"
                    )
                }
            }
            Spacer(Modifier.height(10.dp))

            Text(
                "Event:",
                color=Blue700,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                style = TextStyle(fontSize = 20.sp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = "", onValueChange = { /*TODO*/  },
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(5f)
                )
                Spacer(modifier = Modifier.width(5.dp))
                IconButton(onClick = { /*TODO*/ },
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = "Edit"
                    )
                }
            }
            Spacer(Modifier.height(10.dp))

            Text("Budget:",color=Blue700, fontWeight= FontWeight.Bold,style = TextStyle(fontSize = 20.sp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = budgetamount, onValueChange = { budgetamount = it },
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(5f)
                )
                Spacer(modifier = Modifier.width(5.dp))
                IconButton(onClick = { /*TODO*/ },
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = "Edit"
                    )
                }
            }
            Spacer(Modifier.height(10.dp))

            Text("* Deadline:",color=Color.Red,fontWeight= FontWeight.Bold,style = TextStyle(fontSize = 20.sp))
            Text("Date:",color=Blue700 ,fontWeight= FontWeight.Bold,style = TextStyle(fontSize = 15.sp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = deadlinedaytemplate, onValueChange = { deadlinedaytemplate = it },
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(5f)
                )
                Spacer(modifier = Modifier.width(5.dp))
                IconButton(onClick = { /*TODO*/ },
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = "Edit"
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Text("Time:",color=Blue700 ,fontWeight= FontWeight.Bold,style = TextStyle(fontSize = 15.sp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = deadlinetimetemplate, onValueChange = { deadlinetimetemplate = it },
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(5f)
                )
                Spacer(modifier = Modifier.width(5.dp))
                IconButton(onClick = { /*TODO*/ },
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = "Edit"
                    )
                }
            }
            Spacer(Modifier.height(10.dp))

            var checked by remember { mutableStateOf(true) }
            Text("Set reminder?",color=Blue700,fontWeight= FontWeight.Bold,style = TextStyle(fontSize = 20.sp))
            Row(
               // horizontalArrangement = Arrangement.Start,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.width(30.dp))
                Switch(
                    checked = checked,
                    onCheckedChange = {
                    checked = it
                    })
            }

            if (checked) {
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    /*TODO*/
                    Row(
                        //horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    {
                        Text("Start:", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("End:", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(10.dp))
                    }
                }
            }
            Spacer(Modifier.height(10.dp))

            //notes
            Text("Notes:",color=Blue700,fontWeight= FontWeight.Bold,style = TextStyle(fontSize = 20.sp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = note, onValueChange = { note = it },
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(5f)
                )
                Spacer(modifier = Modifier.width(5.dp))
                IconButton(onClick = { /*TODO*/ },
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = "Edit"
                    )
                }
            }
            Spacer(Modifier.height(10.dp))

            // buttons
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Button(onClick = { /*TODO*/ }) {
                    Text("Cancel Draft")
                }
                Spacer(modifier = Modifier.width(16.dp))

                Button(onClick = { /*TODO*/ }) {
                    Text("Schedule Task")
                }
            }

        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview
@Composable
fun TodoPreview(){
    TodoPage()
}
