package com.example.onlythefam

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
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.onlythefam.GlobalVariables.userId
import java.sql.*

@Composable
fun Todos() {
    Text("Todos")

}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Events(navController: NavController) {
    EventsPage(navController = navController)
}


@Composable
fun Family() {
    Text("Family")
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Add() {
    AddEvent()
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
fun NavigationGraph(navController: NavHostController, logoutProcess: () -> Unit) {
    val currentRoute = navController.currentDestination?.route
    Log.d("Navigation", "Entered NavGraph, currentRoute: $currentRoute")
    NavHost(navController, startDestination = BottomNavItem.Home.screen_route) {
        composable(BottomNavItem.Home.screen_route) {
            Log.d("Navigation", "Entering Homepage")
            HomePage(navController)
        }
        composable(BottomNavItem.Todos.screen_route) {
            Todos()
        }
        composable(BottomNavItem.Add.screen_route) {
            Add()
        }
        composable(BottomNavItem.Events.screen_route) {
            Events(navController = navController)
        }
        composable(BottomNavItem.Family.screen_route) {
            Family()
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
    val bottomNavRoutes = setOf("home", "todos", "add", "events", "family")

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    Log.d("Navigation", "Bottom Navigation currentRoute: $currentRoute")
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
    val localIP: String? = "10.0.2.2"
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun App() {
    val mainNavController = rememberNavController()
    val loginController = rememberNavController()

    val logout = {
        Log.d("Navigation", "Starting logout process")
        mainNavController.popBackStack()
        mainNavController.popBackStack("home", inclusive=false, saveState = false)
        userId = null
        Log.d("Logout", "User ID cleared")
        mainNavController.popBackStack(mainNavController.graph.startDestinationId, inclusive = true, saveState = false)
        val currentRoute = mainNavController.currentDestination?.route
        Log.d("Navigation", "currentRoute: $currentRoute")
        Log.d("Navigation", "navController back stack cleared")
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
                LoginScreen(
                    { loginController.navigate("home") },
                    { loginController.navigate("signup") })
            }
            composable("signup") {
                SignUpFlow(
                    { loginController.navigate("home") },
                    { loginController.navigate("login") })
            }
            composable("home") {
                Scaffold(
                    bottomBar = { BottomNavigation(navController = mainNavController) }
                ) {
                    NavigationGraph(navController = mainNavController, logoutProcess = logout)
                }
            }
        }
    }
}

//class DatabaseHelper(private val url: String, private val user: String, private val password: String) {
//
//    fun executeQuery(query: String, params: Array<Any>): ResultSet? {
//        var resultSet: ResultSet? = null
//        var connection: Connection? = null
//
//        try {
//            connection = DriverManager.getConnection(url, user, password)
//            val preparedStatement: PreparedStatement = connection.prepareStatement(query)
//            for (i in params.indices) {
//                preparedStatement.setObject(i + 1, params[i])
//            }
//            resultSet = preparedStatement.executeQuery()
//        } catch (e: SQLException) {
//            e.printStackTrace()
//        }
//        connection?.close()
//        return resultSet
//    }
//
//    fun executeUpdate(query: String, params: Array<Any>): Int {
//        var connection: Connection? = null
//        var ret: Int = -1
//
//        try {
//            connection = DriverManager.getConnection(url, user, password)
//            val preparedStatement: PreparedStatement = connection.prepareStatement(query)
//            for (i in params.indices) {
//                preparedStatement.setObject(i + 1, params[i])
//            }
//            ret = preparedStatement.executeUpdate()
//        } catch (e: SQLException) {
//            e.printStackTrace()
//        }
//        connection?.close()
//        return ret
//    }
//
//
//}
