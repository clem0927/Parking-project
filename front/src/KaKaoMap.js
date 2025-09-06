import { useEffect } from "react";

function KakaoMap() {
    useEffect(() => {
        window.kakao.maps.load(() => {
            const container = document.getElementById("map");
            const options = {
                center: new window.kakao.maps.LatLng(37.4138, 127.5183), // 경기 중심 (수원 예시)
                level: 7,
            };
            const map = new window.kakao.maps.Map(container, options);

            const ps = new window.kakao.maps.services.Places();

            // 1️⃣ 경기도 주차장 100개 표시
            const fetchGyeonggiParking = async () => {
                try {
                    const numOfRows = 1000; // 한 페이지 최대
                    const totalToFetch = 1000;
                    let pageNo = 1;
                    let allItems = [];

                    while (allItems.length < totalToFetch) {
                        const response = await fetch(
                            `http://apis.data.go.kr/B553881/Parking/PrkSttusInfo?serviceKey=UJwIk6dRRbBUl%2F%2Fx4CaTlCUPXgy5sJHsu%2BqaC1SDrpCBt%2B6fRG75s6BYi6sWJuIRLAGagNh23q8F6y820JW70g%3D%3D&numOfRows=${numOfRows}&pageNo=${pageNo}&format=2`
                        );
                        const result = await response.json();
                        const items = result.PrkSttusInfo || [];
                        if (items.length === 0) break;

                        // 경기도만 필터링
                        const gyeonggiItems = items.filter(
                            (item) => item.prk_plce_adres && item.prk_plce_adres.includes("경기")
                        );

                        allItems = allItems.concat(gyeonggiItems);
                        pageNo++;
                    }

                    const mapItems = allItems.slice(0, 100); // 지도 마커는 최대 100개
                    console.log("경기도 주차장 데이터:", allItems.slice(0, totalToFetch));

                    mapItems.forEach((item) => {
                        const marker = new window.kakao.maps.Marker({
                            map,
                            position: new window.kakao.maps.LatLng(
                                item.prk_plce_entrc_la,
                                item.prk_plce_entrc_lo
                            ),
                        });

                        const infowindow = new window.kakao.maps.InfoWindow({
                            content: `
                <div style="padding:5px; font-size:12px;">
                  <b>${item.prk_plce_nm}</b><br/>
                  ${item.prk_plce_adres}<br/>
                  <a href="https://map.kakao.com/link/to/${encodeURIComponent(
                                item.prk_plce_nm
                            )},${item.prk_plce_entrc_la},${item.prk_plce_entrc_lo}" target="_blank">길안내</a>
                </div>
              `,
                        });

                        window.kakao.maps.event.addListener(marker, "click", () => {
                            infowindow.open(map, marker);
                        });
                    });
                } catch (err) {
                    console.error("경기도 주차장 API 호출 실패:", err);
                }
            };

            fetchGyeonggiParking();

            // 2️⃣ 지도 클릭 시 주변 모든 카테고리 POI 표시 (마커 없이 InfoWindow)
            let clickInfoWindow = null;

            const allCategories = [
                "FD6", // 음식점
                "CE7", // 카페
                "HP8", // 병원
                "PM9", // 편의점
                "BK9", // 은행
                "AC5", // 문화시설
                "AT4", // 관광명소
            ];

            window.kakao.maps.event.addListener(map, "click", function (mouseEvent) {
                const clickLatLng = mouseEvent.latLng;

                // 이전 InfoWindow 닫기
                if (clickInfoWindow) clickInfoWindow.close();

                allCategories.forEach((cat) => {
                    ps.categorySearch(
                        cat,
                        (data, status) => {
                            if (
                                status === window.kakao.maps.services.Status.OK &&
                                data.length > 0
                            ) {
                                // 첫 번째 검색 결과만 표시
                                const place = data[0];
                                clickInfoWindow = new window.kakao.maps.InfoWindow({
                                    map,
                                    position: clickLatLng,
                                    content: `
                    <div style="padding:5px; font-size:12px;">
                      <b>${place.place_name}</b><br/>
                      ${place.road_address_name || place.address_name}<br/>
                      <a href="https://map.kakao.com/link/to/${encodeURIComponent(
                                        place.place_name
                                    )},${place.y},${place.x}" target="_blank">길안내</a>
                    </div>
                  `,
                                });
                                clickInfoWindow.open(map);
                            }
                        },
                        { location: clickLatLng, radius: 100 } // 반경 100m
                    );
                });
            });
        });
    }, []);

    return (
        <div
            id="map"
            style={{ width: "100%", height: "500px", border: "1px solid black" }}
        ></div>
    );
}

export default KakaoMap;
