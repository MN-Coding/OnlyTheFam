package com.example.onlythefam

// Modified using source code from:
// Author: Matthias Kerat
// Title: CalendarYT
// Repository: https://github.com/MatthiasKerat/CalendarYT/blob/main/app/src/main/java/com/kapps/calendaryt/MainActivity.kt
// Commit: 7b97175

import android.graphics.Paint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.onlythefam.ui.theme.Blue200
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Cal() {
    val today = LocalDate.now()
    val year = today.year
    val month = today.month.ordinal + 1
    val calendarInputList by remember {
        mutableStateOf(createCalendarList(year, month))
    }
    var clickedCalendarElem by remember {
        mutableStateOf<CalendarInput?>(null)
    }
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Calendar(
            calendarInput = calendarInputList,
            onDayClick = { day ->
                clickedCalendarElem = calendarInputList.first { it.day == day }
            },
            month = month,
            year = year,
            modifier = Modifier
//                .padding(0.dp, 10.dp)
                .fillMaxWidth()
                .aspectRatio(1.4f)
                .border(
                    border = BorderStroke(1.dp, Color.LightGray),
                    shape = RoundedCornerShape(16.dp) // Adjust the shape as needed
                )
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
//                .align(Alignment.Center)
        ) {
            clickedCalendarElem?.toDos?.forEach {
                Text(
                    if (it.contains("Day")) it else "- $it",
                    color = Blue200,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = if (it.contains("Day")) 12.sp else 18.sp
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun createCalendarList(year: Int, month: Int): List<CalendarInput> {
    val yearMonth = YearMonth.of(year, month)
    val daysInMonth = yearMonth.lengthOfMonth()
    val calendarInputs = mutableListOf<CalendarInput>()
    for (i in 1..daysInMonth) {
        calendarInputs.add(
            CalendarInput(
                i, toDos = listOf(
                    "Day $i:", "2 p.m. Buying groceries", "4 p.m. Meeting with Larissa"
                )
            )
        )
    }
    return calendarInputs
}


private const val CALENDAR_ROWS = 6
private const val CALENDAR_COLUMNS = 7

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Calendar(
    modifier: Modifier = Modifier,
    calendarInput: List<CalendarInput>,
    onDayClick: (Int) -> Unit,
    strokeWidth: Float = 2f,
    year: Int,
    month: Int
) {

    var canvasSize by remember {
        mutableStateOf(Size.Zero)
    }
    var clickAnimationOffset by remember {
        mutableStateOf(Offset.Zero)
    }

    var animationRadius by remember {
        mutableStateOf(0f)
    }

    val scope = rememberCoroutineScope()
    val yearMonth = YearMonth.of(year, month)
    val startingDayOfMonth = yearMonth.atDay(1).dayOfWeek.ordinal + 1

    Column(
        modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = Month.of(month).toString(),
            color = Blue200,
            fontSize = 20.sp,
            modifier = Modifier.padding(10.dp, 5.dp)
        )
        Divider(
            modifier = Modifier.padding(horizontal = 15.dp), color = Color.LightGray
        )
        Canvas(modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .pointerInput(true) {
                detectTapGestures(onTap = { offset ->
                    val column = (offset.x / canvasSize.width * CALENDAR_COLUMNS).toInt() + 1
                    val row = (offset.y / canvasSize.height * CALENDAR_ROWS).toInt() + 1
                    val day = column + (row - 1) * CALENDAR_COLUMNS - startingDayOfMonth
                    if (day > 0 && day <= calendarInput.size) {
                        onDayClick(day)
                        clickAnimationOffset = offset
                        scope.launch {
                            animate(0f, 225f, animationSpec = tween(300)) { value, _ ->
                                animationRadius = value
                            }
                        }
                    }

                })
            }) {
            val canvasHeight = size.height
            val canvasWidth = size.width
            canvasSize = Size(canvasWidth, canvasHeight)
            val ySteps = canvasHeight / CALENDAR_ROWS
            val xSteps = canvasWidth / CALENDAR_COLUMNS

            val column = (clickAnimationOffset.x / canvasSize.width * CALENDAR_COLUMNS).toInt() + 1
            val row = (clickAnimationOffset.y / canvasSize.height * CALENDAR_ROWS).toInt() + 1

            val path = Path().apply {
                moveTo((column - 1) * xSteps, (row - 1) * ySteps)
                lineTo(column * xSteps, (row - 1) * ySteps)
                lineTo(column * xSteps, row * ySteps)
                lineTo((column - 1) * xSteps, row * ySteps)
                close()
            }

            clipPath(path) {
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(Blue200.copy(0.8f), Blue200.copy(0.2f)),
                        center = clickAnimationOffset,
                        radius = animationRadius + 0.1f
                    ), radius = animationRadius + 0.1f, center = clickAnimationOffset
                )
            }

            drawRoundRect(
                Color(0x00FFFFFF), cornerRadius = CornerRadius(25f, 25f), style = Stroke(
                    width = strokeWidth
                )
            )

            for (i in 1 until CALENDAR_ROWS) {
                drawLine(
                    color = Color.LightGray,
                    start = Offset(0f, ySteps * i),
                    end = Offset(canvasWidth, ySteps * i),
                    strokeWidth = strokeWidth
                )
            }
            for (i in 1 until CALENDAR_COLUMNS) {
                drawLine(
                    color = Color.LightGray,
                    start = Offset(xSteps * i, 0f),
                    end = Offset(xSteps * i, canvasHeight),
                    strokeWidth = strokeWidth
                )
            }
            val textHeight = 17.dp.toPx()
            for (i in calendarInput.indices) {
                val textPositionX = xSteps * ((i + startingDayOfMonth) % CALENDAR_COLUMNS) + strokeWidth
                val textPositionY =
                    ((i + startingDayOfMonth) / CALENDAR_COLUMNS) * ySteps + textHeight + strokeWidth / 2
                drawContext.canvas.nativeCanvas.apply {
                    drawText("${i + 1}", textPositionX, textPositionY, Paint().apply {
                        textSize = textHeight
                        color = Blue200.toArgb()
                        isFakeBoldText = true
                    })
                }
            }
        }
    }

}

data class CalendarInput(
    val day: Int, val toDos: List<String> = emptyList()
)