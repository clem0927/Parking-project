// src/components/panels/MyPanel.js
import React, { useState, useEffect } from "react";
import axios from "axios";
import "../../css/MyPanel.css";

export default function MyPanel({ onSelectFavorite, onRemoveFavorite }) {
    const [activeTab, setActiveTab] = useState("favorites"); // favorites, point, edit
    const [point, setPoint] = useState(0);
    const [chargeAmount, setChargeAmount] = useState("");
    const [user, setUser] = useState(null);
    useEffect(() => {
        fetch("/api/user/me", { credentials: "include" })
            .then(res => res.ok ? res.json() : null)
            .then(data => {
                setUser(data);
                console.log("로그인 유저 정보:", data); // 여기서 찍으면 fetch 결과 확인 가능
            })
            .catch(() => {
                setUser(null);
                console.log("유저 정보 가져오기 실패");
            });
    },[])
    const favorites = [
        { parkId: "P001", name: "종묘주차장 공영주차장(시)" },
        { parkId: "P002", name: "청계1(북) 공영주차장(시)" },
        { parkId: "P003", name: "세종로 공영주차장(시)" },
    ];

    const parkingList = [
        { PKLT_CD: "P001", PKLT_CHRG_YN: "Y", OPERT_BEGIN_TM: "08:00", OPERT_END_TM: "22:00", remainCnt: 813, totalCnt: 20 },
        { PKLT_CD: "P002", PKLT_CHRG_YN: "N", OPERT_BEGIN_TM: "00:00", OPERT_END_TM: "24:00", remainCnt: 4, totalCnt: 30 },
        { PKLT_CD: "P003", PKLT_CHRG_YN: "Y", OPERT_BEGIN_TM: "09:00", OPERT_END_TM: "21:00", remainCnt: 869, totalCnt: 15 },
    ];

    const hasFavorites = favorites.length > 0;

    useEffect(() => {
        axios.get("/api/auth/point")
            .then(res => setPoint(res.data))
            .catch(err => console.error("포인트 조회 실패", err));
    }, []);

    const handleCharge = () => {
        const amount = parseInt(chargeAmount);
        if (isNaN(amount) || amount <= 0) {
            alert("올바른 금액을 입력하세요.");
            return;
        }
        axios.post(`/api/auth/point/charge?amount=${amount}`)
            .then(res => {
                setPoint(res.data);
                setChargeAmount("");
                alert(`${amount}P 충전 완료!`);
            })
            .catch(err => {
                console.error(err);
                alert("충전 실패");
            });
    };

    return (
        <div className="panel my-panel">
            {/* 하위 내브바 */}
            <div className="my-subnav">
                <button
                    className={activeTab === "favorites" ? "active" : ""}
                    onClick={() => setActiveTab("favorites")}
                >
                    즐겨찾기
                </button>
                <button
                    className={activeTab === "point" ? "active" : ""}
                    onClick={() => setActiveTab("point")}
                >
                    포인트 충전
                </button>
                <button
                    className={activeTab === "edit" ? "active" : ""}
                    onClick={() => setActiveTab("edit")}
                >
                    정보 수정
                </button>
            </div>

            {/* 하위 탭 내용 */}
            {activeTab === "favorites" && (
                <div className="my-tab-content">
                    {!hasFavorites ? (
                        <div className="my-empty">
                            아직 즐겨찾기한 주차장이 없습니다.<br />
                            지도나 목록에서 마음에 드는 주차장을 즐겨찾기로 등록해 보세요.
                        </div>
                    ) : (
                        <ul className="my-fav-list">
                            {favorites.map((fav) => {
                                const park = parkingList.find((p) => p.PKLT_CD === fav.parkId);
                                const remain = park?.remainCnt != null ? `${park.remainCnt}면` : "-";

                                return (
                                    <li key={fav.parkId} className="my-fav-item">
                                        <div className="my-fav-main">
                                            <div className="my-fav-name">{fav.name}</div>
                                            <div className="my-fav-meta">여석: {remain}</div>
                                        </div>
                                        <div className="my-fav-actions">
                                            <button
                                                className="my-fav-btn my-fav-btn-primary"
                                                onClick={() => onSelectFavorite(fav)}
                                            >
                                                길안내
                                            </button>
                                            <button
                                                className="my-fav-btn my-fav-btn-ghost"
                                                onClick={() => onRemoveFavorite(fav.parkId)}
                                            >
                                                삭제
                                            </button>
                                        </div>
                                    </li>
                                );
                            })}
                        </ul>
                    )}
                </div>
            )}

            {activeTab === "point" && (
                <div className="my-tab-content">
                    <div style={{ color: "black", fontWeight: "bold" }}>내 포인트: {point}P</div>
                    <div style={{ marginTop: "5px" }}>
                        <input
                            type="number"
                            value={chargeAmount}
                            onChange={(e) => setChargeAmount(e.target.value)}
                            placeholder="충전 금액 입력"
                            style={{ width: "200px" }}
                        />
                        <button
                            className="my-fav-btn my-fav-btn-primary"
                            style={{ marginLeft: "5px", fontSize: "15px" }}
                            onClick={handleCharge}
                        >
                            충전
                        </button>
                    </div>
                </div>
            )}

            {activeTab === "edit" && user && (
                <div className="my-tab-content">
                    <h4>회원 정보 수정</h4>
                    <form
                        onSubmit={(e) => {
                            e.preventDefault();
                            const updates = {};
                            if (e.target.username.value.trim() !== "") updates.username = e.target.username.value.trim();
                            if (e.target.password.value.trim() !== "") updates.password = e.target.password.value.trim();

                            //수정
                            if (updates.username) {
                                axios.put("/api/user/me", updates, { withCredentials: true })
                                    .then(res => {
                                        setUser(res.data); // 수정 후 최신 정보 반영
                                        alert("수정 완료!");
                                    })
                                    .catch(err => {
                                        console.error(err);
                                        alert("수정 실패");
                                    });
                            }
                            e.target.reset();
                        }}
                    >
                        <div style={{ marginBottom: "8px" }}>
                            <label>닉네임 </label>
                            <input
                                type="text"
                                name="username"
                                placeholder="닉네임 입력"
                                defaultValue={user.username || ""}
                                style={{marginLeft:"10px"}}
                            />
                        </div>
                        <div style={{ marginBottom: "8px" }}>
                            <label>비밀번호 </label>
                            <input
                                type="password"
                                name="password"
                                placeholder="새 비밀번호 입력"
                                style={{width:"77%",marginLeft:"10px"}}
                            />
                        </div>
                        <button type="submit" className="my-fav-btn my-fav-btn-primary">
                            수정
                        </button>
                        <button
                            type="button"
                            className="danger"
                            onClick={() => {
                                if(window.confirm("정말 계정을 삭제하시겠습니까?")) {
                                    axios.delete("/api/user/me", { withCredentials: true })
                                        .then(res => {
                                            alert("계정 삭제 완료");
                                            setUser(null);
                                            // 필요시 리다이렉트
                                            window.location.reload();
                                        })
                                        .catch(err => {
                                            console.error(err);
                                            alert("계정 삭제 실패");
                                        });
                                }
                            }}
                        >
                            탈퇴
                        </button>
                    </form>
                </div>
            )}
        </div>
    );
}
