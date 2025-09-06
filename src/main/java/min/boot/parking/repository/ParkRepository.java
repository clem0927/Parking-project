package min.boot.parking.repository;

import min.boot.parking.entity.Park;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ParkRepository extends JpaRepository<Park, String> {

    // JPQL에서 snake_case 필드를 그대로 사용
    @Query("SELECT p FROM Park p WHERE LOWER(p.prk_plce_nm) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Park> findByPrkPlceNmContainingIgnoreCase(@Param("name") String name, Pageable pageable);

    @Query("SELECT p FROM Park p WHERE LOWER(p.prk_plce_adres) LIKE LOWER(CONCAT('%', :adres, '%'))")
    Page<Park> findByPrkPlceAdresContainingIgnoreCase(@Param("adres") String adres, Pageable pageable);

    @Query("SELECT p FROM Park p WHERE LOWER(p.prk_center_id) LIKE LOWER(CONCAT('%', :centerId, '%'))")
    Page<Park> findByPrkCenterIdContainingIgnoreCase(@Param("centerId") String centerId, Pageable pageable);

    // 그냥 전체 Slice 조회
    @Query("SELECT p FROM Park p")
    Page<Park> findAllBy(Pageable pageable);
}
