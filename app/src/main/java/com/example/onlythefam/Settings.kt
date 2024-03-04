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
import androidx.compose.material.Button
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import com.example.onlythefam.ui.theme.Blue500
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SettingsPage(onGoBack: () -> Unit, onLogout: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()

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

        var name by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var dob by remember { mutableStateOf("January 1st, 1999") }
        var bloodType by remember { mutableStateOf("") }
        var allergies by remember { mutableStateOf("") }
        val uid = GlobalVariables.userId?.replace("\"", "") ?: ""

        LaunchedEffect(uid){
            if (uid.isNotEmpty()){
                allergies = getAllergies(uid)
                name = getUserInfo(uid)?.name ?: ""
                email = getUserInfo(uid)?.email ?: ""
                bloodType = getUserInfo(uid)?.bloodType ?: ""
            }
        }

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
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Log out")
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

@Serializable
data class UserInfo(
    val name: String,
    val email: String,
    val bloodType: String
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
        allergiesList.joinToString(separator = ", ").also {
            println("jsonArrayString: $jsonArrayString")
            println("allergiesList: $allergiesList")
            println("allergiesString: $it")
        }
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    } finally {
        client.close()
    }
}