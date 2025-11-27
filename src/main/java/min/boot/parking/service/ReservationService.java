package min.boot.parking.service;

import lombok.RequiredArgsConstructor;
import min.boot.parking.entity.Park;
import min.boot.parking.entity.Reservation;
import min.boot.parking.repository.ParkRepository;
import min.boot.parking.repository.ReservationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ParkRepository parkRepository;

    public Reservation save(Reservation reservation) {
        return reservationRepository.save(reservation);
    }

    public List<Reservation> findAllReservations() {
        return reservationRepository.findAll();
    }

    public List<Reservation> findByParkCode(Integer parkCode) {
        return reservationRepository.findByPark_PkltCd(parkCode);
    }

    public void deleteReservation(Long reservationId) {
        reservationRepository.deleteById(reservationId);
    }

}
