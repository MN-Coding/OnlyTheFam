import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
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
import kotlinx.serialization.json.Json
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.draw.clip
import coil.compose.rememberImagePainter
import kotlin.random.Random


@Serializable
data class FamilyMember(
    val name: String,
    val email: String,
    val bloodType: String,
    val dob: String,
    val familyID: String
)

@RequiresApi(Build.VERSION_CODES.O)@Composable
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
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                familyMembers!!.forEach { member ->
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
    val randomImageId = Random.nextInt(1, 100)
    val imageUrl = "https://picsum.photos/id/$randomImageId/100/100"

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Image(
                painter = rememberImagePainter(imageUrl),
                contentDescription = "Profile picture of ${member.name}",
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(8.dp))
            Text(text = "Name: ${member.name}", style = MaterialTheme.typography.h6)
            Text(text = "Birthday: ${member.dob}", style = MaterialTheme.typography.body1)
            Text(text = "Email: ${member.email}", style = MaterialTheme.typography.body1)
            Text(text = "Blood Type: ${member.bloodType}", style = MaterialTheme.typography.body1)
        }
    }
}
