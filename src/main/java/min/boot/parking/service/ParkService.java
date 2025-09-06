package min.boot.parking.service;

import min.boot.parking.entity.Park;
import min.boot.parking.repository.ParkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ParkService {
    @Autowired
    public ParkRepository parkRepository;

    /**
     * 페이징 + 검색 (접두사 검색 사용)
     */
    public Page<Park> searchParks(String searchFilter, String searchQuery, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);

        if (searchQuery == null || searchQuery.isEmpty()) {
            return parkRepository.findAllBy(pageable);
        }

        switch (searchFilter) {
            case "prk_plce_nm":
                return parkRepository.findByPrkPlceNmContainingIgnoreCase(searchQuery, pageable);
            case "prk_plce_adres":
                return parkRepository.findByPrkPlceAdresContainingIgnoreCase(searchQuery, pageable);
            case "prk_center_id":
                return parkRepository.findByPrkCenterIdContainingIgnoreCase(searchQuery, pageable);
            default:
                return parkRepository.findAllBy(pageable);
        }
    }

    @Transactional
    public Park savePark(Park park){
        return parkRepository.save(park);
    }

    @Transactional
    public List<Park> findAllPark(){
        return parkRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Park findParkById(String id){
        return parkRepository.findById(id).orElse(null);
    }

    @Transactional
    public void deletePark(String id){
        parkRepository.deleteById(id);
    }

    public boolean existsParkById(String id){
        return parkRepository.existsById(id);
    }
}
