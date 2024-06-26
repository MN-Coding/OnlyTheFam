package com.example.onlythefam

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.onlythefam.ui.theme.Blue200
import com.example.onlythefam.ui.theme.Blue500
import com.example.onlythefam.ui.theme.Blue700
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

//temporary data
data class EventPreview(val name: String, val date: LocalDateTime)
data class TodoPreview(val name: String)

val temp_todos = listOf(
    TodoPreview("Take out trash"),
    TodoPreview("Walk the dog"),
    TodoPreview("Buy grandma a birthday present")
)

@RequiresApi(Build.VERSION_CODES.O)
val temp_events = listOf(
    EventPreview("Weekly Family Dinner", LocalDateTime.of(2024, Month.MARCH, 9, 18, 0)),
    EventPreview("Weekly Family Dinner", LocalDateTime.of(2024, Month.MARCH, 16, 18, 0)),
    EventPreview("Grandma's 91st Birthday", LocalDateTime.of(2024, Month.MARCH, 16, 20, 0))
)


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PreviewCard(m: Modifier = Modifier, title: String, events: List<EventResponse>, todos: List<TodosResponse>) {
    Column(
        modifier = m,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title,
            fontWeight = FontWeight.Normal,
            fontSize = 17.sp,
            color = Color.Gray
        )
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            backgroundColor = MaterialTheme.colors.surface,
            shape = RoundedCornerShape(16.dp),
            elevation = 4.dp,
            border = BorderStroke(1.dp, Color.Gray)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                if (title == "Events") {
                    events.map {e -> EventItem(e)}
                } else {
                    todos.map {t -> TodoItem(t)}
                }
            }
        }
    }
}

@Composable
fun TodoItem(t: TodosResponse) {
    Card(
        backgroundColor = Blue200,
        contentColor = Color.White,
        ) {
            Text(
                text = t.name,
                style = MaterialTheme.typography.body1,
                fontSize = 10.sp,
                modifier = Modifier.padding(5.dp, 5.dp)
            )
        }
    Spacer(modifier = Modifier.height(10.dp))
}

@RequiresApi(Build.VERSION_CODES.O)
fun formatDate(date: LocalDateTime): String {
    val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
    val dayOfMonth = date.dayOfMonth
    val month = date.month
    val year = date.year

    val monthNames = mapOf(
        1 to "January",
        2 to "February",
        3 to "March",
        4 to "April",
        5 to "May",
        6 to "June",
        7 to "July",
        8 to "August",
        9 to "September",
        10 to "October",
        11 to "November",
        12 to "December"
    )

    val monthName = monthNames[month.value]

    return "$dayOfWeek, $monthName $dayOfMonth, $year"
}

@RequiresApi(Build.VERSION_CODES.O)
fun formatTime12Hour(localDateTime: LocalDateTime): String {
    val formatter = DateTimeFormatter.ofPattern("hh:mm a")
    return localDateTime.format(formatter)
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EventItem(e: EventResponse) {
    Row() {
        Column(modifier = Modifier.weight(1f)) {
            Row() {
                Icon(Icons.Rounded.CalendarToday, null,modifier = Modifier.size(15.dp))
                Text(formatDate(LocalDateTime.parse(e.startDatetime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))),fontSize = 10.sp)
            }
            Card(
                backgroundColor = Blue200,
                contentColor = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = e.name,
                    style = MaterialTheme.typography.body1,
                    fontSize = 10.sp,
                    modifier = Modifier
                        .padding(5.dp, 5.dp)
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxHeight()
        ) {
            Icon(Icons.Rounded.Timer, null, modifier = Modifier.size(15.dp))
            Text(formatTime12Hour(LocalDateTime.parse(e.startDatetime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))), fontSize = 10.sp)
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}