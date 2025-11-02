package min.boot.parking.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="admins")
@Getter
@Setter
@NoArgsConstructor
public class Admin {
    @Id
    private String id;
    private String username;
    private String password;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PKLT_CD", referencedColumnName = "PKLT_CD")
    @JsonIgnore
    private Park park;
}
