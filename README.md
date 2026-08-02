# AI & ML PDF Study Assistant (Kotlin + FastAPI + ChromaDB + TFLite + Web)

A high-precision, full-stack PDF AI Assistant application that parses uploaded PDF documents and provides interactive **Summaries**, **High-Precision RAG Q&A**, **Flashcards**, and **Multiple-Choice Quizzes** using **Kotlin Jetpack Compose**, **FastAPI**, **ChromaDB**, **TensorFlow Lite (TFLite)**, and modern **Web (HTML/CSS/JS)**.

---

## 🏗️ Architecture & Technology Stack

```
                               ┌─────────────────────────────────────────┐
                               │       Android App / Web Frontend        │
                               │   (Jetpack Compose UI & Glassmorphism)  │
                               └────────────────────┬────────────────────┘
                                                    │
                                       Retrofit REST API Requests
                                                    │
                                                    ▼
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                                Python FastAPI Backend Server                           │
│ ├──────────────────────────┬──────────────────────────┬──────────────────────────────┤ │
│ │  PyPDF Structural Engine │  ChromaDB + BM25 RAG     │  High-Precision AI Generator │ │
│ │  & Semantic Chunking     │  Hybrid Vector Store     │  (LLM & Standalone RAG)      │ │
│ └──────────────────────────┴──────────────────────────┴──────────────────────────────┘ │
└────────────────────────────────────────────────────────────────────────────────────────┘
```

- **Android Client**: Kotlin, Jetpack Compose (Material 3), Retrofit2, OkHttp3, Coroutines, `org.tensorflow:tensorflow-lite` for on-device ML feature extraction.
- **Web Client**: HTML5, Vanilla CSS3 (Glassmorphic Design), Modern JavaScript (Fetch API, RAG UI).
- **Backend Server**: Python 3.10+, FastAPI, Uvicorn.
- **Vector Store & Retrieval**: ChromaDB + BM25 Sparse Keyword Search with Reciprocal Rank Fusion (RRF) and exact-phrase boosting.
- **AI Answer Engine**: Supports **Google Gemini API** (`google-genai`), **OpenAI API**, and a standalone **Multi-Sentence RAG Synthesizer** (offline / un-keyed) producing clean answers without page citation clutter.
- **Edge ML**: TensorFlow Lite (TFLite) for text embedding calculations and local similarity scoring.

---

## 📊 Evaluation Metrics

The system evaluates RAG performance across two primary dimensions: **Retrieval Accuracy** and **Answer Generation Quality**.

### 1. Retrieval Metrics
| Metric | Description | Target Benchmark |
| :--- | :--- | :--- |
| **Hit Rate @ K** | Percentage of queries where at least one ground-truth passage appears in top-K matches. | `> 95%` (K=7) |
| **MRR (Mean Reciprocal Rank)** | Evaluates how close to the top position the first relevant chunk appears ($MRR = \frac{1}{|Q|} \sum \frac{1}{\text{rank}_i}$). | `> 0.88` |
| **Precision @ K** | Ratio of relevant retrieved passages to total retrieved passages. | `> 0.80` |
| **NDCG @ K** | Normalized Discounted Cumulative Gain measuring rank position and relevance decay. | `> 0.90` |

### 2. Answer Generation & Quality Metrics
| Metric | Description | Target Benchmark |
| :--- | :--- | :--- |
| **Faithfulness / Groundedness** | Degree to which the answer contains ONLY facts supported by retrieved PDF context (0 hallucination). | `99.2%` |
| **Answer Relevance** | Cosine similarity between query intent and generated answer summary. | `> 0.91` |
| **ROUGE-L / BLEU-4** | N-gram and longest common subsequence overlap against gold standard reference answers. | `ROUGE-L > 0.72` |
| **Response Latency** | End-to-end processing time from question submission to answer rendering. | `< 450 ms` (Local RAG) |

---

## 📁 Repository Structure

```
AI pdf Study App/
├── backend/
│   ├── main.py               # FastAPI entrypoint with CORS & API routes
│   ├── pdf_processor.py      # Structural PDF parser & semantic paragraph chunker
│   ├── vector_store.py       # Hybrid Vector Store (ChromaDB + BM25 + RRF)
│   ├── ai_generator.py       # High-Precision Q&A Synthesizer, Flashcards & Quiz engine
│   ├── tflite_inference.py   # TensorFlow Lite Python inference helper
│   ├── create_sample_pdf.py  # Sample PDF generator for automated testing
│   ├── test_backend.py       # Comprehensive backend test suite
│   └── requirements.txt      # Python backend dependencies
│
├── web/
│   ├── index.html            # Web interface HTML5 structure
│   ├── styles.css            # Glassmorphic Dark Design System
│   └── app.js                # Frontend logic & RAG chat handler
│
├── android/
│   ├── build.gradle.kts      # Project-level Gradle build
│   └── app/                  # Android Jetpack Compose app source
│
├── .gitignore                # Production ignore settings
└── README.md                 # Project documentation
```

---

## 🚀 Getting Started

### 1. Running the FastAPI Backend

Navigate to `backend/`, install dependencies, and launch Uvicorn:

```bash
cd backend
pip install -r requirements.txt
python main.py
```

The FastAPI server starts at `http://127.0.0.1:8000`. Interactive Swagger documentation is available at `http://127.0.0.1:8000/docs`.

### 2. Optional API Key Configuration
To enable LLM answer generation via Gemini or OpenAI, set environment variables before running Uvicorn:
```bash
# Optional: Set Gemini API key
export GEMINI_API_KEY="your-gemini-api-key"

# Optional: Set OpenAI API key
export OPENAI_API_KEY="your-openai-api-key"
```
*(If no API keys are provided, the system automatically uses its standalone Multi-Sentence RAG Synthesizer).*

### 3. Automated Verification Tests
Run the automated test suite to verify extraction, indexing, search ranking, and answer synthesis:
```bash
python backend/test_backend.py
```

---

## 📡 API Endpoints

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/upload` | Upload PDF file, extract text, chunk semantically, and index into Hybrid Vector Store. |
| `GET` | `/api/summary` | Generate executive summary, key insights, reading time, and topic tags. |
| `POST` | `/api/qa` | Perform Hybrid RAG search & synthesize high-precision answer (no page number clutter). |
| `GET` | `/api/flashcards` | Generate interactive flashcard deck with key concepts & definitions. |
| `GET` | `/api/quiz` | Generate multiple-choice quiz questions with answer options & explanations. |
| `POST` | `/api/tflite/similarity` | Compute neural embedding similarity via TFLite engine. |
