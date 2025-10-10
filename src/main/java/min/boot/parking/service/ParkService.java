package min.boot.parking.service;
import min.boot.parking.entity.Park;
import min.boot.parking.repository.ParkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ParkService {

    private final ParkRepository parkRepository;

    public List<Park> findAllPark() {
        return parkRepository.findAll();
    }

    public Park findParkById(Long id) {
        return parkRepository.findById(id).orElse(null);
    }

    public Park savePark(Park park) {
        return parkRepository.save(park);
    }

    public void deletePark(Long id) {
        parkRepository.deleteById(id);
    }

    public boolean existsParkById(Long id) {
        return parkRepository.existsById(id);
    }

    public Page<Park> searchParks(String searchFilter, String searchQuery, Pageable pageable) {
        // 단순 예시: PKLT_NM 기준 검색
        if ("PKLT_NM".equalsIgnoreCase(searchFilter)) {
            return parkRepository.findByPkltNmContainingIgnoreCase(searchQuery, pageable);
        } else {
            return parkRepository.findAll(pageable);
        }
    }
}
