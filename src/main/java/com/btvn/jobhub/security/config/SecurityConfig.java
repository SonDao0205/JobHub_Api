package com.btvn.jobhub.security.config;

import com.btvn.jobhub.security.jwt.JwtAuthenticationFilter;
import com.btvn.jobhub.security.principal.UserPrincipalService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserPrincipalService userPrincipalService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // Thiết lập thuật toán mã băm mật khẩu BCrypt với độ mạnh (strength) = 10
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userPrincipalService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Vô hiệu hóa CSRF vì hệ thống sử dụng RESTful API không trạng thái (Stateless)
                .csrf(AbstractHttpConfigurer::disable)

                // 2. Thiết lập cơ chế quản lý phiên không lưu trữ trạng thái trên Server
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 3. Ma trận kiểm soát phân quyền đường dẫn API dựa trên Role
                .authorizeHttpRequests(auth -> auth
                        // Cấp quyền Public cho các cổng Auth hệ thống công cộng
                                // 1. Cổng mở hoàn toàn tự do (Public)
                                .requestMatchers("/api/auth/**").permitAll()

                                // 2. Phân quyền hệ thống User (Chỉ Admin)
                                .requestMatchers("/api/users/**").hasRole("ADMIN")

                                // 3. Phân quyền Hệ thống quản lý Tin tuyển dụng (Job Posting)
                                .requestMatchers("/api/jobs/search").hasAnyRole("ADMIN", "EMPLOYER")
                                .requestMatchers("/api/jobs/createJob").hasRole("EMPLOYER")
                                .requestMatchers("/api/jobs/*/submit-approval").hasRole("EMPLOYER")
                                .requestMatchers("/api/jobs/*/close").hasRole("EMPLOYER")
                                .requestMatchers("/api/jobs/*/approve").hasRole("ADMIN")
                                .requestMatchers("/api/jobs/*/reject").hasRole("ADMIN")
                        // Mọi đường dẫn khác nằm ngoài danh sách trên đều có thể tự do truy cập công khai
                        .anyRequest().permitAll()
                );

        // Đăng ký kiến trúc kiểm tra chứng thực bảo mật
        http.authenticationProvider(authenticationProvider());

        // Thêm Bộ lọc kiểm tra JWT trước bộ lọc kiểm định Username/Password mặc định
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}