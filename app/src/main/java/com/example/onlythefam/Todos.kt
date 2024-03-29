package com.example.onlythefam

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.navigation.NavController
import io.ktor.client.request.delete
import io.ktor.client.request.setBody
import kotlinx.coroutines.launch

@Serializable
data class TodosResponse(
    @SerialName("todo_id") val todoID: String,
    val event_id: String,
    val name: String,
    val description: String,
    val price: Int,
    val assigned_user_id: String,
    val creator_id: String,
)
// For UI state management
data class TodosUiModel(
    val todosResponse: TodosResponse,
) {
    var expanded by mutableStateOf(false)
}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TodosPage(navController: NavController) {
    val uid = GlobalVariables.userId?.replace("\"", "") ?: ""
    val username = remember { GlobalVariables.username }
    val todosUIModel = remember { mutableStateOf(listOf<TodosUiModel>()) }

    val coroutineScope = rememberCoroutineScope()

    suspend fun fetchTodos() {
        if (uid.isNotEmpty()) {
            val todos = getTodosByUserId(uid)
            todosUIModel.value = todos.map { TodosUiModel(it) }
        }
    }

    LaunchedEffect(uid) {
        coroutineScope.launch {
            fetchTodos()
        }
    }

    Scaffold {
        Column {
            Text(text = "${username}'s Todos", style = MaterialTheme.typography.h4, modifier = Modifier.padding(16.dp))
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                if (todosUIModel.value.isEmpty()) {
                    Text(
                        text = "No current todos",
                        style = MaterialTheme.typography.body1,
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    todosUIModel.value.forEach { todo ->
                        TodoCard(todoUiModel = todo, navController = navController, onDone = { coroutineScope.launch { fetchTodos() } })
                    }
                }
            }
        }
    }
}

@Composable
fun ExpandableCard(
    title: String,
    description: String,
    price: String,
    expanded: Boolean,
    onExpand: () -> Unit,
    todoID: String,
    onDone: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    Card(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth(),
        backgroundColor = MaterialTheme.colors.surface,
        shape = RoundedCornerShape(16.dp),
        elevation = 8.dp,
        border = BorderStroke(1.dp, Color.Gray)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                IconButton(onClick = onExpand) {
                    Icon(
                        imageVector = if (expanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                        contentDescription = if (expanded) "Collapse" else "Expand"
                    )
                }
            }

            if (expanded) {
                Text(
                    text = "description: $description",
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "cost: $price",
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Button(
                    onClick = { coroutineScope.launch { GlobalVariables.userId?.trim('"')?.let {
                        if (deleteTodo(todoID)) {
                            Log.d("TodosPage", "Todo deleted successfully")
                            onDone()
                        } else {
                            Log.d("TodosPage", "Failed to delete todo")
                        }
                    } } },
                    modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.End)
                ) {
                    Text("Done")
                }
            }
        }
    }
}

@Composable
fun TodoCard(todoUiModel: TodosUiModel, navController: NavController, onDone: () -> Unit) {
    val (expanded, setExpanded) = remember { mutableStateOf(false) }

    ExpandableCard(
        title = todoUiModel.todosResponse.name,
        description = todoUiModel.todosResponse.description,
        price = todoUiModel.todosResponse.price.toString(),
        expanded = expanded,
        onExpand = { setExpanded(!expanded) },
        todoID = todoUiModel.todosResponse.todoID,
        onDone = onDone
    )
}

private suspend fun getTodosByUserId(userId: String): List<TodosResponse> {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

    Log.d("TodosPage", "getTodosByUserID called with userID: $userId")

    return try {
        val todosEndpoint = "http://${GlobalVariables.localIP}:5050/getTodosByUserID?userID=$userId"
        Log.d("TodosPage", "Requesting todos from: $todosEndpoint")
        val response: HttpResponse = withContext(Dispatchers.IO) {
            client.get(todosEndpoint) {
                contentType(ContentType.Application.Json)
            }
        }
        Log.d("TodosPage", "Response status: ${response.status}")

        if (response.status == HttpStatusCode.OK) {
            val todos = Json.decodeFromString<List<TodosResponse>>(response.bodyAsText())
            Log.d("TodosPage", "Todos retrieved successfully: ${todos.size}")
            todos
        } else {
            Log.d("TodosPage", "Failed to retrieve todos")
            emptyList()
        }
    } catch (e: Exception) {
        Log.e("TodosPage", "Error fetching todos", e)
        emptyList()
    } finally {
        client.close()
    }
}

private suspend fun deleteTodo(todoID: String): Boolean {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

    return try {
        val deleteTodoEndpoint = "http://${GlobalVariables.localIP}:5050/deleteTodo?todo_id=${todoID}"
        Log.d("deleteTodo", "Deleting todos from: $deleteTodoEndpoint")
        Log.d("deleteTodo", todoID)
        val response: HttpResponse = withContext(Dispatchers.IO) {
            client.delete(deleteTodoEndpoint) {
                contentType(ContentType.Application.Json)
            }
        }
        Log.d("deleteTodo", "Response status: ${response.status}")

        if (response.status == HttpStatusCode.OK) {

            return true

        } else {

            return false

        }
    } catch (e: Exception) {
        Log.e("TodosPage", "Error deleting todo" + todoID, e)
        return false
    } finally {
        client.close()
    }
}
