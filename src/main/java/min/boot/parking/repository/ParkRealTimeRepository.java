package min.boot.parking.repository;

import min.boot.parking.entity.ParkRealTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ParkRealTimeRepository extends JpaRepository<ParkRealTime, String> {
    // 주차장 ID 검색
    @Query("SELECT p FROM ParkRealTime p WHERE LOWER(p.prk_center_id) LIKE LOWER(CONCAT('%', :centerId, '%'))")
    Page<ParkRealTime> findByPrkCenterIdContainingIgnoreCase(@Param("centerId") String centerId, Pageable pageable);

    // 전체 Slice 조회 (Page 형태)
    @Query("SELECT p FROM ParkRealTime p")
    Page<ParkRealTime> findAllBy(Pageable pageable);

    // 총 주차 구역 수가 특정 값 이상인 데이터 조회
    @Query("SELECT p FROM ParkRealTime p WHERE p.pkfc_ParkingLots_total >= :minTotal")
    Page<ParkRealTime> findByMinParkingLots(@Param("minTotal") Integer minTotal, Pageable pageable);

    // 남은 주차 자리 수가 특정 값 이하인 데이터 조회
    @Query("SELECT p FROM ParkRealTime p WHERE p.pkfc_Available_ParkingLots_total <= :maxAvailable")
    Page<ParkRealTime> findByMaxAvailable(@Param("maxAvailable") Integer maxAvailable, Pageable pageable);
}
