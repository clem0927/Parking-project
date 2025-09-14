package min.boot.parking.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@Entity
public class ParkRealTime {
    @Id
    private String prk_center_id;
    private Integer pkfc_ParkingLots_total;
    private Integer pkfc_Available_ParkingLots_total;
}
