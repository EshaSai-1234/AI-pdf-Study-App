import os

def create_sample_pdf(file_path: str = "sample_ai_lecture.pdf"):
    """Generates a valid, sample PDF file containing structured educational content."""
    pdf_content = (
        "%PDF-1.4\n"
        "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n"
        "2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n"
        "3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>\nendobj\n"
        "4 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n"
        "5 0 obj\n<< /Length 580 >>\nstream\n"
        "BT\n"
        "/F1 18 Tf\n"
        "50 720 Td\n"
        "(Artificial Intelligence and Machine Learning Notes) Tj\n"
        "0 -30 Td\n"
        "/F1 12 Tf\n"
        "(Machine learning is a subset of artificial intelligence that allows systems) Tj\n"
        "0 -18 Td\n"
        "(to learn from data, identify patterns, and make automated decisions.) Tj\n"
        "0 -25 Td\n"
        "(Supervised learning algorithms rely on labeled datasets to train models) Tj\n"
        "0 -18 Td\n"
        "(such as linear regression, decision trees, and neural networks.) Tj\n"
        "0 -25 Td\n"
        "(ChromaDB serves as a vector database optimized for similarity search) Tj\n"
        "0 -18 Td\n"
        "(and retrieval-augmented generation (RAG) applications in LLM pipelines.) Tj\n"
        "0 -25 Td\n"
        "(TensorFlow Lite enables fast on-device inference for mobile Android apps) Tj\n"
        "0 -18 Td\n"
        "(by quantizing weights and reducing memory overhead efficiently.) Tj\n"
        "ET\n"
        "endstream\nendobj\n"
        "xref\n"
        "0 6\n"
        "0000000000 65535 f \n"
        "0000000009 00000 n \n"
        "0000000058 00000 n \n"
        "0000000115 00000 n \n"
        "0000000229 00000 n \n"
        "0000000300 00000 n \n"
        "trailer\n"
        "<< /Size 6 /Root 1 0 R >>\n"
        "startxref\n"
        "935\n"
        "%%EOF\n"
    )

    with open(file_path, "wb") as f:
        f.write(pdf_content.encode("latin-1"))

    print(f"Sample PDF created successfully at {file_path}")

if __name__ == "__main__":
    create_sample_pdf()
