package min.boot.parking.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationDTO {
    private Long id;
    private String parkName;
    private Integer minutes;
    private Integer price;
    private String eta;
    private String startTime;
    private String endTime;
    private String userId;
    private Integer parkCode; // 외래키 주차장 코드
}
