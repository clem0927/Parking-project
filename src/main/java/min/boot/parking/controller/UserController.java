package min.boot.parking.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import min.boot.parking.dto.AdminDTO;
import min.boot.parking.entity.Admin;
import min.boot.parking.entity.User;
import min.boot.parking.repository.AdminRepository;
import min.boot.parking.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class UserController {
    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody User user){
        if(userRepository.existsById(user.getId())){
            return ResponseEntity.badRequest().body("이미 존재하는 아이디입니다.");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return ResponseEntity.ok("회원가입 성공!");
    }
    @PostMapping("/adminSignup")
    public ResponseEntity<?> adminSignup(@RequestBody Admin admin){
        if(adminRepository.existsById(admin.getId())){
            return ResponseEntity.badRequest().body("이미 존재하는 아이디입니다.");
        }
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        adminRepository.save(admin);
        return ResponseEntity.ok("관리자 회원가입 성공!");
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody User user, HttpSession session) {
        //먼저 일반 사용자 체크
        User u = userRepository.findById(user.getId()).orElse(null);
        if(u != null) {
            if(!passwordEncoder.matches(user.getPassword(), u.getPassword()))
                return ResponseEntity.badRequest().body("비밀번호가 일치하지 않습니다.");

            session.setAttribute("user", u); // 세션에 저장
            session.setAttribute("role", "USER"); // 역할 저장
            return ResponseEntity.ok("일반 사용자 로그인 성공!");
        }
        //Admin 체크
        Admin a = adminRepository.findById(user.getId()).orElse(null);
        if(a != null) {
            if(!passwordEncoder.matches(user.getPassword(), a.getPassword()))
                return ResponseEntity.badRequest().body("비밀번호가 일치하지 않습니다.");

            session.setAttribute("user", a);
            session.setAttribute("role", "ADMIN"); // 역할 저장
            return ResponseEntity.ok("관리자 로그인 성공!");
        }

        //둘 다 없으면 아이디 없음
        return ResponseEntity.badRequest().body("아이디가 존재하지 않습니다.");
    }

    // 현재 로그인한 사용자 정보 조회
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpSession session) {
        String role = (String) session.getAttribute("role");
        if (role == null) {
            return ResponseEntity.status(401).build();
        }

        // 세션에서 ID 추출
        Object loggedIn = session.getAttribute("user");
        if (loggedIn == null) {
            return ResponseEntity.status(401).build();
        }

        if ("USER".equals(role)) {
            User sessionUser = (User) loggedIn;
            // 항상 최신 데이터로 갱신
            User freshUser = userRepository.findById(sessionUser.getId()).orElse(null);
            if (freshUser == null) return ResponseEntity.status(404).build();
            freshUser.setPassword(null);
            return ResponseEntity.ok(freshUser);
        }
        else if ("ADMIN".equals(role)) {
            Admin sessionAdmin = (Admin) loggedIn;
            // DB에서 새로 가져옴
            Admin freshAdmin = adminRepository.findById(sessionAdmin.getId()).orElse(null);
            if (freshAdmin == null) return ResponseEntity.status(404).build();
            freshAdmin.setPassword(null);
            return ResponseEntity.ok(new AdminDTO(freshAdmin));
        }

        return ResponseEntity.status(401).build();
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpSession session){
        session.invalidate();
        return ResponseEntity.ok("로그아웃 성공!");
    }
    // 현재 로그인한 사용자 포인트 조회
    @GetMapping("/point")
    public ResponseEntity<?> getPoint(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return ResponseEntity.status(401).body("로그인 필요");

        User freshUser = userRepository.findById(user.getId()).orElse(null);
        if (freshUser == null) return ResponseEntity.status(404).body("사용자 없음");

        return ResponseEntity.ok(freshUser.getPoint());
    }

    // 포인트 충전
    @PostMapping("/point/charge")
    public ResponseEntity<?> chargePoint(@RequestParam int amount, HttpSession session) {
        if (amount <= 0) return ResponseEntity.badRequest().body("충전 금액은 0 이상이어야 합니다.");

        User user = (User) session.getAttribute("user");
        if (user == null) return ResponseEntity.status(401).body("로그인 필요");

        User freshUser = userRepository.findById(user.getId()).orElse(null);
        if (freshUser == null) return ResponseEntity.status(404).body("사용자 없음");

        freshUser.setPoint(freshUser.getPoint() + amount);
        userRepository.save(freshUser);

        session.setAttribute("user", freshUser); // 세션도 최신화
        return ResponseEntity.ok(freshUser.getPoint());
    }
}
