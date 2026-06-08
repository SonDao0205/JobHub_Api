package com.btvn.jobhub.security.jwt;

import com.btvn.jobhub.security.principal.UserPrincipal;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtTokenProvider {

    @Value("${jwt-secret}")
    private String jwtSecret;

    @Value("${jwt.access-expiration-ms}")
    private long jwtAccessExpirationMs;

    @Value("${jwt.refresh-expiration-ms}")
    private long jwtRefreshExpirationMs;

    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    // Tạo AccessToken (Tuổi thọ ngắn)
    public String generateAccessToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return generateTokenFromUserPrincipal(userPrincipal, jwtAccessExpirationMs);
    }

    // Tạo RefreshToken (Tuổi thọ dài)
    public String generateRefreshToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return generateTokenFromUserPrincipal(userPrincipal, jwtRefreshExpirationMs);
    }

    public String generateTokenFromUserPrincipal(UserPrincipal userPrincipal, long expirationMs) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userPrincipal.getId());
        claims.put("role", userPrincipal.getAuthorities().iterator().next().getAuthority());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userPrincipal.getEmail())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // Lấy Email từ chuỗi JWT Token
    public String getEmailFromJwt(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    // Kiểm tra cấu trúc logic của Token
    public boolean validateToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(key).build().parseClaimsJws(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            // Có thể bổ sung Logger qua AOP hoặc ghi đè log lỗi tại đây nếu cần
            return false;
        }
    }
}