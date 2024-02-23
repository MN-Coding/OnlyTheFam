package com.example.onlythefam

import android.content.ContentValues.TAG
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.onlythefam.ui.theme.Blue500

@Composable
fun Header() {
    val name = "John"
    TopAppBar(
        title = {
            Text(
                text = "Welcome  Home  $name",
                style = MaterialTheme.typography.h1,
                fontSize = 24.sp,
                color = Color.Black
            )
        },
        actions = {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(end = 16.dp)
            ) {
                IconButton(
                    onClick = { onIconClick() },
                ) {
                    Icon(
                        Icons.Rounded.Person,
                        contentDescription = "icon",
                        tint = Blue500
                    )
                }
            }
        },
        backgroundColor = Color(0x00FFFFFF)
    )
}

fun onIconClick() {
    Log.w(TAG, "clicked")
}
