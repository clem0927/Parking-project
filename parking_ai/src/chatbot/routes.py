# src/chatbot/routes.py
import csv
import requests
import numpy as np
from flask import Blueprint, request, jsonify
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity

# ==============================
# Blueprint 생성
# ==============================
chat_bp = Blueprint("chat", __name__)

# ==============================
# Ollama 모델 설정
# ==============================
model_name = "gemma3:4b"
api_url = "http://localhost:11434/api/chat"

# ==============================
# 회사 문서 CSV 경로
# (server.py 기준 실행이므로 경로 주의)
# ==============================
csv_path = "./data/parking_docs.csv"

# ==============================
# 전역 변수
# ==============================
chat_history: list[dict] = []
documents: list[dict] = []
corpus: list[str] = []
vectorizer: TfidfVectorizer | None = None
doc_vectors = None

# ==============================
# 한글 텍스트 정규화
# ==============================
def normalize_korean_text(text: str) -> str:
    if not text:
        return ""
    return " ".join(text.strip().split())

# ==============================
# CSV 문서 로드
# ==============================
def load_documents_from_csv(path: str) -> list[dict]:
    docs: list[dict] = []
    with open(path, newline="", encoding="utf-8-sig") as f:
        reader = csv.DictReader(f)
        for row in reader:
            text = (row.get("text") or "").strip()
            intent = (row.get("intent") or "").strip()
            if not text or not intent:
                continue
            docs.append({"title": text, "content": intent})
    if not docs:
        raise ValueError("CSV에서 유효한 문서를 하나도 읽지 못했다.")
    return docs

def build_tfidf_index(docs: list[dict]):
    local_corpus: list[str] = []
    for doc in docs:
        text = (doc["title"] + "\n" + doc["content"]).strip()
        local_corpus.append(text)

    local_vectorizer = TfidfVectorizer(
        preprocessor=normalize_korean_text,
        ngram_range=(1, 2)
    )
    local_doc_vectors = local_vectorizer.fit_transform(local_corpus)
    return local_corpus, local_vectorizer, local_doc_vectors

# ==============================
# 서버 시작 시 CSV 로드 & 벡터화
# (Blueprint import 시 1회 실행)
# ==============================
try:
    documents = load_documents_from_csv(csv_path)
    corpus, vectorizer, doc_vectors = build_tfidf_index(documents)
    print(f"[INFO] CSV 문서 {len(documents)}개 로드 완료")
except Exception as e:
    print(f"[WARN] 지식 베이스 초기화 실패: {e}")
    documents = []
    corpus = []
    vectorizer = None
    doc_vectors = None

# ==============================
# 벡터 검색
# ==============================
def retrieve_top_docs(query: str, top_k: int = 3):
    if not vectorizer or doc_vectors is None:
        return []

    query_vec = vectorizer.transform([query])
    sims = cosine_similarity(query_vec, doc_vectors)[0]
    ranked_indices = np.argsort(-sims)

    results = []
    for idx in ranked_indices[:top_k]:
        results.append((documents[idx], float(sims[idx])))

    return results

def format_context(docs_with_scores, min_score: float = 0.0) -> str:
    if not docs_with_scores:
        return "관련된 회사 문서를 찾지 못했다."

    lines: list[str] = []
    for i, (doc, score) in enumerate(docs_with_scores, start=1):
        if score < min_score:
            continue

        title = doc["title"] or f"문서 {i}"
        content = doc["content"] or ""

        lines.append(f"[문서 {i}] 질문: {title}")
        lines.append(f"내용: {content}")
        lines.append("")

    return "\n".join(lines) if lines else "관련된 회사 문서를 찾지 못했다."

def build_context_text(user_message: str) -> str:
    top_docs = retrieve_top_docs(user_message, top_k=5)
    return format_context(top_docs)

# ==============================
# Ollama API 호출
# ==============================
def ask_ollama(user_message: str, context_text: str) -> str:
    system_role_message = {
        "role": "system",
        "content": (
            "너는 parking_docs.csv를 기반으로 주차장 서비스를 소개하는 챗봇이야. "
            "CSV파일에 있는 질문을 우선으로 하되 상식적인 내용은 자연스럽게 설명해."
        ),
    }

    context_message = {
        "role": "system",
        "content": (
            "다음은 주차 CSV에서 검색된 관련 문서다.\n"
            f"{context_text}\n\n"
            "위 문서 내용에서만 정보를 추출해 한국어로 답하라. "
            "관련 내용이 없으면 '서비스에 관련된 내용이 아니라 답변을 드릴 수 없습니다.'라고만 말하라."
        ),
    }

    messages: list[dict] = [system_role_message, context_message]

    if chat_history:
        messages.extend(chat_history[-8:])

    messages.append({"role": "user", "content": user_message})

    payload = {
        "model": model_name,
        "messages": messages,
        "stream": False
    }

    resp = requests.post(api_url, json=payload, timeout=120)
    resp.raise_for_status()
    data = resp.json()

    return data.get("message", {}).get("content", "").strip()

# ==============================
# ✅ 챗봇 API 라우트
# ==============================
@chat_bp.route("/chat", methods=["POST"])
def chat():
    data = request.get_json(silent=True) or {}
    user_message = data.get("message", "").strip()

    if not user_message:
        return jsonify({"error": "메시지가 비어 있습니다."}), 400

    if not documents:
        return jsonify({"error": "지식 베이스가 초기화되지 않았습니다."}), 500

    try:
        context_text = build_context_text(user_message)
        answer = ask_ollama(user_message, context_text)

        chat_history.append({"role": "user", "content": user_message})
        chat_history.append({"role": "assistant", "content": answer})

        if len(chat_history) > 20:
            del chat_history[:-20]

        return jsonify({"answer": answer})

    except Exception as e:
        return jsonify({"error": str(e)}), 500
