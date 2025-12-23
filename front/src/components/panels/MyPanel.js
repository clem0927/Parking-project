// src/components/panels/MyPanel.js
import React, { useState, useEffect } from "react";
import axios from "axios";
import "../../css/MyPanel.css";

export default function MyPanel({
                                    onSelectFavorite,
                                    onRemoveFavorite,
                                }) {
    const favorites = [
        { parkId: "P001", name: "종묘주차장 공영주차장(시)" },
        { parkId: "P002", name: "청계1(북) 공영주차장(시)" },
        { parkId: "P003", name: "세종로 공영주차장(시)" },
    ];

    const parkingList = [
        {
            PKLT_CD: "P001",
            PKLT_CHRG_YN: "Y",
            OPERT_BEGIN_TM: "08:00",
            OPERT_END_TM: "22:00",
            remainCnt: 813,
            totalCnt: 20,
        },
        {
            PKLT_CD: "P002",
            PKLT_CHRG_YN: "N",
            OPERT_BEGIN_TM: "00:00",
            OPERT_END_TM: "24:00",
            remainCnt: 4,
            totalCnt: 30,
        },
        {
            PKLT_CD: "P003",
            PKLT_CHRG_YN: "Y",
            OPERT_BEGIN_TM: "09:00",
            OPERT_END_TM: "21:00",
            remainCnt: 869,
            totalCnt: 15,
        },
    ];

    const hasFavorites = favorites && favorites.length > 0;

    // --- 포인트 상태 관리 ---
    const [point, setPoint] = useState(0);
    const [chargeAmount, setChargeAmount] = useState("");

    // 페이지 로딩 시 포인트 조회
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
                setPoint(res.data);      // 충전 후 포인트 갱신
                setChargeAmount("");      // 입력 초기화
                alert(`${amount}P 충전 완료!`);
            })
            .catch(err => {
                console.error(err);
                alert("충전 실패");
            });
    };

    return (
        <>
            {/* 즐겨찾기 영역 */}
            <div className="panel my-panel">
                <h3 className="my-panel-title">즐겨찾기</h3>
                {!hasFavorites && (
                    <div className="my-empty">
                        아직 즐겨찾기한 주차장이 없습니다.
                        <br />
                        지도나 목록에서 마음에 드는 주차장을 즐겨찾기로 등록해 보세요.
                    </div>
                )}

                {hasFavorites && (
                    <ul className="my-fav-list">
                        {favorites.map((fav) => {
                            const park = parkingList.find((p) => p.PKLT_CD === fav.parkId);
                            const remain =
                                park && park.remainCnt != null ? `${park.remainCnt}면` : "-";

                            return (
                                <li key={fav.parkId} className="my-fav-item">
                                    <div className="my-fav-main">
                                        <div className="my-fav-name">{fav.name}</div>
                                        <div className="my-fav-meta">
                                            여석: {remain}
                                        </div>
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

            <p></p>

            {/* 포인트 충전 영역 */}
            <div className="panel my-panel">
                <h3 className="my-panel-title">포인트 충전</h3>
                <div style={{ color: "black", fontWeight: "bold" }}>
                    내 포인트: {point}P
                </div>
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
                        style={{ marginLeft: "5px",fontSize:"15px" }}
                        onClick={handleCharge}
                    >
                        충전
                    </button>
                </div>
            </div>
        </>
    );
}
