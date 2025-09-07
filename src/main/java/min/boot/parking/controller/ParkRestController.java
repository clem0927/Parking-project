package min.boot.parking.controller;

import lombok.RequiredArgsConstructor;
import min.boot.parking.entity.Park;
import min.boot.parking.service.ParkService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ParkRestController {
    private static final Logger logger= LogManager.getLogger(ParkRestController.class);

    private final ParkService parkService;

    @GetMapping("/selectAll")
    public List<Park> getAllPark(){
        return parkService.findAllPark();
    }

    @GetMapping("/select/{deptno}")
    public ResponseEntity<Park> getParkById(@PathVariable("prkCenterId") String prkCenterId){
        Park park= parkService.findParkById(prkCenterId);
        if(park!=null){
            return new ResponseEntity<>(park,HttpStatus.OK);
        }else{
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    @PostMapping("/insert")
    public ResponseEntity<String> createPark(@RequestBody Park park){
        if(parkService.existsParkById(park.getPrk_center_id())){
            return new ResponseEntity<>("이미 주차장이 존재합니다.",HttpStatus.BAD_REQUEST);
        }
        Park park2=parkService.savePark(park);
        return new ResponseEntity<>("주차장이 성공적으로 등록됨.",HttpStatus.CREATED);
    }
    @PutMapping("/update/{prkCenterId}")
    public ResponseEntity<Park> updatePark(@PathVariable("prkCenterId") String prkCenterId, @RequestBody Park parkdetails){
        Park park=parkService.findParkById(prkCenterId);
        if(park!=null){
            park.setPrk_cmprt_co(parkdetails.getPrk_cmprt_co());
            park.setPrk_plce_nm(parkdetails.getPrk_plce_nm());
            park.setPrk_plce_adres(parkdetails.getPrk_plce_adres());
            park.setPrk_plce_adres_sido(parkdetails.getPrk_plce_adres_sido());
            park.setPrk_plce_adres_sigungu(parkdetails.getPrk_plce_adres_sigungu());
            park.setPrk_plce_entrc_la(parkdetails.getPrk_plce_entrc_la());
            park.setPrk_plce_entrc_lo(parkdetails.getPrk_plce_entrc_lo());

            Park park2=parkService.savePark(park);
            return new ResponseEntity<>(park2,HttpStatus.OK);
        }else{
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    @DeleteMapping("/delete/{prkCenterId}")
    public ResponseEntity<HttpStatus> deletePark(@PathVariable("prkCenterId") String prkCenterId ){
        parkService.deletePark(prkCenterId);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    //주차장 빅데이터 저장
    @PostMapping("/insertBulk")
    public ResponseEntity<String> insertBulk(@RequestBody List<Park> parkList) {
        int count = 0;
        for (Park park : parkList) {
            if (!parkService.existsParkById(park.getPrk_center_id())) {
                parkService.savePark(park);
                count++;
            }
        }
        return new ResponseEntity<>(count + "개 주차장 등록 완료", HttpStatus.CREATED);
    }
    @GetMapping("/parks")
    public Page<Park> getParks(
            @RequestParam(defaultValue = "") String searchQuery,
            @RequestParam(defaultValue = "prk_plce_nm") String searchFilter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size
    ) {
        return parkService.searchParks(searchFilter, searchQuery, page, size);
    }
}
