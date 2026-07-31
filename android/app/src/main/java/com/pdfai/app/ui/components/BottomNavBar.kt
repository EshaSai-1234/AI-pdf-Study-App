package com.pdfai.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.pdfai.app.ui.theme.PrimaryBlue
import com.pdfai.app.ui.theme.SurfaceDark
import com.pdfai.app.ui.theme.TextPrimary
import com.pdfai.app.ui.theme.TextSecondary

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "PDF Upload", Icons.Default.UploadFile)
    object Summary : Screen("summary", "Summary", Icons.Default.AutoAwesome)
    object Qa : Screen("qa", "Ask Q&A", Icons.Default.QuestionAnswer)
    object Flashcards : Screen("flashcards", "Flashcards", Icons.Default.Style)
    object Quiz : Screen("quiz", "Quiz", Icons.Default.Quiz)
}

@Composable
fun BottomNavBar(navController: NavController) {
    val items = listOf(
        Screen.Home,
        Screen.Summary,
        Screen.Qa,
        Screen.Flashcards,
        Screen.Quiz
    )

    NavigationBar(
        containerColor = SurfaceDark,
        contentColor = TextPrimary
    ) {
        val navBackStackEntry = navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry.value?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = currentRoute == item.route,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryBlue,
                    selectedTextColor = PrimaryBlue,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary,
                    indicatorColor = SurfaceDark
                ),
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}
