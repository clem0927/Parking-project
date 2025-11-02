package min.boot.parking.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "PARK")
@Getter
@Setter
public class Park {

    // 🔹 주차장 코드(PK)
    @Id
    @Column(name = "PKLT_CD")
    @JsonProperty("PKLT_CD")
    private Integer pkltCd;

    // 🔹 주차장 이름
    @Column(name = "PKLT_NM")
    @JsonProperty("PKLT_NM")
    private String pkltNm;

    // 🔹 주차 요금
    @Column(name = "PRK_CRG")
    @JsonProperty("PRK_CRG")
    private Integer prkCrg;

    // 🔹 총 주차면수
    @Column(name = "TPKCT")
    @JsonProperty("TPKCT")
    private Integer tpkct;

    // 🔹 평일 운영 시작시간
    @Column(name = "WD_OPER_BGNG_TM")
    @JsonProperty("WD_OPER_BGNG_TM")
    private String wdOperBgngTm;

    // 🔹 평일 운영 종료시간
    @Column(name = "WD_OPER_END_TM")
    @JsonProperty("WD_OPER_END_TM")
    private String wdOperEndTm;

    // 🔹 주말 운영 시작시간
    @Column(name = "WE_OPER_BGNG_TM")
    @JsonProperty("WE_OPER_BGNG_TM")
    private String weOperBgngTm;

    // 🔹 주말 운영 종료시간
    @Column(name = "WE_OPER_END_TM")
    @JsonProperty("WE_OPER_END_TM")
    private String weOperEndTm;
}
