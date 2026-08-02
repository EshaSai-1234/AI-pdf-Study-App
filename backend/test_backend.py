import os
from pdf_processor import PDFProcessor
from vector_store import VectorStoreManager
from ai_generator import AIGenerator
from tflite_inference import TFLiteTextEngine
from create_sample_pdf import create_sample_pdf

def test_full_pipeline():
    print("--- 1. Generating Sample PDF ---")
    pdf_filename = "sample_test.pdf"
    create_sample_pdf(pdf_filename)

    with open(pdf_filename, "rb") as f:
        pdf_bytes = f.read()

    print("\n--- 2. PDF Extraction & Chunking ---")
    pages_data = PDFProcessor.extract_pages_from_bytes(pdf_bytes)
    extracted_text = PDFProcessor.extract_text_from_bytes(pdf_bytes)
    print(f"Extracted Pages: {len(pages_data)}, Total Length: {len(extracted_text)} chars")
    print(f"Extracted Text Preview:\n{extracted_text[:180]}...")

    chunks = PDFProcessor.chunk_pages_semantically(pages_data, max_chars=400, overlap_chars=100)
    print(f"Generated {len(chunks)} chunks.")

    print("\n--- 3. ChromaDB Vector Store Indexing & Search ---")
    vs = VectorStoreManager(collection_name="test_collection")
    vs.add_chunks(chunks)
    
    query = "What is ChromaDB used for?"
    matches = vs.query_similar(query, n_results=3)
    print(f"Query: '{query}'")
    for m in matches:
        print(f" -> Match (Score {m['score']}): {m['text']}")

    print("\n--- 4. AI Generator (Summary, Q&A, Flashcards, Quiz) ---")
    summary = AIGenerator.generate_summary(pages_data)
    print(f"Summary: {summary['summary']}")
    print(f"Topics: {summary['topics']}")

    qa_res = AIGenerator.answer_question("What is TensorFlow Lite?", matches)
    print(f"Q&A Answer: {qa_res['answer']}")

    cards = AIGenerator.generate_flashcards(pages_data, count=2)
    print(f"Flashcards Generated: {len(cards)}")
    for c in cards:
        print(f" [Flashcard {c['id']}] Q: {c['question']} | A: {c['answer']}")

    quiz = AIGenerator.generate_quiz(pages_data, count=2)
    print(f"Quiz Questions Generated: {len(quiz)}")
    for q in quiz:
        print(f" [Quiz {q['id']}] Question: {q['question']}")
        print(f"   Options: {q['options']}")

    print("\n--- 5. TensorFlow Lite Engine Test ---")
    tflite = TFLiteTextEngine()
    sim = tflite.calculate_tflite_similarity("TensorFlow Lite Android", "On-device mobile machine learning")
    print(f"TFLite Similarity Score: {sim}")

    print("\nALL BACKEND PIPELINE TESTS COMPLETED SUCCESSFULLY!")

if __name__ == "__main__":
    test_full_pipeline()
