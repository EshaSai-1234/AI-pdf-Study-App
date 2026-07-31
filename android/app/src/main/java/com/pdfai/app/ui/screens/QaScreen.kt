package com.pdfai.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Source
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pdfai.app.data.api.RetrofitClient
import com.pdfai.app.data.model.QaRequest
import com.pdfai.app.data.model.QaResponse
import com.pdfai.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QaScreen() {
    val coroutineScope = rememberCoroutineScope()
    var questionInput by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val chatHistory = remember { mutableStateListOf<QaResponse>() }

    val samplePrompts = listOf(
        "What is the main topic of this PDF?",
        "Explain machine learning concepts discussed",
        "Summarize ChromaDB features",
        "How is TensorFlow Lite used?"
    )

    fun sendQuestion(text: String) {
        if (text.isBlank()) return
        isLoading = true
        coroutineScope.launch {
            try {
                val response = RetrofitClient.apiService.askQuestion(QaRequest(question = text, topK = 3))
                if (response.isSuccessful && response.body() != null) {
                    chatHistory.add(0, response.body()!!)
                    questionInput = ""
                }
            } catch (e: Exception) {
                // handle error
            } finally {
                isLoading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
    ) {
        Text("Document RAG Q&A", style = Typography.headlineMedium)
        Text("Ask questions based on indexed ChromaDB vectors", style = Typography.bodyMedium)

        Spacer(modifier = Modifier.height(12.dp))

        // Quick Suggestion Chips
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(samplePrompts) { prompt ->
                Box(
                    modifier = Modifier
                        .background(SurfaceCard, CircleShape)
                        .clickable {
                            questionInput = prompt
                            sendQuestion(prompt)
                        }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(prompt, style = Typography.bodyMedium, color = AccentCyan)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Question Input Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = questionInput,
                onValueChange = { questionInput = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Ask anything from PDF...") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = TextSecondary,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = { sendQuestion(questionInput) },
                enabled = questionInput.isNotBlank() && !isLoading,
                modifier = Modifier.background(PrimaryBlue, CircleShape)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = TextPrimary)
                } else {
                    Icon(Icons.Default.Send, contentDescription = "Send", tint = TextPrimary)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Chat History List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(chatHistory) { qa ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Q: ${qa.question}",
                            style = Typography.titleLarge,
                            color = PrimaryPurple
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = qa.answer,
                            style = Typography.bodyLarge,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Confidence: ${(qa.confidenceScore * 100).toInt()}%",
                                style = Typography.bodyMedium,
                                color = SuccessGreen,
                                fontWeight = FontWeight.Bold
                            )

                            if (qa.sources.isNotEmpty()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Source, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("ChromaDB Context", style = Typography.bodyMedium, color = AccentCyan)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
