package min.boot.parking.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationDetailDTO {
    private Long reservationId;
    private String userId;
    private Integer parkId;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private Boolean noShow;
}
