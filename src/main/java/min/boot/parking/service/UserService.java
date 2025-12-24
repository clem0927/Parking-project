package min.boot.parking.service;

import lombok.RequiredArgsConstructor;
import min.boot.parking.entity.User;
import min.boot.parking.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원가입
    public String signup(User user) {
        if(userRepository.existsById(user.getId())){
            return "이미 존재하는 아이디입니다.";
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return "회원가입 성공!";
    }

    // 로그인
    public Optional<User> login(User user) {
        User u = userRepository.findById(user.getId()).orElse(null);
        if(u == null) return Optional.empty();
        if(!passwordEncoder.matches(user.getPassword(), u.getPassword())) return Optional.ofNullable(null);
        return Optional.of(u);
    }

    // 사용자 정보 조회
    public Optional<User> getFreshUser(User sessionUser) {
        User freshUser = userRepository.findById(sessionUser.getId()).orElse(null);
        if(freshUser != null) freshUser.setPassword(null);
        return Optional.ofNullable(freshUser);
    }

    // 포인트 조회
    public Optional<Integer> getPoint(User sessionUser) {
        User freshUser = userRepository.findById(sessionUser.getId()).orElse(null);
        if(freshUser == null) return Optional.empty();
        return Optional.of(freshUser.getPoint());
    }

    // 포인트 충전
    @Transactional
    public Optional<Integer> chargePoint(User sessionUser, int amount) {
        User freshUser = userRepository.findById(sessionUser.getId()).orElse(null);
        if(freshUser == null) return Optional.empty();
        freshUser.setPoint(freshUser.getPoint() + amount);
        userRepository.save(freshUser);
        return Optional.of(freshUser.getPoint());
    }
    // 닉네임 수정
    public Optional<User> updateUsername(String id, String newUsername) {
        return userRepository.findById(id).map(user -> {
            user.setUsername(newUsername);
            return userRepository.save(user);
        });
    }
    // 비밀번호 변경

    @Transactional
    public void forceChangePassword(String id, String newPassword) {
        userRepository.findById(id).ifPresent(user -> {
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
        });
    }

    public boolean deleteUser(String id) {
        return userRepository.findById(id).map(user -> {
            userRepository.delete(user);
            return true;
        }).orElse(false);
    }
}
