package min.boot.parking.service;

import min.boot.parking.entity.ParkRealTime;
import min.boot.parking.repository.ParkRealTimeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ParkRealTimeService {
    @Autowired
    private ParkRealTimeRepository parkRealTimeRepository;

    /**
     * 페이징 + 검색
     */
    public Page<ParkRealTime> searchParkRealTimes(String searchFilter, String searchQuery, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);

        if (searchQuery == null || searchQuery.isEmpty()) {
            return parkRealTimeRepository.findAllBy(pageable);
        }

        switch (searchFilter) {
            case "prk_center_id":
                return parkRealTimeRepository.findByPrkCenterIdContainingIgnoreCase(searchQuery, pageable);
            case "min_total":
                try {
                    Integer minTotal = Integer.valueOf(searchQuery);
                    return parkRealTimeRepository.findByMinParkingLots(minTotal, pageable);
                } catch (NumberFormatException e) {
                    return parkRealTimeRepository.findAllBy(pageable);
                }
            case "max_available":
                try {
                    Integer maxAvailable = Integer.valueOf(searchQuery);
                    return parkRealTimeRepository.findByMaxAvailable(maxAvailable, pageable);
                } catch (NumberFormatException e) {
                    return parkRealTimeRepository.findAllBy(pageable);
                }
            default:
                return parkRealTimeRepository.findAllBy(pageable);
        }
    }

    @Transactional
    public ParkRealTime saveParkRealTime(ParkRealTime parkRealTime) {
        return parkRealTimeRepository.save(parkRealTime);
    }

    @Transactional(readOnly = true)
    public List<ParkRealTime> findAllParkRealTimes() {
        return parkRealTimeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public ParkRealTime findParkRealTimeById(String id) {
        return parkRealTimeRepository.findById(id).orElse(null);
    }

    @Transactional
    public void deleteParkRealTime(String id) {
        parkRealTimeRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public boolean existsParkRealTimeById(String id) {
        return parkRealTimeRepository.existsById(id);
    }
}
