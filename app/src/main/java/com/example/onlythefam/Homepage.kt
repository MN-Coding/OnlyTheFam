package com.example.onlythefam
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import java.time.YearMonth

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomePage(onButtonClick: () -> Unit) {
    val name = "John";
    Scaffold(
        topBar = { Header() },
        content = { Calendar(YearMonth.now(), {})},
        bottomBar = { MenuBar()}
    )

}
