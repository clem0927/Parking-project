package min.boot.parking.dto;

import lombok.Data;

@Data
public class ParkRealTimeDTO {
    private String prk_center_id;
    private Integer pkfc_ParkingLots_total;
    private Integer pkfc_Available_ParkingLots_total;
}
