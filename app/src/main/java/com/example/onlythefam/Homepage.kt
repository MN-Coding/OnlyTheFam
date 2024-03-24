package com.example.onlythefam
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import java.time.YearMonth
import androidx.navigation.NavHostController
import java.util.*
import com.google.gson.Gson

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomePage(navController: NavHostController) {
    val name = "John"
    val scrollState = rememberScrollState()
    Scaffold(
        topBar = { Header(navController) },
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp, 10.dp)
//                .verticalScroll(scrollState, enabled = true),
        ) {
            Text("Your Calendar", fontWeight= FontWeight.Bold)
            CalendarApp(modifier = Modifier.height(120.dp))
            FamilyReminderCard()
            Text("Upcoming", fontWeight= FontWeight.Bold)
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                PreviewCard(Modifier.weight(1f), "Todos")
                Spacer(modifier = Modifier.width(16.dp))
                PreviewCard(Modifier.weight(2f), "Events")
            }
            Spacer(Modifier.height(100.dp))
        }
    }
}

data class Quote(val quote: String, val author: String)

@RequiresApi(Build.VERSION_CODES.O)
fun getQuoteForCurrentDay(context: Context): Quote? {
    // Read the JSON file from the raw resources
    val inputStream = context.resources.openRawResource(R.raw.quotes)
    val jsonString = inputStream.bufferedReader().use { it.readText() }

    // Parse the JSON data into an array of Quote objects
    val quotes = Gson().fromJson(jsonString, Array<Quote>::class.java)

    // Get the current day of the month
    val calendar = Calendar.getInstance()
    val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

    // Retrieve the quote for the current day of the month (adjusting for zero-based indexing)
    val quoteIndex = dayOfMonth - 1
    return quotes.getOrNull(quoteIndex)
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FamilyReminderCard() {
    var quote = getQuoteForCurrentDay(LocalContext.current)
    if (quote == null) {
        quote = Quote("null", "null")
    }

    Text("Reminder of the Day", fontWeight= FontWeight.Bold)
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
            Text(
                text = "\"${quote.quote}\"",
                style = MaterialTheme.typography.body1,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "— ${quote.author}",
                style = MaterialTheme.typography.caption
            )
        }
    }
}

