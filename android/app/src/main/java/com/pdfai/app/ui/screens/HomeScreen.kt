package com.pdfai.app.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pdfai.app.data.api.RetrofitClient
import com.pdfai.app.data.tflite.TFLiteManager
import com.pdfai.app.ui.theme.*
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onPdfProcessed: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    var serverUrl by remember { mutableStateOf(RetrofitClient.getBaseUrl()) }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("No PDF document indexed yet.") }
    var documentName by remember { mutableStateOf("") }
    var totalChunks by remember { mutableStateOf(0) }
    var wordCount by remember { mutableStateOf(0) }

    var tfliteTestResult by remember { mutableStateOf("") }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedUri = uri
        uri?.let {
            statusMessage = "PDF file selected. Ready to upload and index."
        }
    }

    val tfLiteManager = remember { TFLiteManager(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(PrimaryBlue, PrimaryPurple)
                        )
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Text(
                        text = "PDF AI Study Assistant",
                        style = Typography.headlineMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = "FastAPI + ChromaDB RAG + TensorFlow Lite Engine",
                        style = Typography.bodyMedium,
                        color = TextPrimary.copy(alpha = 0.8f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Backend URL Setting Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Backend Server API Endpoint:", style = Typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = serverUrl,
                    onValueChange = { 
                        serverUrl = it
                        RetrofitClient.updateBaseUrl(it)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Base URL (Emulator: http://10.0.2.2:8000)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = TextSecondary,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // PDF Upload Box
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.PictureAsPdf,
                    contentDescription = "PDF Icon",
                    tint = AccentCyan,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (selectedUri != null) "File Selected" else "Select PDF to Analyze",
                    style = Typography.titleLarge
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { filePickerLauncher.launch("application/pdf") },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Browse PDF")
                    }

                    Button(
                        onClick = {
                            selectedUri?.let { uri ->
                                isUploading = true
                                coroutineScope.launch {
                                    uploadPdfFile(context, uri,
                                        onSuccess = { resp ->
                                            isUploading = false
                                            documentName = resp.filename
                                            totalChunks = resp.totalChunks
                                            wordCount = resp.wordCount
                                            statusMessage = "Indexed ${resp.totalChunks} chunks in ChromaDB!"
                                            onPdfProcessed()
                                        },
                                        onError = { err ->
                                            isUploading = false
                                            statusMessage = "Upload Failed: $err"
                                        }
                                    )
                                }
                            }
                        },
                        enabled = selectedUri != null && !isUploading,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                    ) {
                        if (isUploading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = TextPrimary)
                        } else {
                            Icon(Icons.Default.CloudUpload, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Index PDF")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Status & Metadata Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Document Index Status", style = Typography.titleLarge, color = AccentCyan)
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = statusMessage, style = Typography.bodyMedium, color = TextPrimary)
                
                if (documentName.isNotEmpty()) {
                    Divider(modifier = Modifier.padding(vertical = 10.dp), color = SurfaceCard)
                    Text("File: $documentName", style = Typography.bodyLarge, fontWeight = FontWeight.Bold)
                    Text("ChromaDB Chunks: $totalChunks", style = Typography.bodyMedium)
                    Text("Total Words: $wordCount", style = Typography.bodyMedium)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // On-Device TFLite Tester Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Memory, contentDescription = null, tint = WarningOrange)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("On-Device TensorFlow Lite Engine", style = Typography.titleLarge)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Button(
                    onClick = {
                        val sim = tfLiteManager.calculateOnDeviceSimilarity(
                            "TensorFlow Lite Android embedding",
                            "On-device neural network vector search"
                        )
                        tfliteTestResult = "TFLite Similarity Score: ${(sim * 100).toInt()}%"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceCard)
                ) {
                    Text("Run Local TFLite Feature Extractor")
                }
                if (tfliteTestResult.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = tfliteTestResult, style = Typography.bodyLarge, color = SuccessGreen)
                }
            }
        }
    }
}

private suspend fun uploadPdfFile(
    context: Context,
    uri: Uri,
    onSuccess: (com.pdfai.app.data.model.UploadResponse) -> Unit,
    onError: (String) -> Unit
) {
    try {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw Exception("Could not open file input stream")
        val bytes = inputStream.readBytes()
        inputStream.close()

        val requestFile = bytes.toRequestBody("application/pdf".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", "uploaded_doc.pdf", requestFile)

        val response = RetrofitClient.apiService.uploadPdf(body)
        if (response.isSuccessful && response.body() != null) {
            onSuccess(response.body()!!)
        } else {
            onError(response.errorBody()?.string() ?: "Server returned error code ${response.code()}")
        }
    } catch (e: Exception) {
        onError(e.message ?: "Unknown network error")
    }
}
