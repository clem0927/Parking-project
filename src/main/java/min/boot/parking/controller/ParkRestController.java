package min.boot.parking.controller;

import lombok.RequiredArgsConstructor;
import min.boot.parking.entity.Park;
import min.boot.parking.service.ParkService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/parkings")
@CrossOrigin(origins = "http://localhost:3000")
public class ParkRestController {
    private static final Logger logger = LogManager.getLogger(ParkRestController.class);
    @Autowired
    private final ParkService parkService;

    // 전체 조회
    @GetMapping("/all")
    public List<Park> getAllPark() {
        return parkService.findAllPark();
    }

    // ID로 조회
    @GetMapping("/{id}")
    public ResponseEntity<Park> getParkById(@PathVariable("id") Long id) {
        Park park = parkService.findParkById(id);
        if (park != null) {
            return new ResponseEntity<>(park, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // 단일 등록
    @PostMapping("/insert")
    public ResponseEntity<String> createPark(@RequestBody Park park) {
        if (parkService.existsParkById(park.getId())) {
            return new ResponseEntity<>("이미 주차장이 존재합니다.", HttpStatus.BAD_REQUEST);
        }
        parkService.savePark(park);
        return new ResponseEntity<>("주차장이 성공적으로 등록됨.", HttpStatus.CREATED);
    }

    // 여러 개 등록 (visibleOnly 같은 리스트)
    @PostMapping("/visible")
    public ResponseEntity<String> insertBulk(@RequestBody List<Park> parkList) {
        int count = 0;
        for (Park park : parkList) {
            if (!parkService.existsParkById(park.getId())) {
                parkService.savePark(park);
                count++;
            }
        }
        return new ResponseEntity<>(count + "개 주차장 등록 완료", HttpStatus.CREATED);
    }

    // Page 단위 조회 (검색 가능)
    @GetMapping
    public Page<Park> getParks(
            @RequestParam(defaultValue = "") String searchQuery,
            @RequestParam(defaultValue = "PKLT_NM") String searchFilter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size
    ) {
        return parkService.searchParks(searchFilter, searchQuery, PageRequest.of(page, size));
    }

    // 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deletePark(@PathVariable("id") Long id) {
        parkService.deletePark(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
