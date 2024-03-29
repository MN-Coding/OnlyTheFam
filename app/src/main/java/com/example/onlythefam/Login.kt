package com.example.onlythefam

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.util.Log
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.FileInputStream
import java.io.File
import java.util.*

val loading = mutableStateOf(false)

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
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
                visualTransformation = PasswordVisualTransformation(),
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
            if (loading.value) {
                CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
            } else {
                Spacer(modifier = Modifier.height(16.dp))
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

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class LoginResponse(val userID: String, val name: String)

private fun signIn(email: String, password: String, scaffoldState: ScaffoldState, coroutineScope: CoroutineScope, onlogin: () -> Unit) {
    loading.value = true
    CoroutineScope(Dispatchers.Main).launch {
        val loginEndpoint = "http://${GlobalVariables.localIP}:5050/login"
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json()
            }
        }

        try {
            val response: HttpResponse = client.post(loginEndpoint) {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(email, password))
            }
            client.close()
            val status = response.status
            if (status != HttpStatusCode.Accepted) {
                loading.value = false
                throw Exception("Error " + status.value.toString())
            }
            val responseBody = Json.decodeFromString<LoginResponse>(response.bodyAsText())
            GlobalVariables.userId = responseBody.userID
            GlobalVariables.username = responseBody.name
            scaffoldState.snackbarHostState.showSnackbar("Sign-in successful")
            loading.value = false
            onlogin()
        } catch (e: Exception) {
            e.printStackTrace()
            scaffoldState.snackbarHostState.showSnackbar("Sign-in failed: ${e.message}")
            loading.value = false
        }
        loading.value = false
    }
}