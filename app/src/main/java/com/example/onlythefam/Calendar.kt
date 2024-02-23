package com.example.onlythefam

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalFoundationApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Calendar(month: YearMonth, onDateClicked: (LocalDate) -> Unit) {
    val daysOfWeek = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val currentMonthDays = month.lengthOfMonth()
    val firstDayOfMonth = month.atDay(1).dayOfWeek.value

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            daysOfWeek.forEach {
                Text(
                    text = it,
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp),
                    style = MaterialTheme.typography.body2,
                    color = Color.Black
                )
            }
        }

        val dayItems = (1..currentMonthDays).map { dayOfMonth ->
            val dayOfWeek = month.atDay(dayOfMonth).dayOfWeek.value
//            val textColor = if (dayOfWeek == 7) Color.Red else Color.Black
            val textColor = Color.Black
            DayItem(dayOfMonth, textColor) { clickedDay ->
                val clickedDate = month.atDay(clickedDay)
                onDateClicked(clickedDate)
            }
        }

        LazyVerticalGrid(cells = GridCells.Fixed(7)) {
            items(dayItems.size) { index ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp)
                        .background(Color.White)
                        .clickable { dayItems[index].onClick(dayItems[index].dayOfMonth) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = dayItems[index].dayOfMonth.toString(),
                        style = MaterialTheme.typography.body1,
                        color = dayItems[index].textColor
                    )
                }
            }
        }
    }
}

data class DayItem(
    val dayOfMonth: Int,
    val textColor: Color,
    val onClick: (Int) -> Unit
)

@RequiresApi(Build.VERSION_CODES.O)
@Preview
@Composable
fun CalendarPreview() {
    val currentMonth = YearMonth.now()
    Calendar(month = currentMonth, onDateClicked = {})
}
