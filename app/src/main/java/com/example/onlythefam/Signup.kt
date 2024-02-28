package com.example.onlythefam

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

sealed class SignUpPhase {
    object Phase1 : SignUpPhase()
    object Phase2 : SignUpPhase()
    object Phase3 : SignUpPhase()
}

// Email, Name, Password, DOB
@Composable
fun SignUpPhase1(onNextPhase: () -> Unit, gotologin: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp), horizontalAlignment = Alignment.Start) {
        Text(
            "(1) Create your account",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = dob,
                onValueChange = { dob = it },
                label = { Text("Birthday") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = onNextPhase, modifier = Modifier.fillMaxWidth()) {
                Text(text = "Next")
            }
        }
    }

}

// Family Info + Share Location
@Composable
fun SignUpPhase2(onNextPhase: () -> Unit, onPreviousPhase: () -> Unit) {

    var startNew by remember { mutableStateOf(true) }
    var shareLocation by remember { mutableStateOf(false) }
    var familyId by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
    ) {
        Text(
            "(2) Join your Family",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Row() {
            Text(
                "Creating a new family?",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.LightGray
            )
            Switch(
                checked = startNew,
                onCheckedChange = { startNew = it },
//                modifier = Modifier.padding(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Is your Family already on OnlyTheFam? Join Them!",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.LightGray
        )
        OutlinedTextField(
            value = familyId,
            onValueChange = { familyId = it },
            label = { Text("Family ID") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Share Location",
                fontSize = 16.sp,
                color = Color.LightGray,
                modifier = Modifier
                    .weight(1f)
            )
            Switch(
                checked = shareLocation,
                onCheckedChange = { shareLocation = it },
//                modifier = Modifier.padding(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = onPreviousPhase) {
                Text(text = "Back")
            }
            Button(onClick = onNextPhase) {
                Text(text = "Next")
            }
        }

    }
}

// Allergies, Blood Type, Health Facts
@Composable
fun SignUpPhase3(onPreviousPhase: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Sign Up - Phase 3")
    }
}

@Composable
fun SignUpBottomBar(gotologin: () -> Unit) {
    Column(
        modifier = Modifier
            .height(100.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            buildAnnotatedString {
                append("Back to ")
                withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline)) {
                    append("Login")
                }
            },
            modifier = Modifier
                .clickable { gotologin() }
        )
    }
}

@Composable
fun ProgressBar(currentPhase: SignUpPhase) {
    val progress = when (currentPhase) {
        SignUpPhase.Phase1 -> 0f
        SignUpPhase.Phase2 -> 0.5f
        SignUpPhase.Phase3 -> 1f
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = MaterialTheme.colors.background,
            color = MaterialTheme.colors.secondary
        )
        Spacer(modifier = Modifier.height(80.dp))
        Image(
            painter = painterResource(R.drawable.minilogo),
            contentDescription = "My Image",
            modifier = Modifier
                .height(100.dp)
                .aspectRatio(1f)
        )
        Spacer(modifier = Modifier.height(80.dp))
    }

}

@Composable
fun SignUpFlow(onsignup: () -> Unit, gotologin: () -> Unit) {
    var currentPhase by remember { mutableStateOf<SignUpPhase>(SignUpPhase.Phase1) }

    Scaffold(
        topBar = { ProgressBar(currentPhase) },
        bottomBar = { SignUpBottomBar(gotologin)}
    ) {
        when (currentPhase) {
            SignUpPhase.Phase1 -> SignUpPhase1( { currentPhase = SignUpPhase.Phase2 }, gotologin)
            SignUpPhase.Phase2 -> SignUpPhase2( { currentPhase = SignUpPhase.Phase3}, { currentPhase = SignUpPhase.Phase1 })
            SignUpPhase.Phase3 -> SignUpPhase3( { currentPhase = SignUpPhase.Phase2 } )
        }
    }
}

// need to update to verify
// finish adding other info
private fun signUp(email: String, password: String, scaffoldState: ScaffoldState, coroutineScope: CoroutineScope, redirectOnSignup: () -> Unit) {
    coroutineScope.launch {
        try {
            val query = """
            INSERT INTO users (email, password) VALUES (?, ?)
            """.trimIndent()
            val result = GlobalVariables.db.executeUpdate(query, arrayOf(email, password))
            if (result == -1) {
                throw Exception("DB Update Failed.")
            }
            scaffoldState.snackbarHostState.showSnackbar("Sign-up successful")
            redirectOnSignup()
        } catch (e: Exception) {
            scaffoldState.snackbarHostState.showSnackbar("Sign-up failed: ${e.message}")
        }
    }
}
