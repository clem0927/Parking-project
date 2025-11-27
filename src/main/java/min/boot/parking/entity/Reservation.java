package min.boot.parking.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 예약 PK 자동 증가
    private Long id;

    // Park 엔티티와 외래키 관계 설정 (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PKLT_CD") // park_code FK 컬럼 생성
    @JsonProperty("parkCode")
    @JsonIgnore
    private Park park;

    @JsonProperty("parkName")
    private String parkName;

    @JsonProperty("minutes")
    private Integer minutes;

    @JsonProperty("price")
    private Integer price;

    @JsonProperty("eta")
    private String eta;

    @JsonProperty("startTime")
    private String startTime;

    @JsonProperty("endTime")
    private String endTime;

    @JsonProperty("userId")
    private String userId;

    @Column(name = "PARKING_DATE")
    @JsonProperty("date")
    private String date;
    // 예약 등록 시점
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    // 생성 시 자동 등록
    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
