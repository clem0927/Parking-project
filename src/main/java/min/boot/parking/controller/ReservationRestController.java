package min.boot.parking.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import min.boot.parking.dto.ReservationDTO;
import min.boot.parking.entity.Admin;
import min.boot.parking.entity.Park;
import min.boot.parking.entity.Reservation;
import min.boot.parking.repository.ParkRepository;
import min.boot.parking.service.ReservationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reservations")
public class ReservationRestController {

    private final ReservationService reservationService;
    private final ParkRepository parkRepository;

    // ✅ 모든 예약 조회
    @GetMapping
    public ResponseEntity<List<Reservation>> getAllReservations() {
        List<Reservation> reservations = reservationService.findAllReservations();
        return ResponseEntity.ok(reservations);
    }

    // ✅ 특정 주차장(parkCode) 예약 목록 조회
    @GetMapping("/park/{parkCode}")
    public ResponseEntity<List<Reservation>> getReservationsByPark(@PathVariable Integer parkCode) {
        List<Reservation> reservations = reservationService.findByParkCode(parkCode);
        return ResponseEntity.ok(reservations);
    }

    // ✅ 예약 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        reservationService.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/create")
    public ResponseEntity<String> createReservation(@RequestBody ReservationDTO dto) {
        try {
            // Park 조회
            Park park = parkRepository.findById(dto.getParkCode())
                    .orElseThrow(() -> new IllegalArgumentException("해당 주차장을 찾을 수 없습니다."));

            // Reservation 생성
            Reservation reservation = Reservation.builder()
                    .park(park)
                    .parkName(dto.getParkName())
                    .minutes(dto.getMinutes())
                    .price(dto.getPrice())
                    .eta(dto.getEta())
                    .startTime(dto.getStartTime())
                    .endTime(dto.getEndTime())
                    .userId(dto.getUserId())
                    .build();

            // DB 저장
            reservationService.save(reservation);

            return ResponseEntity.ok("예약 완료");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("예약 등록 중 오류 발생: " + e.getMessage());
        }
    }

    @PostMapping("/findReserve")
    public ResponseEntity<List<ReservationDTO>> findReserve(HttpSession session) {
        Object loggedIn = session.getAttribute("user");
        String role = (String) session.getAttribute("role");

        if (loggedIn == null || !"ADMIN".equals(role)) {
            return ResponseEntity.status(401).build();
        }

        Admin admin = (Admin) loggedIn;
        Integer parkCode = admin.getPark() != null ? admin.getPark().getPkltCd() : null;
        if (parkCode == null) {
            return ResponseEntity.ok(List.of());
        }

        List<ReservationDTO> reservations = reservationService.findByParkCode(parkCode)
                .stream()
                .map(r -> ReservationDTO.builder()
                        .id(r.getId())  // ✅ 여기서 getId() 사용
                        .parkName(r.getParkName())
                        .minutes(r.getMinutes())
                        .price(r.getPrice())
                        .eta(r.getEta())
                        .startTime(r.getStartTime())
                        .endTime(r.getEndTime())
                        .userId(r.getUserId())
                        .parkCode(r.getPark() != null ? r.getPark().getPkltCd() : null)
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(reservations);
    }
}