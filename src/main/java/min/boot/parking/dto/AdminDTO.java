package min.boot.parking.dto;

import lombok.*;
import min.boot.parking.entity.Admin;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDTO {
    private String id;
    private String username;
    private String password;
    private Integer parkCode;

    // Admin 엔티티를 받아 DTO로 변환하는 생성자
    public AdminDTO(Admin admin) {
        this.id = admin.getId();
        this.username = admin.getUsername();
        this.password = null; // 패스워드는 보안상 null 처리
        this.parkCode = admin.getPark() != null ? admin.getPark().getPkltCd() : null;
    }
}
