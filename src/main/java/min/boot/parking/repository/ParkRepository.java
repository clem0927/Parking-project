package min.boot.parking.repository;

import min.boot.parking.entity.Park;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParkRepository extends JpaRepository<Park, Integer> {

    Park findByPkltCd(Integer pkltCd);

    List<Park> findByPkltNmContaining(String name);

    List<Park> findByPrkCrgOrderByPkltNmAsc(int prkCrg);
}
