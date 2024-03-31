package com.example.onlythefam

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.ContentValues.TAG
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.ui.focus.onFocusChanged
import java.time.LocalDate
import java.util.*
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.onlythefam.ui.theme.Blue200
import com.example.onlythefam.ui.theme.Blue700
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import androidx.compose.material.TextField
import io.ktor.client.call.*
import io.ktor.util.*
import kotlinx.serialization.Serializable
import kotlin.text.isLowerCase

val errorFontSize = 14.sp
val load = mutableStateOf(false)

class Credentials : ViewModel() {
    var name by mutableStateOf("")
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var confirmPassword by mutableStateOf("")
    var dob: LocalDate? by mutableStateOf(null)
    var startNewFamily by mutableStateOf(true)
    var shareLocation by mutableStateOf(false)
    var familyId by mutableStateOf("")
    var bloodType by mutableStateOf("")
    var allergies = mutableListOf<String>()
    var otherHealthFacts by mutableStateOf("")
}

sealed class SignUpPhase {
    object Phase1 : SignUpPhase()
    object Phase2 : SignUpPhase()
    object Phase3 : SignUpPhase()
}

private fun verifyEmail(
    email: String,
    scaffoldState: ScaffoldState,
    coroutineScope: CoroutineScope,
    onNextPhase: () -> Unit
) {
    load.value = true
    CoroutineScope(Dispatchers.IO).launch {
        val userEndpoint = "http://${GlobalVariables.localIP}:5050/emailExists?email=$email"
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json()
            }
        }

        try {
            val response: HttpResponse = client.get(userEndpoint) {
                contentType(ContentType.Application.Json)
            }
            client.close()
            load.value = false
            val status = response.status
            if (status == HttpStatusCode.NotFound) {
                onNextPhase()
            } else {
                scaffoldState.snackbarHostState.showSnackbar("An account with this email already exists. Please sign in.")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        load.value = false
    }

}

fun isValidEmail(email: String): Boolean {
    val emailRegex = Regex("[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
    return emailRegex.matches(email)
}

fun isValidPassword(password: String): Boolean {
    val minLength = 8
    val hasUpperCase = password.any { it.isUpperCase() }
    val hasLowerCase = password.any { it.isLowerCase() }
    val hasDigit = password.any { it.isDigit() }
    val hasSpecialChar = password.any { !it.isLetterOrDigit() }

    return password.length >= minLength &&
            hasUpperCase &&
            hasLowerCase &&
            hasDigit &&
            hasSpecialChar
}

// Email, Name, Password, DOB
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SignUpPhase1(onNextPhase: () -> Unit, gotologin: () -> Unit, c: Credentials) {
    val errorSpace = 35.dp
    val context = LocalContext.current
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    var nameEdited by remember { mutableStateOf(false) }
    var emailEdited by remember { mutableStateOf(false) }
    var passwordEdited by remember { mutableStateOf(false) }
    var confirmPasswordEdited by remember { mutableStateOf(false) }

    load.value = false

    val isComplete by remember {
        derivedStateOf {
            c.name.isNotBlank()
                    && c.email.isNotBlank()
                    && c.password.isNotBlank()
                    && c.password == c.confirmPassword
                    && c.dob != null
                    && nameError == null
                    && emailError == null
                    && passwordError == null
                    && confirmPasswordError == null
        }
    }

    fun updateDate(year: Int, month: Int, day: Int) {
        c.dob = LocalDate.of(year, month + 1, day)
    }

    // Show day + time picker
    fun showDatePicker() {
        val currentDateTime = Calendar.getInstance()
        var y = 2000
        var m = 0
        var d = 1

        if (c.dob != null) {
            y = c.dob!!.year
            m = c.dob!!.monthValue - 1
            d = c.dob!!.dayOfMonth
        }
        DatePickerDialog(context, R.style.DialogTheme, { _, year, monthOfYear, dayOfMonth ->
            updateDate(year, monthOfYear, dayOfMonth)
        }, y, m, d).show()
    }

    Scaffold(scaffoldState = scaffoldState) {
        Column(Modifier.fillMaxSize().padding(horizontal = 16.dp), horizontalAlignment = Alignment.Start) {
            Text(
                "(1) Create your account",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(10.dp))
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = c.name,
                    onValueChange = { c.name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().onFocusChanged {
                        if (it.isFocused) {
                            nameEdited = true
                        }
                        if (!it.isFocused && nameEdited) {
                            nameError = if (c.name.isBlank()) "Name cannot be empty" else null
                        }
                    }
                )
                Column(
                    modifier = Modifier.height(errorSpace).fillMaxWidth(),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Center
                ) {
                    nameError?.let { Text(it, color = MaterialTheme.colors.error, fontSize = errorFontSize) }
                }

                OutlinedTextField(
                    value = c.email,
                    onValueChange = { c.email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().onFocusChanged {
                        if (it.isFocused) {
                            emailEdited = true
                        }
                        if (!it.isFocused && emailEdited) {
                            emailError = if (!isValidEmail(c.email)) "Invalid email" else null
                        }
                    }
                )

                Column(
                    modifier = Modifier.height(errorSpace).fillMaxWidth(),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Center
                ) {
                    emailError?.let { Text(it, color = MaterialTheme.colors.error, fontSize = errorFontSize) }
                }

                OutlinedTextField(
                    value = c.password,
                    onValueChange = { c.password = it },
                    label = { Text("Password") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().onFocusChanged {
                        if (it.isFocused) {
                            passwordEdited = true
                        }
                        if (!it.isFocused && passwordEdited) {
                            passwordError =
                                if (!isValidPassword(c.password)) "Ensure password has >= 8 characters (> 1 uppercase, 1 lowercase, 1 special) and >= 1 digit" else null
                        }
                    }
                )

                Column(
                    modifier = Modifier.height(errorSpace).fillMaxWidth(),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Center
                ) {
                    passwordError?.let {
                        Text(
                            it,
                            color = MaterialTheme.colors.error,
                            fontSize = errorFontSize,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )
                    }
                }

                OutlinedTextField(
                    value = c.confirmPassword,
                    onValueChange = { c.confirmPassword = it },
                    label = { Text("Confirm Password") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().onFocusChanged {
                        if (it.isFocused) {
                            confirmPasswordEdited = true
                        }
                        if (!it.isFocused && passwordEdited) {
                            confirmPasswordError =
                                if (c.password != c.confirmPassword) "Passwords do not match" else null
                        }
                    }
                )

                Column(
                    modifier = Modifier.height(errorSpace).fillMaxWidth(),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Center
                ) {
                    confirmPasswordError?.let { Text(it, color = MaterialTheme.colors.error, fontSize = errorFontSize) }
                }

                OutlinedTextField(
                    value = if (c.dob is LocalDate) c.dob!!.format(
                        DateTimeFormatter.ofPattern(
                            "MMMM d, yyyy",
                            Locale.ENGLISH
                        )
                    ) else "",
                    onValueChange = { },
                    label = { Text("Birthday") },
                    maxLines = 1,
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker() }) {
                            Icon(Icons.Filled.DateRange, contentDescription = "Birthday")
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        verifyEmail(c.email, scaffoldState, coroutineScope, onNextPhase)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isComplete
                ) {
                    Text(text = "Next")
                }
            }
        }
    }

}

fun checkIfCanJoinFamily(
    familyId: String,
    scaffoldState: ScaffoldState,
    coroutineScope: CoroutineScope,
    onNextPhase: () -> Unit
) {
    load.value = true
    CoroutineScope(Dispatchers.IO).launch {
        val userEndpoint = "http://${GlobalVariables.localIP}:5050/familyExists?familyId=$familyId"
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json()
            }
        }

        try {
            val response: HttpResponse = client.get(userEndpoint) {
                contentType(ContentType.Application.Json)
            }
            client.close()
            load.value = false
            val status = response.status
            if (status == HttpStatusCode.OK) {
                onNextPhase()
            } else {
                scaffoldState.snackbarHostState.showSnackbar("Family ID does not exist.")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        load.value = false
    }
}

private fun checkIfCanCreateFamily(
    familyId: String,
    scaffoldState: ScaffoldState,
    coroutineScope: CoroutineScope,
    onNextPhase: () -> Unit
) {
    load.value = true
    coroutineScope.launch(Dispatchers.IO) {
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json()
            }
        }
        val url = "http://${GlobalVariables.localIP}:5050/familyExists?familyId=$familyId"

        try {
            val response: HttpResponse = client.get(url) {
                contentType(ContentType.Application.Json)
            }
            val status = response.status
            if (status == HttpStatusCode.NotFound) {
                withContext(Dispatchers.Main) {
                    onNextPhase()
                }
            } else {
                withContext(Dispatchers.Main) {
                    scaffoldState.snackbarHostState.showSnackbar("A family with this ID already exists.")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            load.value = false
            client.close()
        }
    }
}

// Family Info + Share Location
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun SignUpPhase2(onNextPhase: () -> Unit, onPreviousPhase: () -> Unit, c: Credentials) {

    val coroutineScope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()

    val isComplete by remember {
        derivedStateOf {
            c.startNewFamily || c.familyId.isNotBlank()
        }
    }

    load.value = false
    Scaffold(scaffoldState = scaffoldState) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
        ) {
            Text(
                "(2) Join your Family!",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Starting a new Family?",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .weight(1f)
                )
                Switch(
                    checked = c.startNewFamily,
                    onCheckedChange = { c.startNewFamily = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Blue200,
                        checkedTrackColor = Blue200.copy(alpha = 0.5f),
                    ),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Is your Family already on OnlyTheFam? Join Them!",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )
            OutlinedTextField(
                value = c.familyId,
                onValueChange = { c.familyId = it },
                enabled = true,
                label = { Text("Family ID") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Share Location",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Gray,
                    modifier = Modifier
                        .weight(1f)
                )
                Switch(
                    checked = c.shareLocation,
                    onCheckedChange = { c.shareLocation = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Blue200,
                        checkedTrackColor = Blue200.copy(alpha = 0.5f),
                    ),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = {
                    load.value = true
                    onPreviousPhase()
                }) {
                    Text(text = "Back")
                }
                Button(
                    enabled = isComplete,
                    onClick = {
                        load.value = true
                        if (!c.startNewFamily) {
                            checkIfCanJoinFamily(c.familyId, scaffoldState, coroutineScope, onNextPhase)
                        } else {
//                            checkIfCanCreateFamily(c.familyId, scaffoldState, coroutineScope, onNextPhase)
                            load.value = false
                            onNextPhase()
                        }
                    }) {
                    Text(text = "Next")
                }
            }

        }
    }

}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Dropdown(title: String, c: Credentials) {
    val context = LocalContext.current
    val bloodTypes = arrayOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
    var expanded by remember { mutableStateOf(false) }
    var selectedText by remember { mutableStateOf(c.bloodType.ifBlank { "not selected" }) }

    Text(
        title,
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        color = Color.Gray
    )
    Spacer(modifier = Modifier.height(5.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = {
                expanded = !expanded
            }
        ) {
            TextField(
                value = selectedText,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                bloodTypes.forEach { item ->
                    DropdownMenuItem(
                        onClick = {
                            selectedText = item
                            c.bloodType = item
                            expanded = false
                            Toast.makeText(context, item, Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text(text = item)
                    }
                }
            }
        }
    }
}

// Allergies, Blood Type, Health Facts
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SignUpPhase3(onPreviousPhase: () -> Unit, onsignup: () -> Unit, c: Credentials) {
    val coroutineScope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()

    var allergiesString by remember { mutableStateOf("") }

    load.value = false
    Scaffold(scaffoldState = scaffoldState) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
        ) {
            Text(
                "(3) Final Step: Let them know any important health information.",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                "Allergies/Dietary Restrictions",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(5.dp))
            TextField(
                value = allergiesString,
                onValueChange = { allergiesString = it },
                placeholder = { Text("Peanuts, Seafood, etc.") },
                label = {Text("Allergies/Dietary Restrictions")},
                maxLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Dropdown(title = "Blood Type", c)

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Any other health facts...",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(5.dp))
            TextField(
                value = c.otherHealthFacts,
                onValueChange = { c.otherHealthFacts = it },
                placeholder = { Text("Asthma, Risk of Falling, etc.") },
                label = {Text("Any other health facts...")},
                maxLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = {
                    load.value = true
                    onPreviousPhase()
                }) {
                    Text(text = "Back")
                }
                Button(
                    onClick = {
                        load.value = true
                        c.allergies += allergiesString.split(",").map { it.trim() }
                        signUp(c, coroutineScope, scaffoldState, onsignup)
                    }) {
                    Text(text = "Sign Up")
                }
            }

        }
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
        Spacer(modifier = Modifier.height(20.dp))
        if (load.value) {
            CircularProgressIndicator(modifier = Modifier.size(100.dp), strokeWidth = 8.dp)
        } else {
            Image(
                painter = painterResource(R.drawable.minilogo),
                contentDescription = "My Image",
                modifier = Modifier
                    .height(100.dp)
                    .aspectRatio(1f)
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
    }

}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SignUpFlow(onsignup: () -> Unit, gotologin: () -> Unit) {
    var currentPhase by remember { mutableStateOf<SignUpPhase>(SignUpPhase.Phase1) }
    val sharedViewModel = viewModel<Credentials>()

    Scaffold(
        topBar = { ProgressBar(currentPhase) },
        bottomBar = { SignUpBottomBar(gotologin) }
    ) {
        when (currentPhase) {
            SignUpPhase.Phase1 -> SignUpPhase1({ currentPhase = SignUpPhase.Phase2 }, gotologin, sharedViewModel)
            SignUpPhase.Phase2 -> SignUpPhase2(
                { currentPhase = SignUpPhase.Phase3 },
                { currentPhase = SignUpPhase.Phase1 },
                sharedViewModel
            )

            SignUpPhase.Phase3 -> SignUpPhase3({ currentPhase = SignUpPhase.Phase2 }, onsignup, sharedViewModel)
        }
    }
}

@Serializable
data class UserSignup(
    val name: String,
    val email: String,
    val password: String,
    val dobstr: String,
    val startNewFamily: Boolean,
    val familyId: String,
    val locationSharing: Boolean,
    val allergies: List<String>,
    val bloodType: String,
    val otherHealth: String
)

@OptIn(InternalAPI::class)
@RequiresApi(Build.VERSION_CODES.O)
private fun signUp(
    c: Credentials,
    coroutineScope: CoroutineScope,
    scaffoldState: ScaffoldState,
    redirectOnSignup: () -> Unit
) {

    load.value = true
    CoroutineScope(Dispatchers.Main).launch {
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json()
            }
        }

        val url = "http://${GlobalVariables.localIP}:5050/signup"

        try {
            val response: HttpResponse = client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(UserSignup(
                    name = c.name,
                    email = c.email,
                    password = c.password,
                    startNewFamily = c.startNewFamily,
                    familyId = c.familyId,
                    locationSharing = c.shareLocation,
                    allergies = c.allergies,
                    bloodType = c.bloodType,
                    otherHealth = c.otherHealthFacts,
                    dobstr = if (c.dob is LocalDate) c.dob!!.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) else ""
                ))
            }
            if (response.status == HttpStatusCode.OK) {
                load.value = false
                val responseBody = Json.decodeFromString<LoginResponse>(response.bodyAsText())
                GlobalVariables.userId = responseBody.userID
                GlobalVariables.username = responseBody.name
                // log each global variable
                Log.d(TAG, "signUp: ${GlobalVariables.userId}")
                Log.d(TAG, "signUp: ${GlobalVariables.username}")
                redirectOnSignup()
            } else {
                scaffoldState.snackbarHostState.showSnackbar(response.bodyAsText())
            }
            load.value = false
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            load.value = false
            client.close()
        }
    }

}
