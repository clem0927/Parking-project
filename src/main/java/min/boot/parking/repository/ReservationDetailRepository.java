package min.boot.parking.repository;

import min.boot.parking.entity.ReservationDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationDetailRepository extends JpaRepository<ReservationDetail, Long> {
    ReservationDetail findByReservationId(Long reservationId);
}
