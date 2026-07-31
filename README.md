# AI & ML PDF Study Assistant (Kotlin + FastAPI + ChromaDB + TFLite)

A full-stack Android & Python application that parses uploaded PDF documents and provides interactive **Summaries**, **Vector RAG Q&A**, **Flashcards**, and **Multiple-Choice Quizzes** using **Kotlin Jetpack Compose**, **FastAPI**, **ChromaDB**, and **TensorFlow Lite (TFLite)**.

---

## 🏗️ Architecture & Technology Stack

```
                              ┌─────────────────────────────────────────┐
                              │           Android Kotlin App            │
                              │         (Jetpack Compose UI)            │
                              └────────────────────┬────────────────────┘
                                                   │
                                      Retrofit REST API Requests
                                                   │
                                                   ▼
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                                Python FastAPI Backend Server                           │
├──────────────────────────┬──────────────────────────┬──────────────────────────────────┤
│   PDF Parser & Chunking  │    ChromaDB Vector Store │   AI NLP Engine & TFLite         │
│   (PyPDF Processor)      │   (Embedding & RAG Q&A) │   (Summaries, Cards & Quizzes)   │
└──────────────────────────┴──────────────────────────┴──────────────────────────────────┘
```

- **Android Client**: Kotlin, Jetpack Compose (Material 3), Retrofit2, OkHttp3, Coroutines, `org.tensorflow:tensorflow-lite` for on-device ML feature extraction.
- **Backend Server**: Python 3.10+, FastAPI, Uvicorn.
- **Vector Database**: ChromaDB for embedding storage and semantic similarity search.
- **On-Device & Edge ML**: TensorFlow Lite (TFLite) for text embedding calculations and local similarity scoring.

---

## 📁 Repository Structure

```
App/
├── backend/
│   ├── main.py               # FastAPI entrypoint with CORS & routes
│   ├── pdf_processor.py      # PDF text extraction & semantic chunking
│   ├── vector_store.py       # ChromaDB vector database manager
│   ├── ai_generator.py       # Summarizer, RAG Q&A, Flashcards & Quiz engine
│   ├── tflite_inference.py   # TensorFlow Lite Python inference helper
│   ├── create_sample_pdf.py  # Sample PDF builder for testing
│   ├── test_backend.py       # Full pipeline test script
│   └── requirements.txt      # Python dependencies
│
├── android/
│   ├── build.gradle.kts      # Project-level Gradle build
│   ├── settings.gradle.kts   # Project settings
│   └── app/
│       ├── build.gradle.kts  # App-level Gradle build (Compose, Retrofit, TFLite)
│       └── src/main/
│           ├── AndroidManifest.xml
│           └── java/com/pdfai/app/
│               ├── MainActivity.kt
│               ├── data/
│               │   ├── api/          # RetrofitClient & PdfAiApiService
│               │   ├── model/        # Kotlin DTO Data Models
│               │   └── tflite/       # TFLiteManager (On-Device ML)
│               └── ui/
│                   ├── components/   # BottomNavBar & Navigation
│                   ├── screens/      # HomeScreen, SummaryScreen, QaScreen, FlashcardsScreen, QuizScreen
│                   └── theme/        # Glassmorphic Dark Design System
└── README.md
```

---

## 🚀 Getting Started

### 1. Running the FastAPI Backend

Navigate to the `backend/` folder, install requirements, and run Uvicorn:

```bash
cd backend
pip install -r requirements.txt
python main.py
```

The FastAPI server will start at `http://localhost:8000`. You can inspect interactive API docs at `http://localhost:8000/docs`.

To test the backend locally without running a web server:
```bash
python test_backend.py
```

### 2. API Endpoints

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/upload` | Upload PDF file, extract text, chunk, and index into ChromaDB. |
| `GET` | `/api/summary` | Generate executive summary, key insights, reading time, and topics. |
| `POST` | `/api/qa` | Perform RAG search on ChromaDB context & answer questions. |
| `GET` | `/api/flashcards` | Generate interactive flashcard deck (Q&A pairs). |
| `GET` | `/api/quiz` | Generate multiple-choice quiz questions with explanations. |
| `POST` | `/api/tflite/similarity` | Compute similarity using TFLite text embedding model. |

---

### 3. Running the Android Application

1. Open the `android/` directory in **Android Studio (Hedgehog or newer)**.
2. Build and Sync Gradle project (`Sync Project with Gradle Files`).
3. Run the app on an Android Emulator or connected Physical Device (Android 7.0+ / API 24+).
4. **Connecting to Local Backend**:
   - On **Android Emulator**, use `http://10.0.2.2:8000` (pre-configured in `RetrofitClient`).
   - On a **Physical Device**, enter your machine's local IP address (e.g. `http://192.168.1.100:8000`) in the server endpoint setting on the app's home screen.

---

## 📱 App Features & Screens

1. **PDF Upload & Indexing (`HomeScreen.kt`)**: Select any PDF document from device storage, upload to FastAPI, view chunk counts, and execute local TFLite neural model similarity tests.
2. **AI Summary (`SummaryScreen.kt`)**: Displays reading time, total word count, topic tags, high-density executive summary, and key takeaways.
3. **Document Q&A (`QaScreen.kt`)**: Interactive chat view powered by ChromaDB RAG vector search, featuring source text references and confidence indicators.
4. **Flashcards (`FlashcardsScreen.kt`)**: Animated 3D flip card study deck with difficulty tags and mastery progress tracking.
5. **Interactive Quiz (`QuizScreen.kt`)**: Test comprehension with multiple-choice questions, immediate correct/incorrect option highlights, explanation cards, and final score summary.
