import React, { useState, useEffect } from "react";

// 거리 계산 함수 (Haversine 공식)
const calculateDistance = (lat1, lng1, lat2, lng2) => {
    const R = 6371; // 지구 반경 (km)
    const dLat = ((lat2 - lat1) * Math.PI) / 180;
    const dLng = ((lng2 - lng1) * Math.PI) / 180;
    const a =
        Math.sin(dLat / 2) ** 2 +
        Math.cos((lat1 * Math.PI) / 180) *
        Math.cos((lat2 * Math.PI) / 180) *
        Math.sin(dLng / 2) ** 2;
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R * c; // km 단위
};

export default function DrivePanel({ map, go, setGO, coordinates, ParkingList }) {
    const [nearbyParking, setNearbyParking] = useState([]);

    // 주행모드 on/off
    const handleSafeDriveClick = () => {
        setGO(prev => !prev);
    };

    // 지도 줌/이동 제한
    useEffect(() => {
        if (!map) return;
        if (go) {
            map.setLevel(1);
            map.setZoomable(false);
        } else {
            map.setLevel(3);
            map.setZoomable(true);
        }
    }, [go, map]);

    // 키보드 이동
    useEffect(() => {
        const handleKeyDown = (event) => {
            if (!map) return;
            const currentCenter = map.getCenter();
            const lat = currentCenter.getLat();
            const lng = currentCenter.getLng();
            const moveDistance = 0.0001;

            switch (event.key) {
                case "w": map.panTo(new window.kakao.maps.LatLng(lat + moveDistance, lng)); break;
                case "a": map.panTo(new window.kakao.maps.LatLng(lat, lng - moveDistance)); break;
                case "s": map.panTo(new window.kakao.maps.LatLng(lat - moveDistance, lng)); break;
                case "d": map.panTo(new window.kakao.maps.LatLng(lat, lng + moveDistance)); break;
                default: break;
            }
        };

        window.addEventListener("keydown", handleKeyDown);
        return () => window.removeEventListener("keydown", handleKeyDown);
    }, [map]);

    // 가까운 주차장 5개 계산
    useEffect(() => {
        if (!ParkingList || ParkingList.length === 0) return;

        const parksWithDistance = ParkingList.map((park) => {
            const lat = park.LAT ?? 0;
            const lng = park.LOT ?? 0;
            const distance = calculateDistance(coordinates.lat, coordinates.lng, lat, lng);
            return { ...park, distance };
        });

        const sorted = parksWithDistance
            .sort((a, b) => a.distance - b.distance)
            .slice(0, 5); // 상위 5개

        setNearbyParking(sorted);
    }, [ParkingList, coordinates]);

    return (
        <div>
            <div className="section-title">주행 모드</div>
            <div className="card">
                <div className="subtle">현재 목적지</div>
                <div style={{ marginTop: 6, fontWeight: 600 }}>목적지 미설정</div>
            </div>
            <button className="primary-btn-center" onClick={handleSafeDriveClick}>
                {go ? "안심 주행 종료" : "안심 주행"}
            </button>

            <div className="nearby-parking" style={{ marginTop: 12 }}>
                {go && nearbyParking.length > 0
                    ? nearbyParking.map((park, index) => (
                        <div key={index} className="card parking-card">
                            <div style={{ display: "flex", alignItems: "center" }}>
                                <div style={{ fontWeight: "bold" }}>
                                    {park.PKLT_NM ?? "이름없는 주차장"}
                                </div>
                                <button
                                    className="primary-btn-center"
                                    style={{ marginLeft: "auto" }}
                                    onClick={() => {
                                        const url = `https://map.kakao.com/link/to/${encodeURIComponent(
                                            park.PKLT_NM
                                        )},${park.LAT},${park.LOT}`;
                                        window.open(url, "_blank");
                                    }}
                                >
                                    길찾기
                                </button>
                            </div>
                            <div style={{ color: "#555" }}>
                                거리: {park.distance < 1
                                ? `${Math.round(park.distance * 1000)} m`
                                : `${park.distance.toFixed(2)} km`}
                            </div>

                            {/* 주차장 상세 정보 */}
                            <div style={{ marginTop: 4, fontSize: 12, color: "#333" }}>
                                <div>총자리: {park.TPKCT}</div>
                                <div>현재 대수: {park.liveCnt ?? "정보 없음"}</div>
                                <div>남은 자리: {park.remainCnt ?? "정보 없음"}</div>
                                <div>운영시간: {park.WD_OPER_BGNG_TM}-{park.WD_OPER_END_TM}</div>
                                <div>주차장 유형: {park.PKLT_KND_NM}</div>
                                <div>유료/무료: {park.CHGD_FREE_NM}</div>
                                <div>연락처: {park.TELNO ?? "-"}</div>
                            </div>
                        </div>
                    ))
                    : go
                        ? <div>주차장 탐색중...</div>
                        : null}
            </div>
        </div>
    );
}
