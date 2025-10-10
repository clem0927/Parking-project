package min.boot.parking.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "PARK")
@Getter
@Setter
public class Park{
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "parking_seq")
    @SequenceGenerator(name = "parking_seq", sequenceName = "VISIBLE_PARKING_SEQ", allocationSize = 1)
    private Long id;

    private String PKLT_CD;
    private String PKLT_NM;
    private int PRK_CRG;
    private int TPKCT;
    private int liveCnt;
    private int remainCnt;
    private String WD_OPER_BGNG_TM;
    private String WD_OPER_END_TM;
    private String WE_OPER_BGNG_TM;
    private String WE_OPER_END_TM;

    // Getter/Setter 생략
}