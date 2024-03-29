package com.example.onlythefam

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
@Composable
fun AddItemDialogListField(fieldName: String, initialFieldVal: String, onNewItemsAdded: (List<String>) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    var textState by remember { mutableStateOf("") } // Always start with an empty field in the dialog
    val itemsList = remember { mutableStateListOf<String>() }
    val newlyAddedItems = remember { mutableStateListOf<String>() }

    // Initialize itemsList from initialFieldVal if it's not empty, but only once
    LaunchedEffect(initialFieldVal) {
        if (initialFieldVal.isNotEmpty() && itemsList.isEmpty()) {
            itemsList.addAll(initialFieldVal.split(", ").filter { it.isNotBlank() })
        }
    }

    // Dialog for adding a new item
    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                if (newlyAddedItems.isNotEmpty()) {
                    onNewItemsAdded(newlyAddedItems.toList()) // Pass only the newly added items
                    newlyAddedItems.clear() // Clear list of newly added items for the next use
                }
                showDialog = false
                textState = "" // text field is empty for the next input
            },
            title = { Text(text = "Add $fieldName") },
            text = {
                OutlinedTextField(
                    value = textState,
                    onValueChange = { textState = it },
                    label = { Text("Enter $fieldName") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (textState.isNotEmpty()) {
                            itemsList.add(textState) // Add to the total list of items
                            newlyAddedItems.add(textState) // Track newly added item
                            textState = "" // Reset field for the next input without closing dialog
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier
            .weight(1f)
            .wrapContentWidth(Alignment.Start)
        ) {
            Text(fieldName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            OutlinedTextField(
                value = itemsList.joinToString(", "),
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
        IconButton(onClick = { showDialog = true }) {
            Icon(
                imageVector = Icons.Default.AddCircle,
                contentDescription = "Add"
            )
        }
    }
    Spacer(modifier = Modifier.height(10.dp))
}