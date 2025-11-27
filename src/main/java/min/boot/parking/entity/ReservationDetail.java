package min.boot.parking.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    // 1:1 매핑: 하나의 예약에 하나의 상세
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    // 유저 FK
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // 주차장 FK
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "park_id")
    private Park park;

    private LocalDateTime checkInTime;  // 입차시간
    private LocalDateTime checkOutTime; // 출차시간

    private Boolean noShow = false;     // 노쇼 여부

    @PrePersist
    public void prePersist() {
        if (noShow == null) noShow = false;
    }
}
