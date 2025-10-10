package min.boot.parking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    // PasswordEncoder Bean
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 보안 설정
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())  // React 연동 시 CSRF 끄기
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**", "/parkings/visible").permitAll()  // 회원가입, 로그인 허용
                        .anyRequest().authenticated()             // 나머지는 인증 필요
                )
                .formLogin(form -> form.disable())            // 폼 로그인 비활성화
                .httpBasic(basic -> basic.disable())         // 기본 인증 비활성화
                .logout(logout -> logout
                        .logoutUrl("/logout")               // 로그아웃 URL
                        .logoutSuccessUrl("/")              // 로그아웃 후 이동
                        .invalidateHttpSession(true)        // 세션 무효화
                        .deleteCookies("JSESSIONID")        // 쿠키 삭제
                );

        return http.build();
    }
}
