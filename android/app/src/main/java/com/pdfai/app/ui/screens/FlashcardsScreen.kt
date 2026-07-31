package com.pdfai.app.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pdfai.app.data.api.RetrofitClient
import com.pdfai.app.data.model.Flashcard
import com.pdfai.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun FlashcardsScreen() {
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var flashcards by remember { mutableStateOf<List<Flashcard>>(emptyList()) }
    var currentIndex by remember { mutableStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }

    fun fetchFlashcards() {
        isLoading = true
        errorMessage = null
        coroutineScope.launch {
            try {
                val response = RetrofitClient.apiService.getFlashcards(count = 6)
                if (response.isSuccessful && response.body() != null) {
                    flashcards = response.body()!!.flashcards
                    currentIndex = 0
                    isFlipped = false
                } else {
                    errorMessage = "Upload a PDF document first to generate flashcards."
                }
            } catch (e: Exception) {
                errorMessage = "Network Error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        fetchFlashcards()
    }

    // Flip Animation calculation
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "Flashcard Flip Animation"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Style, contentDescription = null, tint = PrimaryPurple)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Interactive Flashcards", style = Typography.headlineMedium)
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryPurple)
            }
        } else if (errorMessage != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(errorMessage!!, style = Typography.bodyLarge, color = WarningOrange)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { fetchFlashcards() }) {
                        Text("Retry")
                    }
                }
            }
        } else if (flashcards.isNotEmpty()) {
            val currentCard = flashcards[currentIndex]

            // Progress Header
            Text(
                text = "Card ${currentIndex + 1} of ${flashcards.size}",
                style = Typography.titleLarge,
                color = AccentCyan
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3D Flip Card Container
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .graphicsLayer {
                        rotationY = rotation
                        cameraDistance = 12f * density
                    }
                    .clickable { isFlipped = !isFlipped },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (isFlipped) {
                                Brush.verticalGradient(listOf(SurfaceDark, SurfaceCard))
                            } else {
                                Brush.verticalGradient(listOf(PrimaryBlue.copy(alpha = 0.2f), SurfaceDark))
                            }
                        )
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Category & Difficulty Pill
                        Box(
                            modifier = Modifier
                                .background(if (isFlipped) SuccessGreen else PrimaryPurple, CircleShape)
                                .padding(horizontal = 14.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isFlipped) "ANSWER" else "QUESTION (${currentCard.difficulty.uppercase()})",
                                style = Typography.bodyMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        if (rotation <= 90f) {
                            // Question Front
                            Text(
                                text = currentCard.question,
                                style = Typography.headlineMedium,
                                textAlign = TextAlign.Center,
                                color = TextPrimary
                            )
                        } else {
                            // Answer Back (Flipped)
                            Text(
                                text = currentCard.answer,
                                style = Typography.titleLarge,
                                textAlign = TextAlign.Center,
                                color = AccentCyan,
                                modifier = Modifier.graphicsLayer { rotationY = 180f }
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.graphicsLayer { if (rotation > 90f) rotationY = 180f }
                        ) {
                            Icon(Icons.Default.Flip, contentDescription = null, tint = TextSecondary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tap card to flip", style = Typography.bodyMedium, color = TextSecondary)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Navigation Controls
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = {
                        if (currentIndex > 0) {
                            currentIndex--
                            isFlipped = false
                        }
                    },
                    enabled = currentIndex > 0,
                    modifier = Modifier.background(SurfaceCard, CircleShape)
                ) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Previous", tint = TextPrimary)
                }

                Button(
                    onClick = { isFlipped = !isFlipped },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                ) {
                    Text("Flip Card")
                }

                IconButton(
                    onClick = {
                        if (currentIndex < flashcards.size - 1) {
                            currentIndex++
                            isFlipped = false
                        }
                    },
                    enabled = currentIndex < flashcards.size - 1,
                    modifier = Modifier.background(SurfaceCard, CircleShape)
                ) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Next", tint = TextPrimary)
                }
            }
        }
    }
}
