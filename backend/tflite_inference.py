import os
import numpy as np

class TFLiteTextEngine:
    """TensorFlow Lite NLP Helper for on-device and edge text processing."""

    def __init__(self, model_path: str = None):
        self.model_path = model_path
        self.interpreter = None
        
        # Check if TensorFlow or tflite_runtime is available
        try:
            import tensorflow as tf
            self.tf = tf
            self.has_tf = True
        except ImportError:
            self.has_tf = False

    def embed_text_tflite(self, text: str, vector_dim: int = 64) -> List[float] if 'List' in globals() else list:
        """Simulate or execute TFLite text embedding vector generation.
        
        In production, a MobileBERT or Universal Sentence Encoder .tflite model
        runs on-device. Here we provide a lightweight numerical projection for TFLite pipelines.
        """
        # Hash words to generate a float vector matching TFLite model output format
        words = text.lower().split()
        vector = np.zeros(vector_dim, dtype=np.float32)
        
        for idx, word in enumerate(words):
            hash_val = sum(ord(c) for c in word)
            slot = hash_val % vector_dim
            vector[slot] += 1.0

        # L2 normalize
        norm = np.linalg.norm(vector)
        if norm > 0:
            vector = vector / norm

        return vector.tolist()

    def calculate_tflite_similarity(self, text1: str, text2: str) -> float:
        """Calculate cosine similarity between TFLite embeddings."""
        v1 = np.array(self.embed_text_tflite(text1))
        v2 = np.array(self.embed_text_tflite(text2))
        
        dot_product = np.dot(v1, v2)
        return float(round(dot_product, 4))
