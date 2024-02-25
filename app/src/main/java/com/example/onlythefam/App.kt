package com.example.onlythefam

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.onlythefam.ui.theme.Blue500

@Composable
fun Todos() {
    Text("Todos")
}

@Composable
fun Events() {
    Text("Events")
}

@Composable
fun Family() {
    Text("Family")
}

@Composable
fun Add() {
    Text("Add")
}

sealed class BottomNavItem(val screen_route: String, val icon: ImageVector, val title: String) {
    object Home : BottomNavItem("home", Icons.Rounded.Home, "Home")
    object Todos : BottomNavItem("todos", Icons.Rounded.List, "Todos")
    object Add : BottomNavItem("add", Icons.Rounded.Add, "Add")
    object Events : BottomNavItem("events", Icons.Rounded.Event, "Events")
    object Family : BottomNavItem("family", Icons.Rounded.Group, "Family")
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavigationGraph(navController: NavHostController) {
    NavHost(navController, startDestination = BottomNavItem.Home.screen_route) {
        composable(BottomNavItem.Home.screen_route) {
            HomePage()
        }
        composable(BottomNavItem.Todos.screen_route) {
            Todos()
        }
        composable(BottomNavItem.Add.screen_route) {
            Add()
        }
        composable(BottomNavItem.Events.screen_route) {
            Events()
        }
        composable(BottomNavItem.Family.screen_route) {
            Family()
        }
    }
}

@Composable
fun BottomNavigation(navController: NavController) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Todos,
        BottomNavItem.Add,
        BottomNavItem.Events,
        BottomNavItem.Family
    )
    BottomNavigation(
        backgroundColor = MaterialTheme.colors.background,
        modifier = Modifier
            .padding(start = 40.dp, end = 40.dp, bottom = 40.dp)
            .border(BorderStroke(1.dp, Color.LightGray), RoundedCornerShape(16.dp)),
        elevation = 0.dp
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        items.forEach { item ->
            BottomNavigationItem(
                icon = { Icon(item.icon, item.title)},
                selectedContentColor = Color.Black,
                unselectedContentColor = Color.Black.copy(0.4f),
                alwaysShowLabel = false,
                selected = currentRoute == item.screen_route,
                onClick = {
                    navController.navigate(item.screen_route) {

                        navController.graph.startDestinationRoute?.let { screen_route ->
                            popUpTo(screen_route) {
                                saveState = true
                            }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun App() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = { BottomNavigation(navController = navController) }
    ) {
        NavigationGraph(navController = navController)
    }
}