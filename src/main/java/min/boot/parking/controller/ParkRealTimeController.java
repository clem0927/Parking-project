package min.boot.parking.controller;

import lombok.RequiredArgsConstructor;
import min.boot.parking.entity.ParkRealTime;
import min.boot.parking.service.ParkRealTimeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/live") // ✅ 공통 prefix 추가
public class ParkRealTimeController {

    private final ParkRealTimeService parkRealTimeService;

    /**
     * 여러 건 저장 (배치)
     */
    @PostMapping("/bulk")
    public ResponseEntity<String> insertLiveBulk(@RequestBody List<ParkRealTime> list) {
        int count = 0;
        for (ParkRealTime p : list) {
            if (!parkRealTimeService.existsParkRealTimeById(p.getPrk_center_id())) {
                parkRealTimeService.saveParkRealTime(p);
                count++;
            } else {
                // 이미 존재하면 업데이트 처리
                parkRealTimeService.saveParkRealTime(p);
            }
        }
        return new ResponseEntity<>(count + "개 실시간 데이터 저장/갱신 완료", HttpStatus.CREATED);
    }

    /**
     * 전체 조회
     */
    @GetMapping("/all")
    public List<ParkRealTime> getAllLive() {
        return parkRealTimeService.findAllParkRealTimes();
    }

    /**
     * 단건 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<ParkRealTime> getLiveById(@PathVariable("id") String id) {
        ParkRealTime p = parkRealTimeService.findParkRealTimeById(id);
        if (p != null) {
            return new ResponseEntity<>(p, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteLive(@PathVariable("id") String id) {
        parkRealTimeService.deleteParkRealTime(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
