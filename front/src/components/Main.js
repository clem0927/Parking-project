import React, { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import "../css/Main.css";

import DestinationPanel from "./panels/DestinationPanel";
import DrivePanel from "./panels/DrivePanel";
import FavoritesPanel from "./panels/FavoritesPanel";

export default function Main() {
    const [mode, setMode] = useState("destination"); // destination | drive | favorites
    const [map, setMap] = useState(null);
    const [coordinates, setCoordinates] = useState({
        lat: 37.5662952,
        lng: 126.9779451,
    }); // 서울시청
    const [go, setGO] = useState(false);
    const [parkingList, setParkingList] = useState([]); // 최종 mergedParkingList 저장

    // 카카오 지도 초기화
    useEffect(() => {
        window.kakao.maps.load(() => {
            const container = document.getElementById("map");
            const options = {
                center: new window.kakao.maps.LatLng(coordinates.lat, coordinates.lng),
                level: 2,
            };
            const mapInstance = new window.kakao.maps.Map(container, options);
            setMap(mapInstance);

            // 현재 위치 마커
            const marker = new window.kakao.maps.Marker({
                position: new window.kakao.maps.LatLng(
                    coordinates.lat,
                    coordinates.lng
                ),
                map: mapInstance,
                title: "현재 위치",
                image: new window.kakao.maps.MarkerImage(
                    "/images/car.png",
                    new window.kakao.maps.Size(50, 50)
                ),
            });

            // 지도 중심 이동 시 현재 위치 업데이트
            window.kakao.maps.event.addListener(mapInstance, "center_changed", () => {
                const newCenter = mapInstance.getCenter();
                const lat = newCenter.getLat();
                const lng = newCenter.getLng();
                setCoordinates({ lat, lng });
                marker.setPosition(new window.kakao.maps.LatLng(lat, lng));
            });
        });
    }, []);

    useEffect(() => {
        if (!map) return;

        const fetchAndShowMarkers = async () => {
            const clusterer = new window.kakao.maps.MarkerClusterer({
                map: map,
                averageCenter: true,
                minLevel: 6,
            });

            try {
                // 1. 실시간 정보
                const realtimeResponse = await fetch(
                    `http://openapi.seoul.go.kr:8088/56776f4f766b696d3335704f6b434d/json/GetParkingInfo/1/1000/`
                );
                const realtimeData = await realtimeResponse.json();
                const realtimeList = realtimeData.GetParkingInfo?.row || [];
                console.log("실시간 정보:", realtimeList);

                // 2. 전체 주차장 정보
                let fullParkingList = [];
                for (let i = 0; i < 7; i++) {
                    const start = i * 1000 + 1;
                    const end = (i + 1) * 1000;
                    const response = await fetch(
                        `http://openapi.seoul.go.kr:8088/56776f4f766b696d3335704f6b434d/json/GetParkInfo/${start}/${end}/`
                    );
                    const result = await response.json();
                    const rows = result.GetParkInfo?.row || [];
                    fullParkingList = fullParkingList.concat(rows);
                }
                console.log("전체 주차장 리스트:", fullParkingList);

                // 3. 이름 기준 중복 제거 + 기존 속성 보존
                const parkMapByName = {};
                fullParkingList.forEach((park) => {
                    const name = park.PKLT_NM;
                    if (!name) return;

                    if (!parkMapByName[name]) {
                        // 기존 객체 그대로 복사
                        parkMapByName[name] = { ...park, LATs: [], LOTs: [] };
                    } else {
                        // 자리수 합산
                        parkMapByName[name].TPKCT += park.TPKCT;
                    }

                    parkMapByName[name].LATs.push(parseFloat(park.LAT));
                    parkMapByName[name].LOTs.push(parseFloat(park.LOT));
                });

                const uniqueParkingList = Object.values(parkMapByName).map((park) => ({
                    ...park,             // 기존 모든 필드 유지
                    LAT: park.LATs[0],   // 대표 좌표
                    LOT: park.LOTs[0],
                }));

                // 4. 실시간 데이터 이름 기준 합산
                const realtimeMapByName = {};
                realtimeList.forEach((item) => {
                    const name = item.PKLT_NM;
                    if (!name) return;
                    if (!realtimeMapByName[name]) realtimeMapByName[name] = 0;
                    realtimeMapByName[name] += item.NOW_PRK_VHCL_CNT ?? 0;
                });

                // 5. 정적 + 실시간 병합
                const mergedParkingList = uniqueParkingList.map((park) => {
                    const liveCnt = realtimeMapByName[park.PKLT_NM] ?? null;
                    const remainCnt = liveCnt != null ? park.TPKCT - liveCnt : null;
                    return { ...park, liveCnt, remainCnt };
                });

                // 상태 업데이트
                setParkingList(mergedParkingList);

                console.log("병합된 리스트 예시:", mergedParkingList.slice(0, 30));

                // 6. 마커 생성
                const markers = mergedParkingList
                    .map((park) => {
                        const marker = new window.kakao.maps.Marker({
                            position: new window.kakao.maps.LatLng(park.LAT, park.LOT),
                            title: park.PKLT_NM,
                        });

                        const infowindow = new window.kakao.maps.InfoWindow({
                            content: `
                            <div class="my-infowindow">
                                <h4>${park.PKLT_NM}</h4>
                                <p>총자리: ${park.TPKCT}</p>
                                <p>현재 대수: ${park.liveCnt ?? "정보 없음"}</p>
                                <p>남은 자리: ${park.remainCnt ?? "정보 없음"}</p>
                                <p>가격: ${park.PRK_CRG ?? "정보 없음"}원</p>
                            </div>
                        `,
                            removable: true // 오른쪽 상단 닫기 버튼 추가
                        });

                        window.kakao.maps.event.addListener(
                            marker,
                            "mouseover",
                            () => infowindow.open(map, marker)
                        );
                        window.kakao.maps.event.addListener(
                            marker,
                            "mouseout",
                            () => infowindow.close()
                        );

                        return marker;
                    })
                    .filter((m) => m !== null);

                clusterer.addMarkers(markers);
            } catch (err) {
                console.error("공공 API 불러오기 실패:", err);
            }
        };

        fetchAndShowMarkers();
    }, [map]);

    return (
        <div className="app">
            <aside className="sidebar">
                <div className="brand-row">
                    {go ? (
                        <div style={{ display: "flex", alignItems: "center" }}>
                            <div className="loading-circle"></div>
                            <span className="safe-driving-text">안심 주행중</span>
                        </div>
                    ) : (
                        <>
                            <div className="brand">Eazypark</div>
                            <span className="pill">Beta</span>
                        </>
                    )}
                </div>

                <div className={go ? "fade-out" : "fade-in"}>
                    <div className="tabs">
                        <button
                            className={`tab ${mode === "destination" ? "active" : ""}`}
                            onClick={() => setMode("destination")}
                        >
                            목적지
                        </button>
                        <button
                            className={`tab ${mode === "drive" ? "active" : ""}`}
                            onClick={() => setMode("drive")}
                        >
                            주행
                        </button>
                        <button
                            className={`tab ${mode === "favorites" ? "active" : ""}`}
                            onClick={() => setMode("favorites")}
                        >
                            즐겨찾기
                        </button>
                    </div>
                </div>

                <div className="panel-wrap">
                    {mode === "destination" && (
                        <DestinationPanel
                            map={map}
                            coordinates={coordinates}
                            ParkingList={parkingList}
                        />
                    )}
                    {mode === "drive" && (
                        <DrivePanel
                            map={map}
                            go={go}
                            setGO={setGO}
                            coordinates={coordinates}
                            ParkingList={parkingList}
                        />
                    )}
                    {mode === "favorites" && <FavoritesPanel />}
                </div>

                <div className="footer">@Eazypark</div>
            </aside>

            <main className="map-area">
                <div className="header-links">
                    <Link className="link-btn" to="/admin">
                        관리자
                    </Link>
                    <Link className="link-btn" to="/login">
                        로그인
                    </Link>
                    <Link className="link-btn" to="/mobile">
                        모바일 버전
                    </Link>
                </div>
                <div
                    id="map"
                    className="map-canvas"
                    style={{ width: "100%", height: "100%" }}
                />
            </main>
        </div>
    );
}
