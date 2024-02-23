package com.example.onlythefam

import androidx.compose.foundation.layout.*
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.onlythefam.ui.theme.Blue500

@Composable
fun MenuBar() {
    BottomAppBar(
        backgroundColor = MaterialTheme.colors.background,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon button 1
            IconButton(onClick = { /* Handle click */ }) {
                Icon(Icons.Rounded.Home, contentDescription = null, tint=Color.Black)
            }

            // Icon button 2
            IconButton(onClick = { /* Handle click */ }) {
                Icon(Icons.Rounded.List, contentDescription = null)
            }

            // Plus sign icon button (Middle)
            IconButton(onClick = { /* Handle click */ }) {
                Icon(Icons.Rounded.Add, contentDescription = null,  tint=Blue500)
            }

            // Icon button 3
            IconButton(onClick = { /* Handle click */ }) {
                Icon(Icons.Rounded.Event, contentDescription = null)
            }

            // Icon button 4
            IconButton(onClick = { /* Handle click */ }) {
                Icon(Icons.Rounded.Group, contentDescription = null)
            }
        }
    }
}

