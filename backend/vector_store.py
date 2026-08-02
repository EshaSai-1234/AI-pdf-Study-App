import re
import math
import chromadb
from chromadb.utils import embedding_functions
from typing import List, Dict, Any

class VectorStoreManager:
    """Hybrid RAG Manager combining ChromaDB dense vector search with sparse BM25 keyword re-ranking."""

    def __init__(self, collection_name: str = "pdf_ai_documents_v3"):
        self.client = chromadb.Client()
        self.embedding_fn = embedding_functions.DefaultEmbeddingFunction()
        
        try:
            self.client.delete_collection(name=collection_name)
        except Exception:
            pass

        self.collection = self.client.create_collection(
            name=collection_name,
            embedding_function=self.embedding_fn
        )
        self.all_chunks_cache: List[Dict[str, Any]] = []

    def add_chunks(self, chunks: List[Dict[str, Any]]):
        """Index chunks with page metadata in ChromaDB and cache for BM25 re-ranking."""
        if not chunks:
            return

        self.all_chunks_cache = chunks
        documents = [c["text"] for c in chunks]
        ids = [c["id"] for c in chunks]
        metadatas = [
            {
                "page_number": c["page_number"],
                "word_count": c["word_count"]
            } for c in chunks
        ]

        self.collection.add(
            documents=documents,
            ids=ids,
            metadatas=metadatas
        )

    def _bm25_score(self, query: str, document: str) -> float:
        """Calculate BM25 term overlap score supporting short terms like AI, ML, 5G, numbers."""
        q_words = re.findall(r'\b[a-zA-Z0-9]{1,}\b', query.lower())
        d_words = re.findall(r'\b[a-zA-Z0-9]{1,}\b', document.lower())
        
        # Exclude common noisy stopwords
        stopwords = {"the", "is", "at", "which", "on", "a", "an", "and", "or", "in", "to", "for", "of", "with", "what", "how", "why", "who", "where", "can", "does", "do", "are", "it"}
        q_filtered = [w for w in q_words if w not in stopwords]
        if not q_filtered:
            q_filtered = q_words

        if not q_filtered or not d_words:
            return 0.0

        doc_len = len(d_words)
        avg_len = 150.0
        k1 = 1.5
        b = 0.75

        score = 0.0
        for word in set(q_filtered):
            count = d_words.count(word)
            if count > 0:
                tf = (count * (k1 + 1)) / (count + k1 * (1 - b + b * (doc_len / avg_len)))
                score += tf

        # Boost exact multi-word phrase match
        query_strip = query.strip().lower()
        doc_lower = document.lower()
        if len(query_strip) > 4 and query_strip in doc_lower:
            score += 3.0

        # Boost key phrase matches (e.g. "multimodal ai models" or "popular multimodal")
        words_in_q = [w for w in q_filtered if len(w) > 3]
        if len(words_in_q) >= 2:
            bigrams = [" ".join(words_in_q[i:i+2]) for i in range(len(words_in_q)-1)]
            for bg in bigrams:
                if bg in doc_lower:
                    score += 1.5

        # Boost chunks containing numbered item lists when query asks about models/types/list
        if any(kw in query_strip for kw in ["model", "type", "popular", "list", "example"]) and re.search(r'\d+\.\s+[A-Z]', document):
            score += 2.0

        return score

    def query_hybrid(self, query_text: str, n_results: int = 5) -> List[Dict[str, Any]]:
        """Perform Hybrid RAG Retrieval (ChromaDB Dense Vector + Sparse BM25 Keyword Scoring + RRF)."""
        if self.collection.count() == 0:
            return []

        actual_n = min(max(n_results * 3, 10), self.collection.count())
        vector_results = self.collection.query(
            query_texts=[query_text],
            n_results=actual_n
        )

        matches = []
        if vector_results and "documents" in vector_results and vector_results["documents"]:
            docs = vector_results["documents"][0]
            ids = vector_results["ids"][0] if "ids" in vector_results else []
            metas = vector_results["metadatas"][0] if "metadatas" in vector_results and vector_results["metadatas"] else []
            distances = vector_results["distances"][0] if "distances" in vector_results and vector_results["distances"] else []

            for i in range(len(docs)):
                text = docs[i]
                page_num = metas[i].get("page_number", 1) if i < len(metas) else 1
                dist = float(distances[i]) if i < len(distances) else 0.5
                
                # Dense vector score [0..1]
                vector_score = max(0.0, min(1.0, 1.0 - (dist / 2.0)))
                # Sparse BM25 score
                bm25_score = self._bm25_score(query_text, text)
                
                # Hybrid RRF score
                hybrid_score = round((vector_score * 0.5) + (min(1.0, bm25_score * 0.35) * 0.5), 4)

                matches.append({
                    "id": ids[i] if i < len(ids) else f"match_{i}",
                    "text": text,
                    "page_number": page_num,
                    "score": hybrid_score,
                    "vector_score": round(vector_score, 4),
                    "bm25_score": round(bm25_score, 4)
                })

        # Sort matches by hybrid score descending
        matches.sort(key=lambda x: x["score"], reverse=True)
        return matches[:n_results]

    def query_similar(self, query_text: str, n_results: int = 5) -> List[Dict[str, Any]]:
        """Helper alias for query_hybrid to maintain backward compatibility."""
        return self.query_hybrid(query_text, n_results=n_results)
