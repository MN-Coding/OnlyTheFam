package com.example.onlythefam

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.onlythefam.ui.theme.Blue500

@RequiresApi(Build.VERSION_CODES.O)
@Composable
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
        var name by remember { mutableStateOf("John Doe") }
        val email by remember { mutableStateOf("johndoe@gmail.com") }
        var dob by remember { mutableStateOf("November 4th, 2001") }
        var bloodType by remember { mutableStateOf("O-") }
        var allergies by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            EditableTextField(fieldName = "Name", fieldVal = name, onChange = {updatedName -> name = updatedName})
            EditableTextField(fieldName = "Date of Birth", fieldVal = dob, onChange = {updatedDob -> dob = updatedDob})
            StaticUserProfileField(fieldName = "Email", fieldVal = email)
            Spacer(modifier = Modifier.height(5.dp))
            Text("Health", fontSize = 21.sp)
            Spacer(modifier = Modifier.height(16.dp))
            EditableTextField(fieldName = "Blood Type", fieldVal = bloodType, onChange = {updatedBT -> bloodType = updatedBT})
            EditableTextField(fieldName = "Allergies", fieldVal = allergies, onChange = {updatedAllergies -> allergies = updatedAllergies})
        }
    }
}

//@Composable
//fun EditableUserProfileField(fieldName: String, fieldVal: String, onChange: (String) -> Unit){
//    var inEditMode by remember { mutableStateOf(false)}
//    Row(
//        verticalAlignment = Alignment.CenterVertically,
//        modifier = Modifier.fillMaxWidth()
//    ) {
//        if (inEditMode) {
//            Column (
//                modifier = Modifier
//                    .weight(1f)
//            ) {
//                OutlinedTextField(
//                    value = fieldVal,
//                    onValueChange = onChange,
//                    label = { Text("Edit $fieldName") },
//                    singleLine = true,
//                    modifier = Modifier.fillMaxWidth()
//                )
//            }
//        } else {
//            Column (
//                modifier = Modifier
//                    .weight(1f)
//                    .wrapContentWidth(Alignment.Start)
//            )
//            {
//                Text(fieldName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
//                OutlinedTextField(
//                    value = fieldVal,
//                    onValueChange = {},
//                    readOnly = true,
//                    singleLine = true,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .align(Alignment.CenterHorizontally)
//                )
//            }
//        }
//        IconButton(onClick = { inEditMode = !inEditMode }) {
//            Icon(
//                imageVector = if (inEditMode) Icons.Filled.Check else Icons.Filled.Edit,
//                contentDescription = if (inEditMode) "Save" else "Edit"
//            )
//        }
//    }
//    Spacer(modifier = Modifier.height(18.dp))
//}

@Composable
fun StaticUserProfileField(fieldName: String, fieldVal: String){
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .wrapContentWidth(Alignment.Start)
        ) {
            Text(fieldName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
        Spacer(modifier = Modifier.size(48.dp, 48.dp)) //placeholder for blank icon to balance
    }
    Spacer(modifier = Modifier.height(18.dp))
}