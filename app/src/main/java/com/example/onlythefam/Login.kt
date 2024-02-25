package com.example.onlythefam

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

//still need to complete login feature
@Composable
fun LoginScreen(username: String,
                password: String,
                onUsernameChange: (String) -> Unit,
                onPasswordChange: (String) -> Unit,
                onLoginSuccess: () -> Unit) {

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = username,
            onValueChange = { onUsernameChange(it) },
            label = { Text("Username") },
            modifier = Modifier.padding(16.dp)
        )
        OutlinedTextField(
            value = password,
            onValueChange = { onPasswordChange(it) },
            label = { Text("Password") },
            modifier = Modifier.padding(16.dp)
        )
        Button(
            onClick = { onLoginSuccess() },
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Login")
        }
    }
}

