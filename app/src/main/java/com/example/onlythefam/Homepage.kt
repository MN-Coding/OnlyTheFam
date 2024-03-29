package com.example.onlythefam
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import java.time.YearMonth
import androidx.navigation.NavHostController
import coil.compose.AsyncImagePainter
import java.util.*
import com.google.gson.Gson
import coil.compose.rememberImagePainter

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomePage(navController: NavHostController) {
    val scrollState = rememberScrollState()
    Scaffold(
        topBar = { Header(navController) },
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp, 10.dp)
                .verticalScroll(scrollState, enabled = true),
        ) {
            CalendarApp(modifier = Modifier.height(120.dp))
            FamilyReminderCard()
            Articles()
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

data class ArticlePair(
    val title: String,
    val imageUrl: String,
    val url: String
)

val images = listOf(
    ArticlePair(
        "10 Family Day activities to enjoy with your family",
        "https://calendar.wincalendar.net/img/holiday/family-day.png",
        "https://isure.ca/inews/family-day-activities-to-enjoy-with-your-family/"
    ),
    ArticlePair(
        "20 Fun Ways to Stay Connected with Family and Friends Around the World",
        "https://i.etsystatic.com/9947238/r/il/2401b6/2510122161/il_fullxfull.2510122161_i4qa.jpg",
        "https://www.westernunion.com/blog/en/stay-connected-with-family-and-friends-around-the-world/"
    ),
    ArticlePair(
        "The Cook-Off: A FUN Way to Get Your Entire Family Cooking Together!",
        "https://media.istockphoto.com/id/1257529043/vector/mother-and-father-with-kids-cooking-dishes-at-kitchen.jpg?s=612x612&w=0&k=20&c=yX0jLc2UmZrkhpnDVZ59yPnPWxfT67GpCiDPBkuv-qw=",
        "https://www.100daysofrealfood.com/cook-off-family-blue-apron/"
    )
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Articles() {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { }
    val pagerState = rememberPagerState(pageCount = {
        3
    })

    Text("Tips", fontWeight= FontWeight.Bold)
    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxWidth()
    ) { page ->
        Box (
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(15.dp)
                .fillMaxWidth()
                .wrapContentHeight()
                .background(color = Color.White) // Set background color
                .border(width = 1.dp, color = Color.Gray, shape = RoundedCornerShape(16.dp))
                .clickable {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(images[page].url))
                launcher.launch(intent)
                }
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(15.dp)
            ) {
                var imagePainter: Painter = rememberImagePainter(
                    data = images[page].imageUrl,
                    builder = {
                        crossfade(true)
                    }
                )
                Text(text=images[page].title, fontSize = 14.sp)
                Image(
                    painter = imagePainter,
                    contentDescription = null, // Pass null if the image is purely decorative
                    modifier = Modifier.height(100.dp),
                    contentScale = ContentScale.FillBounds
                )
            }

        }
    }
    Row(
        Modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.Bottom
    ) {
        repeat(pagerState.pageCount) { iteration ->
            val color = if (pagerState.currentPage == iteration) Color.DarkGray else Color.LightGray
            Box(
                modifier = Modifier
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(color)
                    .size(6.dp)
            )
        }
    }
}