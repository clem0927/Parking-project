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

import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final AdminService adminService;
    // 현재 로그인한 사용자 정보 조회
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpSession session) {
        String role = (String) session.getAttribute("role");
        if (role == null) return ResponseEntity.status(401).build();

        Object loggedIn = session.getAttribute("user");
        if (loggedIn == null) return ResponseEntity.status(401).build();

        if ("USER".equals(role)) {
            User freshUser = userService.getFreshUser((User) loggedIn).orElse(null);
            if(freshUser == null) return ResponseEntity.status(404).build();
            return ResponseEntity.ok(freshUser);
        } else if ("ADMIN".equals(role)) {
            AdminDTO dto = adminService.getFreshAdmin((Admin) loggedIn).orElse(null);
            if(dto == null) return ResponseEntity.status(404).build();
            return ResponseEntity.ok(dto);
        }

        return ResponseEntity.status(401).build();
    }
    // ------------------------
    // 닉네임 업데이트
    // ------------------------
    @PutMapping("/me")
    public ResponseEntity<?> updateUser(@RequestBody Map<String, String> updates, HttpSession session) {
        User sessionUser = (User) session.getAttribute("user");
        if(sessionUser == null) return ResponseEntity.status(401).body("로그인 필요");

        // 닉네임 수정
        String newUsername = updates.get("username");
        if(newUsername != null && !newUsername.isBlank()) {
            userService.updateUsername(sessionUser.getId(), newUsername);
        }

        // 비밀번호 변경
        // 비밀번호 변경 (기존 비밀번호 확인 없이)
        // 비밀번호 변경 (기존 비밀번호 확인 없이)
        String newPassword = updates.get("password");
        if(newPassword != null && !newPassword.isBlank()) {
            userService.forceChangePassword(sessionUser.getId(), newPassword);
        }
        // 최신 정보 반환
        User freshUser = userService.getFreshUser(sessionUser).orElse(null);
        if(freshUser == null) return ResponseEntity.status(404).body("사용자 없음");
        return ResponseEntity.ok(freshUser);
    }

    // ------------------------
    // 계정 삭제
    // ------------------------
    @DeleteMapping("/me")
    public ResponseEntity<?> deleteUser(HttpSession session) {
        User sessionUser = (User) session.getAttribute("user");
        if(sessionUser == null) return ResponseEntity.status(401).body("로그인 필요");

        boolean deleted = userService.deleteUser(sessionUser.getId());
        if(deleted) {
            session.invalidate(); // 세션 종료
            return ResponseEntity.ok("계정 삭제 완료");
        } else {
            return ResponseEntity.status(404).body("사용자 없음");
        }
    }
}
