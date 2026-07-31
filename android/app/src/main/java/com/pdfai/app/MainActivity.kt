package com.pdfai.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pdfai.app.ui.components.BottomNavBar
import com.pdfai.app.ui.components.Screen
import com.pdfai.app.ui.screens.*
import com.pdfai.app.ui.theme.PdfAiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PdfAiTheme {
                PdfAiApp()
            }
        }
    }
}

@Composable
fun PdfAiApp() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavBar(navController = navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onPdfProcessed = {
                        navController.navigate(Screen.Summary.route)
                    }
                )
            }
            composable(Screen.Summary.route) {
                SummaryScreen()
            }
            composable(Screen.Qa.route) {
                QaScreen()
            }
            composable(Screen.Flashcards.route) {
                FlashcardsScreen()
            }
            composable(Screen.Quiz.route) {
                QuizScreen()
            }
        }
    }
}
