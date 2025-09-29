package min.boot.parking.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

@RestController
public class MobileParkingController {

    private final String API_KEY = "56776f4f766b696d3335704f6b434d"; // 본인 API 키
    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/mobile-api/parking")
    public ResponseEntity<Map<String, Object>> getParkingData() {

        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 실시간 정보
            String realtimeUrl = "http://openapi.seoul.go.kr:8088/" + API_KEY + "/json/GetParkingInfo/1/1000/";
            Map realtime = restTemplate.getForObject(realtimeUrl, Map.class);
            result.put("realtime", realtime);

            // 2. 전체 주차장 정보 (여기서는 최대 7000건까지 반복)
            Map fullParkInfo = new HashMap();
            for (int i = 0; i < 7; i++) {
                int start = i * 1000 + 1;
                int end = (i + 1) * 1000;
                String parkUrl = "http://openapi.seoul.go.kr:8088/" + API_KEY + "/json/GetParkInfo/" + start + "/" + end + "/";
                Map response = restTemplate.getForObject(parkUrl, Map.class);

                // 합치기
                Map getParkInfo = (Map) response.get("GetParkInfo");
                if (getParkInfo != null) {
                    java.util.List rows = (java.util.List) getParkInfo.get("row");
                    if (rows != null) {
                        fullParkInfo.compute("row", (k, v) -> {
                            if (v == null) return rows;
                            ((java.util.List) v).addAll(rows);
                            return v;
                        });
                    }
                }
            }
            result.put("parkInfo", Map.of("GetParkInfo", fullParkInfo));

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
