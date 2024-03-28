package com.example.onlythefam

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.ExposedDropdownMenuDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.onlythefam.ui.theme.Blue500
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.toLocalDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.LocalDate

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@OptIn(ExperimentalMaterialApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SettingsPage(onGoBack: () -> Unit, onLogout: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

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

        var loading by remember { mutableStateOf(true) }
        var name by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var dob by remember { mutableStateOf("") }
        var bloodType by remember { mutableStateOf("") }
        val bloodTypeOptions = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
        var allergies by remember { mutableStateOf("") }
        val uid = GlobalVariables.userId?.replace("\"", "") ?: ""
        var otherHealth by remember { mutableStateOf("") }
        LaunchedEffect(uid){
            if (uid.isNotEmpty()){
                coroutineScope.launch{
                    val userInfoDelegate = async{ getUserInfo(uid) }
                    val allergiesDelegate = async{ getAllergies(uid) }
                    val healthDelegate = async{ getHealthFacts(uid) }

                    val userInfo = userInfoDelegate.await()
                    allergies = allergiesDelegate.await()
                    otherHealth = healthDelegate.await()

                    name = userInfo?.name ?: ""
                    email = userInfo?.email ?: ""
                    bloodType = userInfo?.bloodType ?: ""
                    dob = userInfo?.dob ?: ""

                    loading = false
                }
            }
        }

        if (!loading) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                EditableTextField(
                    fieldName = "Name",
                    fieldVal = name,
                    onChange = {
                        updatedName -> name = updatedName
                        updateName(uid, updatedName, coroutineScope)
                    })
                DateField(
                    uid = uid,
                    fieldName = "Date of Birth",
                    dateVal = dob,
                    onChange = {updatedDob -> dob = updatedDob},
                    coroutineScope = coroutineScope,
                    context = context
                )
                StaticUserProfileField(fieldName = "Email", fieldVal = email)
                Spacer(modifier = Modifier.height(5.dp))
                Text("Health", fontSize = 21.sp)
                Spacer(modifier = Modifier.height(16.dp))
                ProfileDropdownMenu(
                    menuName = "Blood Type",
                    menuOptions = bloodTypeOptions,
                    placeholder = bloodType
                ) { newBloodType ->
                    bloodType = newBloodType
                    changeBloodType(uid, newBloodType, coroutineScope)
                }
                AddItemDialogListField(
                    fieldName = "Allergies",
                    initialFieldVal = allergies, //comma-separated string
                    onNewItemsAdded = { newAllergies ->
                        // Update the allergies with new entries
                        if (allergies.isNotEmpty()) {
                            allergies += ", "
                        }
                        allergies += newAllergies.joinToString(", ")
                        addAllergies(uid, newAllergies, coroutineScope) //post new allergies in backend
                    }
                )
                StaticUserProfileField(fieldName = "Other Health Info", fieldVal = otherHealth)
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Log out")
                }
            }
        } else{
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun StaticUserProfileField(fieldName: String, fieldVal: String) {
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

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ProfileDropdownMenu(menuName: String, menuOptions: List<String>, placeholder: String, onSelect: (String) -> Unit){
    var expanded by remember { mutableStateOf(false) }
    var selectedText by remember { mutableStateOf(placeholder) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column (
            modifier = Modifier
                .weight(1f)
                .wrapContentWidth(Alignment.Start)
        )
        {
            Text(menuName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = {
                    expanded = !expanded
                }
            ) {
                OutlinedTextField(
                    readOnly = true,
                    value = placeholder,
                    onValueChange = { },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    colors = ExposedDropdownMenuDefaults.textFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = {
                        expanded = false
                    }
                ) {
                    menuOptions.forEach { option ->
                        DropdownMenuItem(
                            onClick = {
                                selectedText = option
                                expanded = false
                                onSelect(option)
                            }
                        ) {
                        Text(text = option)
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.size(48.dp, 48.dp)) //placeholder for blank icon to balance
    }
    Spacer(modifier = Modifier.height(10.dp))
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DateField(uid: String, fieldName: String, dateVal: String, onChange: (String) -> Unit, coroutineScope: CoroutineScope, context: Context){

    fun updateDate(year: Int, month: Int, day: Int) {
        val newDateVal = LocalDate.of(year, month + 1, day).toString()
        onChange(newDateVal)
        updateDob(uid, newDateVal, coroutineScope)
    }

    // Show day + time picker
    fun showDatePicker() {
        val y = dateVal.toLocalDate().year
        val m = dateVal.toLocalDate().monthNumber - 1
        val d = dateVal.toLocalDate().dayOfMonth
        DatePickerDialog(context, R.style.DialogTheme, { _, year, monthOfYear, dayOfMonth ->
            updateDate(year, monthOfYear, dayOfMonth)
        }, y, m, d).show()
    }

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
                value = dateVal,
                onValueChange = {},
                maxLines = 1,
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
            )
        }
        IconButton(onClick = { showDatePicker() }) {
            Icon(Icons.Filled.DateRange, contentDescription = "Birthday")
        }
    }
    Spacer(modifier = Modifier.height(18.dp))
}

@Serializable
data class UserInfo(
    val name: String,
    val email: String,
    val bloodType: String,
    val dob: String,
    val familyID: String
)

private suspend fun getUserInfo(userId: String): UserInfo? {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

    return try {
        val userEndpoint = "http://${GlobalVariables.localIP}:5050/getUserInfo?userID=$userId"
        val response: HttpResponse = withContext(Dispatchers.IO) {
            client.get(userEndpoint) {
                contentType(ContentType.Application.Json)
            }
        }
        if (response.status == HttpStatusCode.OK){
            Json.decodeFromString<UserInfo>(response.bodyAsText())
        } else{
            null
        }

    } catch (e: Exception) {
        e.printStackTrace()
        null
    } finally {
        client.close()
    }
}

private suspend fun getAllergies(userId: String) : String {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

    return try {
        val userEndpoint = "http://${GlobalVariables.localIP}:5050/getAllergies?userID=$userId"
        val response: HttpResponse = withContext(Dispatchers.IO) {
            client.get(userEndpoint) {
                contentType(ContentType.Application.Json)
            }
        }
        val jsonArrayString = response.bodyAsText()
        val allergiesList: List<String> = Json.decodeFromString(jsonArrayString)
        allergiesList.joinToString(separator = ", ")
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    } finally {
        client.close()
    }
}

private suspend fun getHealthFacts(userId: String) : String {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

    return try {
        val userEndpoint = "http://${GlobalVariables.localIP}:5050/getHealthInfo?userID=$userId"
        val response: HttpResponse = withContext(Dispatchers.IO) {
            client.get(userEndpoint) {
                contentType(ContentType.Application.Json)
            }
        }
        val jsonArrayString = response.bodyAsText()
        val allergiesList: List<String> = Json.decodeFromString(jsonArrayString)
        allergiesList.joinToString(separator = ", ")
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    } finally {
        client.close()
    }
}

@Serializable
data class UserPersonal(val userID: String, val name: String, val dobstr: String)

private fun updateName(userId: String, name: String, coroutineScope: CoroutineScope) {
    coroutineScope.launch {
        val userEndpoint = "http://${GlobalVariables.localIP}:5050/updateName"
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json()
            }
        }
        try {
            client.put(userEndpoint) {
                contentType(ContentType.Application.Json)
                setBody(UserPersonal(userId, name, ""))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            client.close()
        }
    }
}

private fun updateDob(userId: String, dob: String, coroutineScope: CoroutineScope){
    Log.d("Server Call", "Update Dob")
    coroutineScope.launch {
        val userEndpoint = "http://${GlobalVariables.localIP}:5050/updateDob"
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json()
            }
        }
        try {
            client.put(userEndpoint) {
                contentType(ContentType.Application.Json)
                setBody(UserPersonal(userId, "", dob))
            }
            Log.d("Server Call", "Put @ updateDob")
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            client.close()
        }
    }
}

@Serializable
data class UserAllergies(val userID: String, val allergies: List<String>)

private fun addAllergies(userId: String, allergiesList: List<String>, coroutineScope: CoroutineScope){
    coroutineScope.launch {
        val userEndpoint = "http://${GlobalVariables.localIP}:5050/addAllergies"
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json()
            }
        }

        try {
            client.post(userEndpoint) {
                contentType(ContentType.Application.Json)
                setBody(UserAllergies(userId, allergiesList))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        client.close()
    }
}

@Serializable
data class UserBloodType(val userID: String, val bloodType: String)

private fun changeBloodType(userId: String, bloodType: String, coroutineScope: CoroutineScope){
    coroutineScope.launch {
        val userEndpoint = "http://${GlobalVariables.localIP}:5050/updateBloodType"
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json()
            }
        }
        try {
            client.put(userEndpoint) {
                contentType(ContentType.Application.Json)
                setBody(UserBloodType(userId, bloodType))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        finally {
            client.close()
        }
    }
}