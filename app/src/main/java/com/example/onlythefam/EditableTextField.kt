package com.example.onlythefam

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EditableTextField(fieldName: String, fieldVal: String, onChange: (String) -> Unit){
    var inEditMode by remember { mutableStateOf(false) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (inEditMode) {
            Column (
                modifier = Modifier
                    .weight(1f)
            ) {
                OutlinedTextField(
                    value = fieldVal,
                    onValueChange = onChange,
                    label = { Text("Edit $fieldName") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            Column (
                modifier = Modifier
                    .weight(1f)
                    .wrapContentWidth(Alignment.Start)
            )
            {
                Text(fieldName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                OutlinedTextField(
                    value = fieldVal,
                    onValueChange = {},
                    readOnly = true,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
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
    Spacer(modifier = Modifier.height(10.dp))
}