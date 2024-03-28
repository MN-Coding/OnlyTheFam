import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.onlythefam.GlobalVariables
import com.example.onlythefam.GlobalVariables.userId
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json


@Serializable
data class FamilyMember(
    val name: String,
    val email: String,
    val bloodType: String,
    val dob: String,
    val familyID: String
)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FamilyPage(navController: NavController) {
    val coroutineScope = rememberCoroutineScope()
    var familyMembers by remember { mutableStateOf<List<FamilyMember>?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(userId) {
        coroutineScope.launch {
            try {
                Log.d("UserID", "UserID: $userId")
                familyMembers = userId?.let { getFamilyMembers(userId!!) }
                Log.d("Family", "Family members loaded: $familyMembers")
                isLoading = false
            } catch (e: Exception) {
                Log.e("Family", "Error fetching family members", e)
                isLoading = false
            }
        }
    }

    val familyId = familyMembers?.firstOrNull()?.familyID

    Column(modifier = Modifier.fillMaxSize()) {
        if (!isLoading && familyId != null) {
            Text(
                text = "Family ID: $familyId",
                style = MaterialTheme.typography.h5,
                modifier = Modifier.padding(16.dp)
            )
        }
        if (isLoading) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator()
            }
        } else if (familyMembers != null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(familyMembers!!) { member ->
                    FamilyMemberCard(member = member)
                }
            }
        } else {
            Text("No family members found.")
        }
    }
}

private suspend fun getFamilyMembers(userId: String): List<FamilyMember>? {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

    return try {
        val familyEndpoint = "http://${GlobalVariables.localIP}:5050/getFamilyMembers?userID=$userId"
        Log.d("Network", "Requesting family members from: $familyEndpoint")
        val response: HttpResponse = withContext(Dispatchers.IO) {
            client.get(familyEndpoint) {
                contentType(ContentType.Application.Json)
            }
        }
        Log.d("Network", "Response status: ${response.status}")
        if (response.status == HttpStatusCode.OK) {
            Json { ignoreUnknownKeys = true }.decodeFromString<List<FamilyMember>>(response.bodyAsText()).also {
                Log.d("Network", "Family members received: $it")
                return it
            }
        } else {
            Log.d("Network", "Failed to load family members, status code: ${response.status}")
            null
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    } finally {
        client.close()
    }
}

@Composable
fun FamilyMemberCard(member: FamilyMember) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "Name: ${member.name}", style = MaterialTheme.typography.h6)
            Text(text = "Birthday: ${member.dob}", style = MaterialTheme.typography.body1)
            Text(text = "Email: ${member.email}", style = MaterialTheme.typography.body1)
            Text(text = "Blood Type: ${member.bloodType}", style = MaterialTheme.typography.body1)
//            if (member.allergies.isNotEmpty()) {
//                Text(text = "Allergies: ${member.allergies.joinToString(", ")}", style = MaterialTheme.typography.body1)
//            }
//            if (member.otherHealthInfo.isNotEmpty()) {
//                Text(text = "Other Health Info: ${member.otherHealthInfo}", style = MaterialTheme.typography.body1)
//            }
        }
    }
}