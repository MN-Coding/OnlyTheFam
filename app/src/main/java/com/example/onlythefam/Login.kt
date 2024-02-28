package com.example.onlythefam

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.sql.ResultSet
import java.sql.SQLException

@Composable
fun LoginScreen(onlogin: () -> Unit, gotosignup: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(scaffoldState = scaffoldState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .padding(30.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(R.drawable.logo),
                    contentDescription = "My Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                )
            }
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next // Change to ImeAction.Next if you want it to go to the next field
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Go // Change to ImeAction.Next if you want it to go to the next field
                ),
                keyboardActions = KeyboardActions(
                    onGo = { signIn(email, password, scaffoldState, coroutineScope, onlogin) }
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { signIn(email, password, scaffoldState, coroutineScope, onlogin) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Sign In")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                buildAnnotatedString {
                    append("New User? ")
                    withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline)) {
                        append("Signup")
                    }
                },
                modifier = Modifier.clickable { gotosignup() }
            )
        }
    }
}

// update for hashing
private fun signIn(email: String, password: String, scaffoldState: ScaffoldState, coroutineScope: CoroutineScope, onlogin: () -> Unit) {
    coroutineScope.launch {
        try {
            val query = """
            SELECT COUNT(*) AS count
            FROM users
            WHERE email = ? AND password = ?;
            """.trimIndent()
            val result = GlobalVariables.db.executeQuery(query, arrayOf(email, password))
            if (result != null) {
                if (result.next()) {
                    val count = result.getInt("count")
                    if (count < 1) {
                        throw Exception("Login Unsuccessful.")
                    }
                }
            } else {
                throw Exception("Database Error.")
            }
            scaffoldState.snackbarHostState.showSnackbar("Sign-in successful")
            onlogin()
        } catch (e: Exception) {
            e.printStackTrace()
            scaffoldState.snackbarHostState.showSnackbar("Sign-in failed: ${e.message}")
        }
    }
}