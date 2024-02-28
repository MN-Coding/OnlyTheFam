package com.example.onlythefam
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import java.time.YearMonth

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomePage() {
    val name = "John"
    val scrollState = rememberScrollState()
    Scaffold(
        topBar = { Header() },
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp, 10.dp)
                .verticalScroll(scrollState, enabled = true),
        ) {
            FamilyReminderCard()
            Text("Your Calendar", fontWeight= FontWeight.Bold)
//            Cal()
            Text("Upcoming", fontWeight= FontWeight.Bold)
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                PreviewCard(modifier = Modifier.weight(1f), "Todos")
                Spacer(modifier = Modifier.width(16.dp))
                PreviewCard(modifier = Modifier.weight(2f), "Events")
            }
            Spacer(Modifier.height(100.dp))
        }
    }
}

@Composable
fun FamilyReminderCard() {
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
                text = "\"If the family were a boat, it would be a canoe that makes no progress unless everyone paddles.\"",
                style = MaterialTheme.typography.body1,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "— Letty Cottin Pogrebin",
                style = MaterialTheme.typography.caption
            )
        }
    }
}