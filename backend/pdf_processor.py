import io
import re
from typing import List, Dict, Any
from pypdf import PdfReader

class PDFProcessor:
    """Ultra-precision PDF processor with sentence/paragraph boundary chunking and structural cleaning."""

    @staticmethod
    def extract_pages_from_bytes(pdf_bytes: bytes) -> List[Dict[str, Any]]:
        """Extract and clean text from every page of the PDF."""
        reader = PdfReader(io.BytesIO(pdf_bytes))
        pages_data = []

        for idx, page in enumerate(reader.pages):
            raw_text = page.extract_text() or ""
            
            # Replace bullet symbols with standard newlines or clean markers
            text = re.sub(r'[•\uf0a7\u2022]', '\n- ', raw_text)
            # Insert newline before numbered items like " 1. Google Gemini", " 2. GPT-4V"
            text = re.sub(r'(\s+)(?=\d+\.\s+[A-Z])', r'\n', text)
            # Clean up hyphenated line-breaks (e.g., "ma- \n chine" -> "machine")
            text = re.sub(r'(\w+)-\s*\n\s*(\w+)', r'\1\2', text)
            # Normalize carriage returns
            text = text.replace('\r\n', '\n').replace('\r', '\n')
            # Replace single line-breaks inside normal sentences with spaces, preserve paragraph double line-breaks and lists
            text = re.sub(r'(?<!\n)\n(?!\n|-|\d+\.)', ' ', text)
            # Compress multiple spaces
            text = re.sub(r'[ \t]+', ' ', text).strip()

            if text:
                lines = [line.strip() for line in text.split('\n') if line.strip()]
                heading = lines[0] if lines else f"Section {idx + 1}"

                pages_data.append({
                    "page_number": idx + 1,
                    "heading": heading[:80],
                    "text": text,
                    "word_count": len(text.split())
                })

        return pages_data

    @staticmethod
    def extract_text_from_bytes(pdf_bytes: bytes) -> str:
        """Helper for extracting concatenated text across all pages."""
        pages = PDFProcessor.extract_pages_from_bytes(pdf_bytes)
        return "\n\n".join(p["text"] for p in pages)

    @staticmethod
    def chunk_pages_semantically(pages_data: List[Dict[str, Any]], max_chars: int = 800, overlap_chars: int = 200) -> List[Dict[str, Any]]:
        """Split text into precise semantic chunks aligned to paragraph and sentence boundaries."""
        all_chunks = []
        chunk_counter = 0

        for page_info in pages_data:
            page_num = page_info["page_number"]
            page_text = page_info["text"]
            heading = page_info.get("heading", "")
            
            # Split text into paragraphs first, then sentences
            paragraphs = [p.strip() for p in page_text.split('\n\n') if p.strip()]
            units = []
            for p in paragraphs:
                # Split paragraph into clean sentence/clause units
                sentences = [s.strip() for s in re.split(r'(?<=[.!?])\s+', p) if s.strip()]
                if sentences:
                    units.extend(sentences)
                else:
                    units.append(p)

            current_chunk_units = []
            current_len = 0

            for unit in units:
                current_chunk_units.append(unit)
                current_len += len(unit) + 1

                if current_len >= max_chars:
                    chunk_text = " ".join(current_chunk_units)
                    all_chunks.append({
                        "id": f"p{page_num}_c{chunk_counter}",
                        "text": chunk_text,
                        "page_number": page_num,
                        "heading": heading,
                        "word_count": len(chunk_text.split()),
                        "snippet": chunk_text[:140] + "..." if len(chunk_text) > 140 else chunk_text
                    })
                    chunk_counter += 1

                    # Keep last 1-2 units for overlap context
                    current_chunk_units = current_chunk_units[-2:] if len(current_chunk_units) >= 2 else current_chunk_units[-1:]
                    current_len = sum(len(u) + 1 for u in current_chunk_units)

            if current_chunk_units:
                chunk_text = " ".join(current_chunk_units)
                all_chunks.append({
                    "id": f"p{page_num}_c{chunk_counter}",
                    "text": chunk_text,
                    "page_number": page_num,
                    "heading": heading,
                    "word_count": len(chunk_text.split()),
                    "snippet": chunk_text[:140] + "..." if len(chunk_text) > 140 else chunk_text
                })
                chunk_counter += 1

        return all_chunks

    @staticmethod
    def chunk_text(text: str, chunk_size: int = 800, overlap: int = 200) -> List[Dict[str, Any]]:
        """Helper for chunking raw text string into dictionary chunks."""
        fake_page = [{"page_number": 1, "heading": "Document", "text": text, "word_count": len(text.split())}]
        return PDFProcessor.chunk_pages_semantically(fake_page, max_chars=chunk_size, overlap_chars=overlap)
