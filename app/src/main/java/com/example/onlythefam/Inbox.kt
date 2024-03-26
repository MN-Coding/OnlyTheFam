package com.example.onlythefam


// make a kotlin composable resembling that of an email inbox, with previews on the left column and actual content on the right
// the previews should be clickable and should change the content on the right

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.onlythefam.ui.theme.OnlyTheFamTheme
import java.sql.*
import java.util.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Inbox(navController: NavHostController) {
    val scrollState = rememberScrollState()
    var selectedEmail by remember { mutableStateOf("Email 1") } // Default selected email

    Row(
        modifier = Modifier
            .padding(30.dp, 45.dp)

    ) {
        // Left column for email previews
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp)
                // .verticalScroll(scrollState, enabled = true),
        ) {
            items(listOf("Email 1", "Email 2", "Email 3")) { email ->
                InboxPreviewCard(Modifier.fillMaxWidth(), email) {
                    selectedEmail = email
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Right column for selected email content
        Box(
            modifier = Modifier
                .weight(2f)
                .fillMaxHeight(0.75f)
                .border(BorderStroke(1.dp, Color.Black)),
            contentAlignment = Alignment.Center
        ) {
            Text("Content of $selectedEmail", fontSize = 20.sp)
        }
    }
}

@Composable
fun InboxPreviewCard(modifier: Modifier, title: String, onClick: () -> Unit) {
    Card(
        modifier = modifier
            .clickable { onClick() },
        border = BorderStroke(1.dp, Color.Black),
        shape = RoundedCornerShape(8.dp),
        backgroundColor = Color.White
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(title, fontSize = 20.sp)
        }
    }
}