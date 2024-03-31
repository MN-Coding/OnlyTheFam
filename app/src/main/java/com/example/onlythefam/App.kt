package com.example.onlythefam

import FamilyPage
import TodoEventScreen
import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.onlythefam.GlobalVariables.userId
import java.sql.*

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Todos(navController: NavController) {
    TodosPage(navController = navController)
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Events(navController: NavController) {
    EventsPage(navController = navController)
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Family(navController: NavController) {
    FamilyPage(navController = navController)
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Add(navController: NavController) {
    TodoEventScreen(navController)
}

sealed class BottomNavItem(val screen_route: String, val icon: ImageVector, val title: String) {
    object Home : BottomNavItem("home", Icons.Rounded.Home, "Home")
    object Todos : BottomNavItem("todos", Icons.Rounded.List, "Todos")
    object Add : BottomNavItem("add", Icons.Rounded.Add, "Add")
    object Events : BottomNavItem("events", Icons.Rounded.Event, "Events")
    object Inbox : BottomNavItem("inbox", Icons.Rounded.Inbox, "Inbox")
    object Family : BottomNavItem("family", Icons.Rounded.Group, "Family")

}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavigationGraph(navController: NavHostController, logoutProcess: () -> Unit) {
    NavHost(navController, startDestination = BottomNavItem.Home.screen_route) {
        composable(BottomNavItem.Home.screen_route) {
            HomePage(navController)
        }
        composable(BottomNavItem.Todos.screen_route) {
            Todos(navController)
        }
        composable(BottomNavItem.Add.screen_route) {
            Add(navController)
        }
        composable(BottomNavItem.Events.screen_route) {
            Events(navController = navController)
        }
        composable(BottomNavItem.Family.screen_route) {
            Family(navController = navController)
        }
        composable("profileSettings"){
            SettingsPage(onGoBack = {navController.popBackStack()},
                         onLogout = logoutProcess)
        }
        composable("eventDetails/{eventId}") { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId")
            if (eventId != null) {
                EventDetails(navController = navController, eventId = eventId)
            }
        }

        composable("add_todo") { AddTodo(navController) }
        composable("add_event") { AddEvent(navController) }
        composable("todo_event_screen") { TodoEventScreen(navController) }
        composable("inbox") { Inbox(navController) }
    }
}

@Composable
fun BottomNavigation(navController: NavController) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Todos,
        BottomNavItem.Add,
        BottomNavItem.Events,
        BottomNavItem.Family,
        BottomNavItem.Inbox
    )
    val bottomNavRoutes = setOf("home", "todos", "add", "events", "family", "todo_event_screen",
        "add_event", "add_todo", "inbox")

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomNav = currentRoute in bottomNavRoutes

    if (showBottomNav) {
        BottomNavigation(
            backgroundColor = MaterialTheme.colors.background,
            modifier = Modifier
                .padding(start = 40.dp, end = 40.dp, bottom = 40.dp)
                .border(BorderStroke(1.dp, Color.LightGray), RoundedCornerShape(16.dp)),
            elevation = 0.dp
        ) {
            items.forEach { item ->
                BottomNavigationItem(
                    icon = { Icon(item.icon, item.title) },
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
}

object GlobalVariables {
    var userId: String? = null
    var username: String? = null
    val localIP: String? = "10.0.2.2"
}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun App() {
    val loginController = rememberNavController()

    val logout = {
        Log.d("Navigation", "Starting logout process")
        userId = null
        Log.d("Logout", "User ID cleared")
        loginController.navigate("login") {
            popUpTo(loginController.graph.startDestinationId)
            {
                inclusive = false
            }
            Log.d("Navigation", "Navigated to login")
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(navController = loginController, startDestination = "login") {
            composable("login") {
                LoginScreen({loginController.navigate("home") }, {loginController.navigate("signup") })
            }
            composable("signup") {
                SignUpFlow({loginController.navigate("home") }, {loginController.navigate("login") })
            }
            composable("home") {
                val mainNavController = rememberNavController()
                Scaffold(
                    bottomBar = { BottomNavigation(navController = mainNavController) }
                ) {
                    NavigationGraph(navController = mainNavController, logoutProcess = logout)
                }
            }
        }
    }
}
