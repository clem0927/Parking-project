import React, { useState, useRef, useEffect } from "react";
import "../css/ChatWidget.css";          // CSS 분리
import { FaComments, FaRobot, FaUser, FaPaperPlane, FaTimes } from "react-icons/fa";

export default function ChatWidget() {
    const [open, setOpen] = useState(false);
    const [messages, setMessages] = useState([]);
    const [input, setInput] = useState("");

    const chatLogRef = useRef(null);

    useEffect(() => {
        if (chatLogRef.current) {
            chatLogRef.current.scrollTop = chatLogRef.current.scrollHeight;
        }
    }, [messages]);

    const appendMessage = (text, role) => {
        setMessages(prev => [...prev, { text, role }]);
    };

    const sendMessage = async () => {
        const msg = input.trim();
        if (!msg) return;

        // 사용자 메시지 추가
        appendMessage(msg, "user");
        setInput("");

        // AI 고민중 메시지 추가
        const thinkingMessage = { text: "생각중...", role: "bot", thinking: true };
        setMessages(prev => [...prev, thinkingMessage]);

        try {
            const resp = await fetch("/ml/chat", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ message: msg })
            });

            const data = await resp.json();

            setMessages(prev => {
                // 마지막 메시지가 thinking이면 교체
                const updated = [...prev];
                const index = updated.findIndex(m => m.thinking);
                if (index !== -1) {
                    updated[index] = { text: data.answer, role: "bot" };
                } else {
                    updated.push({ text: data.answer, role: "bot" });
                }
                return updated;
            });

            if (!resp.ok) {
                console.error(data.error || "알 수 없는 오류");
            }
        } catch {
            setMessages(prev => {
                const updated = [...prev];
                const index = updated.findIndex(m => m.thinking);
                if (index !== -1) {
                    updated[index] = { text: "봇 오류: 서버에 연결할 수 없습니다", role: "bot" };
                }
                return updated;
            });
        }
    };

    const handleKey = (e) => {
        if (e.key === "Enter") sendMessage();
    };

    return (
        <>
            {/* 오른쪽 아래 플로팅 버튼 */}
            <div
                className="chat-fab"
                onClick={() => setOpen(!open)}
                title="업무 Q&A 챗봇 열기"
            >
                <FaComments />
            </div>

            {/* 챗봇 박스 */}
            {open && (
                <div className="chat-widget">
                    <div className="chat-widget-header">
                        <div className="chat-widget-header-left">
                            <div className="chat-widget-avatar"> <img
                                src="/images/car.png"  // 프로젝트 public 폴더 안 이미지 경로
                                alt="로봇"
                                style={{ background:"white",width: "100%", height: "100%", borderRadius: "50%" }}
                            /></div>
                            <div>
                                <div className="chat-widget-title">Ezpark 챗봇</div>
                                <div className="chat-widget-subtitle">
                                    주차장 서비스 챗봇에게 문의하세요!
                                </div>
                            </div>
                        </div>

                        <div className="chat-widget-close" onClick={() => setOpen(false)}>
                            <FaTimes />
                        </div>
                    </div>

                    <div className="chat-log" ref={chatLogRef}>
                        {messages.length === 0 ? (
                            <div className="chat-empty-text">
                                💬 아직 질문이 없네요..채팅을 시작하세요!
                            </div>
                        ) : (
                            messages.map((m, i) => (
                                <div key={i} className={`message ${m.role}-message`}>
                                    <div className="avatar">
                                        {m.role === "user" ? (
                                            <FaUser />
                                        ) : (
                                            <img
                                                src="/images/car.png"
                                                alt="로봇"
                                                style={{ background: "white", width: "100%", height: "100%", borderRadius: "50%" }}
                                            />
                                        )}
                                    </div>
                                    <div className="bubble" className={m.thinking ? "bubble thinking" : "bubble"}>
                                        {m.thinking ? "생각중" : m.text}
                                    </div>

                                </div>
                            ))
                        )}
                    </div>

                    <div className="chat-widget-input">
                        <input
                            type="text"
                            value={input}
                            placeholder="주차장 서비스에 대해 질문하세요!"
                            onChange={e => setInput(e.target.value)}
                            onKeyDown={handleKey}
                        />
                        <button id="send-btn" onClick={sendMessage}>
                            <FaPaperPlane /> 전송
                        </button>
                    </div>
                </div>
            )}
        </>
    );
}
