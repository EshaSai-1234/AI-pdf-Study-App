package com.pdfai.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pdfai.app.data.api.RetrofitClient
import com.pdfai.app.data.model.SummaryResponse
import com.pdfai.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun SummaryScreen() {
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var summaryData by remember { mutableStateOf<SummaryResponse?>(null) }

    fun fetchSummary() {
        isLoading = true
        errorMessage = null
        coroutineScope.launch {
            try {
                val response = RetrofitClient.apiService.getSummary()
                if (response.isSuccessful && response.body() != null) {
                    summaryData = response.body()
                } else {
                    errorMessage = "Upload a PDF first to generate a summary."
                }
            } catch (e: Exception) {
                errorMessage = "Network Error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        fetchSummary()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AccentCyan)
                Spacer(modifier = Modifier.width(8.dp))
                Text("AI Executive Summary", style = Typography.headlineMedium)
            }
            IconButton(onClick = { fetchSummary() }) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = PrimaryBlue)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryBlue)
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
                    Button(onClick = { fetchSummary() }) {
                        Text("Retry")
                    }
                }
            }
        } else summaryData?.let { data ->
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Topic Chips Row
                item {
                    Text("Extracted Key Topics:", style = Typography.titleLarge)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(data.topics) { topic ->
                            Box(
                                modifier = Modifier
                                    .background(SurfaceCard, CircleShape)
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(topic, style = Typography.bodyMedium, color = AccentCyan)
                            }
                        }
                    }
                }

                // Document Metadata Card
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Icon(Icons.Default.Schedule, contentDescription = null, tint = PrimaryBlue)
                                Text("Reading Time", style = Typography.bodyMedium)
                                Text("${data.readingTimeMinutes} mins", style = Typography.titleLarge)
                            }
                        }

                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen)
                                Text("Word Count", style = Typography.bodyMedium)
                                Text("${data.wordCount} words", style = Typography.titleLarge)
                            }
                        }
                    }
                }

                // Executive Summary Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text("Summary", style = Typography.titleLarge, color = PrimaryBlue)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(data.summary, style = Typography.bodyLarge, color = TextPrimary)
                        }
                    }
                }

                // Key Points List
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text("Key Takeaways", style = Typography.titleLarge, color = AccentCyan)
                            Spacer(modifier = Modifier.height(10.dp))
                            data.keyPoints.forEachIndexed { idx, point ->
                                Row(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text("${idx + 1}. ", color = PrimaryPurple, fontWeight = FontWeight.Bold)
                                    Text(point, style = Typography.bodyMedium, color = TextPrimary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
