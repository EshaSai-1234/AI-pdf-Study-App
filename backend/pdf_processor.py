import io
import re
from typing import List, Dict, Any
from pypdf import PdfReader

class PDFProcessor:
    """Ultra-precision PDF processor with sentence-boundary chunking and structural cleaning."""

    @staticmethod
    def extract_pages_from_bytes(pdf_bytes: bytes) -> List[Dict[str, Any]]:
        """Extract and clean text from every page of the PDF."""
        reader = PdfReader(io.BytesIO(pdf_bytes))
        pages_data = []

        for idx, page in enumerate(reader.pages):
            raw_text = page.extract_text() or ""
            
            # Clean up hyphenated line-breaks (e.g., "ma- \n chine" -> "machine")
            text = re.sub(r'(\w+)-\s*\n\s*(\w+)', r'\1\2', raw_text)
            # Replace single line-breaks inside sentences with spaces, preserve double line-breaks (paragraphs)
            text = re.sub(r'(?<!\n)\n(?!\n)', ' ', text)
            text = re.sub(r'[ \t]+', ' ', text).strip()

            if text:
                # Extract page headings if available
                lines = [line.strip() for line in text.split('\n') if line.strip()]
                heading = lines[0] if lines else f"Page {idx + 1}"

                pages_data.append({
                    "page_number": idx + 1,
                    "heading": heading[:80],
                    "text": text,
                    "word_count": len(text.split())
                })

        return pages_data

    @staticmethod
    def chunk_pages_semantically(pages_data: List[Dict[str, Any]], max_chars: int = 600, overlap_chars: int = 150) -> List[Dict[str, Any]]:
        """Split text into precise semantic chunks aligned to sentence boundaries."""
        all_chunks = []
        chunk_counter = 0

        for page_info in pages_data:
            page_num = page_info["page_number"]
            page_text = page_info["text"]
            
            # Split text into clean sentences
            sentences = [s.strip() for s in re.split(r'(?<=[.!?])\s+', page_text) if s.strip()]
            
            current_chunk_sentences = []
            current_len = 0

            for sentence in sentences:
                current_chunk_sentences.append(sentence)
                current_len += len(sentence) + 1

                if current_len >= max_chars:
                    chunk_text = " ".join(current_chunk_sentences)
                    all_chunks.append({
                        "id": f"p{page_num}_c{chunk_counter}",
                        "text": chunk_text,
                        "page_number": page_num,
                        "word_count": len(chunk_text.split()),
                        "snippet": chunk_text[:140] + "..." if len(chunk_text) > 140 else chunk_text
                    })
                    chunk_counter += 1

                    # Keep last sentence for overlap
                    current_chunk_sentences = current_chunk_sentences[-1:] if len(current_chunk_sentences) > 1 else []
                    current_len = sum(len(s) + 1 for s in current_chunk_sentences)

            if current_chunk_sentences:
                chunk_text = " ".join(current_chunk_sentences)
                all_chunks.append({
                    "id": f"p{page_num}_c{chunk_counter}",
                    "text": chunk_text,
                    "page_number": page_num,
                    "word_count": len(chunk_text.split()),
                    "snippet": chunk_text[:140] + "..." if len(chunk_text) > 140 else chunk_text
                })
                chunk_counter += 1

        return all_chunks
