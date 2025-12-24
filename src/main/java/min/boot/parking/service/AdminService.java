package min.boot.parking.service;

import lombok.RequiredArgsConstructor;
import min.boot.parking.dto.AdminDTO;
import min.boot.parking.entity.Admin;
import min.boot.parking.repository.AdminRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    // 관리자 회원가입
    public String signup(Admin admin) {
        if(adminRepository.existsById(admin.getId())){
            return "이미 존재하는 아이디입니다.";
        }
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        adminRepository.save(admin);
        return "관리자 회원가입 성공!";
    }

    // 관리자 로그인
    public Optional<Admin> login(Admin admin) {
        Admin a = adminRepository.findById(admin.getId()).orElse(null);
        if(a == null) return Optional.empty();
        if(!passwordEncoder.matches(admin.getPassword(), a.getPassword())) return Optional.ofNullable(null);
        return Optional.of(a);
    }

    // 관리자 정보 조회
    public Optional<AdminDTO> getFreshAdmin(Admin sessionAdmin) {
        Admin freshAdmin = adminRepository.findById(sessionAdmin.getId()).orElse(null);
        if(freshAdmin == null) return Optional.empty();
        freshAdmin.setPassword(null);
        return Optional.of(new AdminDTO(freshAdmin));
    }
}
