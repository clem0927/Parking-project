package min.boot.parking.repository;

import min.boot.parking.entity.Park;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ParkRepository extends JpaRepository<Park, Long> { // PK 타입 Long

    // PKLT_NM 기준 검색
    @Query("SELECT p FROM Park p WHERE LOWER(p.PKLT_NM) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Park> findByPkltNmContainingIgnoreCase(@Param("name") String name, Pageable pageable);

    // PKLT_CD 기준 검색
    @Query("SELECT p FROM Park p WHERE LOWER(p.PKLT_CD) LIKE LOWER(CONCAT('%', :code, '%'))")
    Page<Park> findByPkltCdContainingIgnoreCase(@Param("code") String code, Pageable pageable);

    // 전체 조회 (Slice/Page 사용 가능)
    @Query("SELECT p FROM Park p")
    Page<Park> findAllBy(Pageable pageable);
}
