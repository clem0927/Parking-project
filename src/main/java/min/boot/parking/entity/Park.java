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
public class Park {
    @Id
    private String prk_center_id;
    private Integer prk_cmprt_co;
    private String prk_plce_nm;
    private String prk_plce_adres;
    private String prk_plce_adres_sido;
    private String prk_plce_adres_sigungu;
    private Double prk_plce_entrc_la;
    private Double prk_plce_entrc_lo;
}
