package min.boot.parking.controller;

import lombok.RequiredArgsConstructor;
import min.boot.parking.dto.ReservationDetailDTO;
import min.boot.parking.entity.ReservationDetail;
import min.boot.parking.service.ReservationDetailService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reservation-detail")
@RequiredArgsConstructor
public class ReservationDetailController {

    private final ReservationDetailService detailService;

    @PostMapping("/create")
    public ReservationDetail createDetail(@RequestBody ReservationDetailDTO dto) {
        return detailService.createDetail(dto);
    }

    @PostMapping("/check-in/{reservationId}")
    public ReservationDetail checkIn(@PathVariable Long reservationId) {
        return detailService.checkIn(reservationId);
    }

    @PostMapping("/check-out/{reservationId}")
    public ReservationDetail checkOut(@PathVariable Long reservationId) {
        return detailService.checkOut(reservationId);
    }

    @PostMapping("/check-no-show/{reservationId}")
    public void checkNoShow(@PathVariable Long reservationId) {
        detailService.checkNoShow(reservationId);
    }
}
