package min.boot.parking.service;

import lombok.RequiredArgsConstructor;
import min.boot.parking.dto.ReservationDetailDTO;
import min.boot.parking.entity.Park;
import min.boot.parking.entity.Reservation;
import min.boot.parking.entity.ReservationDetail;
import min.boot.parking.entity.User;
import min.boot.parking.repository.ParkRepository;
import min.boot.parking.repository.ReservationDetailRepository;
import min.boot.parking.repository.ReservationRepository;
import min.boot.parking.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReservationDetailService {

    private final ReservationDetailRepository detailRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final ParkRepository parkRepository;

    @Transactional
    public ReservationDetail createDetail(ReservationDetailDTO dto) {
        Reservation reservation = reservationRepository.findById(dto.getReservationId())
                .orElseThrow(() -> new RuntimeException("예약이 존재하지 않습니다."));
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("사용자가 존재하지 않습니다."));
        Park park = parkRepository.findById(dto.getParkId())
                .orElseThrow(() -> new RuntimeException("주차장이 존재하지 않습니다."));

        ReservationDetail detail = ReservationDetail.builder()
                .reservation(reservation)
                .user(user)
                .park(park)
                .checkInTime(dto.getCheckInTime())
                .checkOutTime(dto.getCheckOutTime())
                .noShow(false)
                .build();

        reservation.setDetail(detail); // 양방향 연관관계 설정

        return detailRepository.save(detail);
    }

    @Transactional
    public ReservationDetail checkIn(Long reservationId) {
        ReservationDetail detail = detailRepository.findByReservationId(reservationId);
        detail.setCheckInTime(LocalDateTime.now());
        detail.setNoShow(false); // 입차했으므로 노쇼 false
        return detailRepository.save(detail);
    }

    @Transactional
    public ReservationDetail checkOut(Long reservationId) {
        ReservationDetail detail = detailRepository.findByReservationId(reservationId);
        detail.setCheckOutTime(LocalDateTime.now());
        return detailRepository.save(detail);
    }

    @Transactional
    public void checkNoShow(Long reservationId) {
        ReservationDetail detail = detailRepository.findByReservationId(reservationId);
        Reservation reservation = detail.getReservation();
        LocalDateTime now = LocalDateTime.now();

        // 입차시간이 null이고 예약 시작 시간이 지났으면 노쇼 처리
        if (detail.getCheckInTime() == null /* && 예약 시작 시간 비교 */) {
            detail.setNoShow(true);
            detailRepository.save(detail);
        }
    }
}
