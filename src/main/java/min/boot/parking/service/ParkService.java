package min.boot.parking.service;

import lombok.RequiredArgsConstructor;
import min.boot.parking.entity.Park;
import min.boot.parking.repository.ParkRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ParkService {

    private final ParkRepository parkRepository;

    /**
     * 모든 주차장 목록 조회
     */
    public List<Park> findAllParks() {
        return parkRepository.findAll();
    }
    /**
     * 주차장 코드로 조회
     */
    public Park findByCode(Integer pkltCd) {
        return parkRepository.findByPkltCd(pkltCd);
    }

    /**
     * 주차장 이름 일부로 검색
     */
    public List<Park> searchByName(String name) {
        return parkRepository.findByPkltNmContaining(name);
    }

    /**
     * 특정 요금 기준으로 주차장 목록 조회 (예: 무료 주차장 등)
     */
    public List<Park> findByCharge(int prkCrg) {
        return parkRepository.findByPrkCrgOrderByPkltNmAsc(prkCrg);
    }

    /**
     * 주차장 정보 저장 (insert or update)
     */
    public Park savePark(Park park) {
        return parkRepository.save(park);
    }

    /**
     * 주차장 삭제
     */
    public void deletePark(Integer pkltCd) {
        parkRepository.deleteById(pkltCd);
    }
}
