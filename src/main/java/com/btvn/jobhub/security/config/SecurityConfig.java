package com.btvn.jobhub.security.config;

import com.btvn.jobhub.security.jwt.JwtAuthenticationFilter;
import com.btvn.jobhub.security.principal.UserPrincipalService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
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
@EnableAspectJAutoProxy
public class SecurityConfig {

    private final UserPrincipalService userPrincipalService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

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
                .csrf(AbstractHttpConfigurer::disable)

                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                                .requestMatchers("/api/auth/register", "/api/auth/login", "/api/auth/refresh", "/api/auth/forgot-password", "/api/auth/reset-password").permitAll()
                                .requestMatchers("/api/auth/change-password").authenticated()

                                .requestMatchers("/api/users/**").hasRole("ADMIN")

                                .requestMatchers("/api/jobs/search").hasAnyRole("ADMIN", "EMPLOYER")
                                .requestMatchers("/api/jobs/createJob").hasRole("EMPLOYER")
                                .requestMatchers("/api/jobs/*/submit-approval").hasRole("EMPLOYER")
                                .requestMatchers("/api/jobs/*/close").hasRole("EMPLOYER")
                                .requestMatchers("/api/jobs/*/approve").hasRole("ADMIN")
                                .requestMatchers("/api/jobs/*/reject").hasRole("ADMIN")
                                .requestMatchers("/api/jobs/jobApproved").hasAnyRole("CANDIDATE","EMPLOYER","ADMIN")
                                .requestMatchers("/api/applications/apply").hasRole("CANDIDATE")
                                .requestMatchers("/api/applications/history").hasRole("CANDIDATE")
                                .requestMatchers("/api/applications/*/review").hasRole("EMPLOYER")
                                .requestMatchers("/api/applications/*/interview").hasRole("EMPLOYER")
                                .requestMatchers("/api/applications/*/accept").hasRole("EMPLOYER")
                                .requestMatchers("/api/applications/*/reject").hasRole("EMPLOYER")
                        .anyRequest().permitAll()
                );

        http.authenticationProvider(authenticationProvider());

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}