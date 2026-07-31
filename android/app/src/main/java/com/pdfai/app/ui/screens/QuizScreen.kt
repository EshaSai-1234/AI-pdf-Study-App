package com.pdfai.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pdfai.app.data.api.RetrofitClient
import com.pdfai.app.data.model.QuizQuestion
import com.pdfai.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun QuizScreen() {
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var quizQuestions by remember { mutableStateOf<List<QuizQuestion>>(emptyList()) }
    var currentQuestionIdx by remember { mutableStateOf(0) }
    
    var selectedOptionIdx by remember { mutableStateOf<Int?>(null) }
    var score by remember { mutableStateOf(0) }
    var isQuizCompleted by remember { mutableStateOf(false) }

    fun fetchQuiz() {
        isLoading = true
        errorMessage = null
        coroutineScope.launch {
            try {
                val response = RetrofitClient.apiService.getQuiz(count = 5)
                if (response.isSuccessful && response.body() != null) {
                    quizQuestions = response.body()!!.quiz
                    currentQuestionIdx = 0
                    selectedOptionIdx = null
                    score = 0
                    isQuizCompleted = false
                } else {
                    errorMessage = "Upload a PDF document first to generate a quiz."
                }
            } catch (e: Exception) {
                errorMessage = "Network Error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        fetchQuiz()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Quiz, contentDescription = null, tint = AccentCyan)
            Spacer(modifier = Modifier.width(8.dp))
            Text("AI Knowledge Quiz", style = Typography.headlineMedium)
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentCyan)
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
                    Button(onClick = { fetchQuiz() }) {
                        Text("Retry")
                    }
                }
            }
        } else if (isQuizCompleted) {
            // Quiz Score Card Summary
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Quiz Completed!", style = Typography.headlineMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Your Final Score: $score / ${quizQuestions.size}",
                        style = Typography.titleLarge,
                        color = AccentCyan
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { fetchQuiz() },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("Retake Quiz")
                    }
                }
            }
        } else if (quizQuestions.isNotEmpty()) {
            val q = quizQuestions[currentQuestionIdx]

            // Question Progress Header
            Text(
                text = "Question ${currentQuestionIdx + 1} of ${quizQuestions.size}",
                style = Typography.titleLarge,
                color = PrimaryPurple
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Question Text Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(q.question, style = Typography.titleLarge, color = TextPrimary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Options List
            q.options.forEachIndexed { optIdx, optionText ->
                val isSelected = selectedOptionIdx == optIdx
                val isCorrect = optIdx == q.correctOptionIndex

                val cardColor = when {
                    selectedOptionIdx == null -> SurfaceCard
                    isSelected && isCorrect -> SuccessGreen.copy(alpha = 0.3f)
                    isSelected && !isCorrect -> ErrorRed.copy(alpha = 0.3f)
                    isCorrect -> SuccessGreen.copy(alpha = 0.2f)
                    else -> SurfaceCard
                }

                val borderColor = when {
                    selectedOptionIdx == null -> Color.Transparent
                    isSelected && isCorrect -> SuccessGreen
                    isSelected && !isCorrect -> ErrorRed
                    isCorrect -> SuccessGreen
                    else -> Color.Transparent
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable(enabled = selectedOptionIdx == null) {
                            selectedOptionIdx = optIdx
                            if (optIdx == q.correctOptionIndex) {
                                score++
                            }
                        },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${('A' + optIdx)}. ",
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                        Text(
                            text = optionText,
                            style = Typography.bodyLarge,
                            color = TextPrimary
                        )
                    }
                }
            }

            // Explanation box when answered
            if (selectedOptionIdx != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.HelpOutline, contentDescription = null, tint = WarningOrange)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Explanation", style = Typography.titleLarge, color = WarningOrange)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(q.explanation, style = Typography.bodyMedium, color = TextPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (currentQuestionIdx < quizQuestions.size - 1) {
                            currentQuestionIdx++
                            selectedOptionIdx = null
                        } else {
                            isQuizCompleted = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text(if (currentQuestionIdx < quizQuestions.size - 1) "Next Question" else "See Final Results")
                }
            }
        }
    }
}
