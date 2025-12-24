package min.boot.parking.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import min.boot.parking.dto.AdminDTO;
import min.boot.parking.entity.Admin;
import min.boot.parking.entity.User;
import min.boot.parking.service.AdminService;
import min.boot.parking.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AdminService adminService;

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody User user){
        String msg = userService.signup(user);
        if(msg.contains("이미 존재")) return ResponseEntity.badRequest().body(msg);
        return ResponseEntity.ok(msg);
    }

    @PostMapping("/adminSignup")
    public ResponseEntity<?> adminSignup(@RequestBody Admin admin){
        String msg = adminService.signup(admin);
        if(msg.contains("이미 존재")) return ResponseEntity.badRequest().body(msg);
        return ResponseEntity.ok(msg);
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user, HttpSession session) {
        // 일반 사용자 로그인
        var uOpt = userService.login(user);
        if(uOpt.isPresent()) {
            User u = uOpt.get();
            if(u == null) return ResponseEntity.badRequest().body("비밀번호가 일치하지 않습니다.");
            session.setAttribute("user", u);
            session.setAttribute("role", "USER");
            return ResponseEntity.ok("일반 사용자 로그인 성공!");
        }

        // 관리자 로그인
        Admin admin = new Admin();
        admin.setId(user.getId());
        admin.setPassword(user.getPassword());
        var aOpt = adminService.login(admin);
        if(aOpt.isPresent()) {
            Admin a = aOpt.get();
            if(a == null) return ResponseEntity.badRequest().body("비밀번호가 일치하지 않습니다.");
            session.setAttribute("user", a);
            session.setAttribute("role", "ADMIN");
            return ResponseEntity.ok("관리자 로그인 성공!");
        }

        return ResponseEntity.badRequest().body("아이디가 존재하지 않습니다.");
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpSession session){
        session.invalidate();
        return ResponseEntity.ok("로그아웃 성공!");
    }

    // 포인트 조회
    @GetMapping("/point")
    public ResponseEntity<?> getPoint(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if(user == null) return ResponseEntity.status(401).body("로그인 필요");

        return userService.getPoint(user)
                .<ResponseEntity<?>>map(p -> ResponseEntity.ok(p))
                .orElseGet(() -> ResponseEntity.status(404).body("사용자 없음"));
    }

    // 포인트 충전
    @PostMapping("/point/charge")
    public ResponseEntity<?> chargePoint(@RequestParam int amount, HttpSession session) {
        if(amount <= 0) return ResponseEntity.badRequest().body("충전 금액은 0 이상이어야 합니다.");

        User user = (User) session.getAttribute("user");
        if(user == null) return ResponseEntity.status(401).body("로그인 필요");

        return userService.chargePoint(user, amount)
                .<ResponseEntity<?>>map(p -> {
                    session.setAttribute("user", user); // 세션 최신화
                    return ResponseEntity.ok(p);
                })
                .orElseGet(() -> ResponseEntity.status(404).body("사용자 없음"));
    }
}
