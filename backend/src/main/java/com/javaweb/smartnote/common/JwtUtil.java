package com.javaweb.smartnote.common;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

// JWT工具类：生成、解析、验证Token
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expire-ms}")
    private Long expireMs;

    // 生成JWT Token，payload中存储userId
    public String generateToken(Long userId) {
        Date now = new Date();
        Date expire = new Date(now.getTime() + expireMs);
        return JWT.create()
                .withClaim("userId", userId)
                .withIssuedAt(now)
                .withExpiresAt(expire)
                .sign(Algorithm.HMAC256(secret));
    }

    // 从Token中解析userId
    public Long getUserIdFromToken(String token) {
        try {
            DecodedJWT jwt = JWT.require(Algorithm.HMAC256(secret))
                    .build()
                    .verify(token);
            return jwt.getClaim("userId").asLong();
        } catch (Exception e) {
            return null;
        }
    }

    // 验证Token有效性
    public boolean validateToken(String token) {
        return getUserIdFromToken(token) != null;
    }
}
