package min.boot.parking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import min.boot.parking.dto.ReservationDTO;
import min.boot.parking.entity.Admin;
import min.boot.parking.entity.Park;
import min.boot.parking.entity.Reservation;
import min.boot.parking.repository.AdminRepository;
import min.boot.parking.repository.ParkRepository;
import min.boot.parking.service.ParkService;
import min.boot.parking.service.ReservationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class ParkRestController {

    private final ParkService parkService;
    private final ParkRepository parkRepository;
    private final AdminRepository adminRepository;
    private final ReservationService reservationService;
    /**
     * 프론트에서 넘어온 주차장 리스트를 DB에 저장
     */

    @PostMapping("/saveDB")
    public ResponseEntity<String> saveParkingList(@RequestBody List<Park> parkingList) {
        try {
            for (Park park : parkingList) {
                parkService.savePark(park);  // 개별 저장
            }
            return ResponseEntity.ok("주차장 정보가 성공적으로 저장되었습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("주차장 저장 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    @PostMapping("/registerPark")
    public ResponseEntity<String> registerPark(@RequestBody Map<String, String> request) {
        try {
            String adminId = request.get("adminId");
            String pkltCdStr = request.get("pkltCd");

            if (adminId == null || pkltCdStr == null) {
                return ResponseEntity.badRequest().body("필요한 데이터가 없습니다.");
            }

            Admin admin = adminRepository.findById(adminId).orElse(null);
            if (admin == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("등록할 관리자 정보를 찾을 수 없습니다.");
            }

            // 🔹 pkltCd를 Integer로 변환
            Integer pkltCd = Integer.valueOf(pkltCdStr);

            // 🔹 Park 엔티티 찾아오기
            Park park = parkRepository.findById(pkltCd)
                    .orElseThrow(() -> new IllegalArgumentException("해당 주차장을 찾을 수 없습니다."));

            // 🔹 외래키 설정
            admin.setPark(park);

            adminRepository.save(admin);

            return ResponseEntity.ok("관리자에 주차장 연결 완료");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("서버 오류 발생: " + e.getMessage());
        }
    }
    @PostMapping("/unregisterPark")
    public ResponseEntity<String> unregisterPark(@RequestBody Map<String, String> request) {
        try {
            String adminId = request.get("adminId");

            if (adminId == null) {
                return ResponseEntity.badRequest().body("adminId가 필요합니다.");
            }

            Admin admin = adminRepository.findById(adminId).orElse(null);
            if (admin == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("해당 관리자를 찾을 수 없습니다.");
            }

            // 🔹 park 연결 해제
            admin.setPark(null);

            adminRepository.save(admin);

            return ResponseEntity.ok("관리자의 주차장 연결이 해제되었습니다.");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("서버 오류 발생: " + e.getMessage());
        }
    }
}
