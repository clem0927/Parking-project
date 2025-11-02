package min.boot.parking.repository;

import min.boot.parking.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    // 특정 유저의 예약 조회
    List<Reservation> findByUserId(String userId);

    // 특정 주차장의 예약 조회
    List<Reservation> findByPark_PkltCd(Integer pkltCd);
}
