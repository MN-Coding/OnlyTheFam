package com.example.onlythefam

import android.content.ContentValues.TAG
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.onlythefam.ui.theme.Blue500

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Header(navController: NavHostController) {
    val name = "John"
    TopAppBar(
        modifier = Modifier
            .padding(10.dp, 10.dp),
        elevation = 0.dp,
        title = {
            Text(
                text = "Welcome Home $name",
                style = MaterialTheme.typography.h2,
                fontSize = 24.sp,
                color = Color.Black,
            )
        },
        actions = {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(end = 16.dp)
            ) {
                IconButton(
                    onClick = { navController.navigate("profileSettings") },
                ) {
                    Icon(
                        Icons.Rounded.Person,
                        contentDescription = "icon",
                        tint = Blue500,
                        modifier = Modifier
                            .size(30.dp)
                            .aspectRatio(1f)
                    )
                }
            }
        },
        backgroundColor = Color(0x00FFFFFF)
    )
}
