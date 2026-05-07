package com.gamebot.app.navigation

import android.content.Context
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.gamebot.app.ui.screens.*
import com.gamebot.app.ui.theme.*
import androidx.compose.material.icons.filled.CalendarToday
import com.gamebot.app.ui.screens.CalendarScreen

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Welcome : Screen("welcome", "Welcome", Icons.Default.Home)
    object Login : Screen("login", "Login", Icons.Default.Home)
    object SignUp : Screen("signup", "SignUp", Icons.Default.Home)
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Library : Screen("library", "Library", Icons.Default.Book)
    object Community : Screen("community", "Community", Icons.Default.Group)
    object Map : Screen("map", "Mapa", Icons.Default.Map)
    object Profile : Screen("profile", "Profil", Icons.Default.Person)

    object Notes : Screen("notes", "Bilješke", Icons.Default.Book)

    object Calendar : Screen("calendar", "Kalendar", Icons.Default.CalendarToday)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Library,
    Screen.Community,
    Screen.Map,
    Screen.Profile
)

val authScreens = listOf("welcome", "login", "signup")

@Composable
fun GameBotNavigation() {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("gamebot_prefs", Context.MODE_PRIVATE)
    val isLoggedIn = prefs.getBoolean("is_logged_in", false)

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute !in authScreens

    Scaffold(
        containerColor = BackgroundDark,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = SurfaceDark,
                    contentColor = TextSecondary
                ) {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = screen.title
                                )
                            },
                            label = { Text(screen.title) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PrimaryPurple,
                                selectedTextColor = PrimaryPurple,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary,
                                indicatorColor = SurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (isLoggedIn) Screen.Home.route else Screen.Welcome.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Welcome.route) {
                WelcomeScreen(
                    onLoginClick = {
                        navController.navigate(Screen.Login.route)
                    },
                    onSignUpClick = {
                        navController.navigate(Screen.SignUp.route)
                    }
                )
            }
            composable(Screen.Login.route) {
                LoginScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onLoginSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.SignUp.route) {
                SignUpScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onSignUpSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Home.route) { HomeScreen() }
            composable(Screen.Library.route) {
                LibraryScreen(onGameClick = {
                    navController.navigate(Screen.Notes.route)
                })
            }
            composable(Screen.Community.route) { CommunityScreen() }
            composable(Screen.Map.route) { MapScreen() }
            composable(Screen.Profile.route) { ProfileScreen() }
            composable(Screen.Notes.route) { NotesScreen() }
            composable(Screen.Calendar.route) { CalendarScreen() }
            composable(Screen.Home.route) {
                HomeScreen(onEventClick = {
                    navController.navigate(Screen.Calendar.route)
                })
            }
        }
    }
}