from fastapi import FastAPI, File, UploadFile, HTTPException, Query
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import List, Optional, Dict, Any

from pdf_processor import PDFProcessor
from vector_store import VectorStoreManager
from ai_generator import AIGenerator
from tflite_inference import TFLiteTextEngine

app = FastAPI(
    title="PDF AI High-Precision Assistant API",
    description="Hybrid RAG (Vector + BM25) with page-level verification",
    version="3.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

vector_store = VectorStoreManager()
tflite_engine = TFLiteTextEngine()
current_pdf_state = {
    "filename": "",
    "pages_data": [],
    "chunks": [],
    "total_words": 0,
    "total_pages": 0
}

class QARequest(BaseModel):
    question: str
    top_k: Optional[int] = 5

class SimilarityRequest(BaseModel):
    text1: str
    text2: str

@app.get("/")
def read_root():
    return {
        "status": "online",
        "service": "PDF AI High-Precision Engine",
        "document_loaded": bool(current_pdf_state["pages_data"]),
        "filename": current_pdf_state["filename"],
        "total_pages": current_pdf_state["total_pages"],
        "total_chunks": len(current_pdf_state["chunks"])
    }

@app.post("/api/upload")
async def upload_pdf(file: UploadFile = File(...)):
    """Upload PDF, perform structural cleaning, semantic sentence chunking, and Hybrid RAG indexing."""
    if not file.filename.endswith(".pdf"):
        raise HTTPException(status_code=400, detail="Only PDF files are supported.")

    pdf_bytes = await file.read()
    if not pdf_bytes:
        raise HTTPException(status_code=400, detail="Uploaded PDF file is empty.")

    try:
        pages_data = PDFProcessor.extract_pages_from_bytes(pdf_bytes)
        if not pages_data:
            raise HTTPException(status_code=400, detail="Could not extract readable text from PDF.")

        # Sentence-boundary semantic chunking
        chunks = PDFProcessor.chunk_pages_semantically(pages_data, max_chars=600, overlap_chars=150)
        
        # Hybrid Indexing (Vector + BM25)
        global vector_store
        vector_store = VectorStoreManager()
        vector_store.add_chunks(chunks)

        total_words = sum(p["word_count"] for p in pages_data)
        current_pdf_state["filename"] = file.filename
        current_pdf_state["pages_data"] = pages_data
        current_pdf_state["chunks"] = chunks
        current_pdf_state["total_words"] = total_words
        current_pdf_state["total_pages"] = len(pages_data)

        return {
            "status": "success",
            "message": f"Indexed {len(pages_data)} pages ({len(chunks)} semantic chunks) into Hybrid Vector Store",
            "filename": file.filename,
            "total_pages": len(pages_data),
            "word_count": total_words,
            "total_chunks": len(chunks)
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error processing PDF: {str(e)}")

@app.get("/api/summary")
def get_summary():
    """Get high-accuracy document summary."""
    if not current_pdf_state["pages_data"]:
        raise HTTPException(status_code=400, detail="No PDF document uploaded yet.")

    summary_data = AIGenerator.generate_summary(current_pdf_state["pages_data"])
    summary_data["filename"] = current_pdf_state["filename"]
    summary_data["total_pages"] = current_pdf_state["total_pages"]
    return summary_data

@app.post("/api/qa")
def ask_question(request: QARequest):
    """Perform Hybrid RAG search (ChromaDB + BM25) and synthesize page-cited answer."""
    if not current_pdf_state["pages_data"]:
        raise HTTPException(status_code=400, detail="No PDF document uploaded yet.")

    retrieved_matches = vector_store.query_hybrid(request.question, n_results=request.top_k or 5)
    result = AIGenerator.answer_question(request.question, retrieved_matches)
    return result

@app.get("/api/flashcards")
def get_flashcards(count: int = Query(default=5, ge=1, le=20)):
    if not current_pdf_state["pages_data"]:
        raise HTTPException(status_code=400, detail="No PDF document uploaded yet.")

    cards = AIGenerator.generate_flashcards(current_pdf_state["pages_data"], count=count)
    return {"count": len(cards), "flashcards": cards}

@app.get("/api/quiz")
def get_quiz(count: int = Query(default=5, ge=1, le=10)):
    if not current_pdf_state["pages_data"]:
        raise HTTPException(status_code=400, detail="No PDF document uploaded yet.")

    quiz_questions = AIGenerator.generate_quiz(current_pdf_state["pages_data"], count=count)
    return {"count": len(quiz_questions), "quiz": quiz_questions}

@app.post("/api/tflite/similarity")
def tflite_similarity(request: SimilarityRequest):
    score = tflite_engine.calculate_tflite_similarity(request.text1, request.text2)
    return {
        "text1": request.text1,
        "text2": request.text2,
        "tflite_cosine_similarity": score
    }

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="127.0.0.1", port=8000, reload=True)
