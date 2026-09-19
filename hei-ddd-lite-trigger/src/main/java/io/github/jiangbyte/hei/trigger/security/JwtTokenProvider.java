package io.github.jiangbyte.hei.trigger.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.UUID;

/**
 * JWT 令牌工具：签发与解析（含 jti、userType）。
 */
public class JwtTokenProvider {

    private final JwtProperties properties;
    private final SecretKey secretKey;

    public JwtTokenProvider(JwtProperties properties) {
        this.properties = properties;
        this.secretKey = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String createToken(String userId, String username, String userType) {
        Date now = new Date();
        Date expireAt = new Date(now.getTime() + properties.getExpireSeconds() * 1000L);
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .issuer(properties.getIssuer())
                .subject(userId)
                .claim("username", username)
                .claim("userType", userType)
                .issuedAt(now)
                .expiration(expireAt)
                .signWith(secretKey)
                .compact();
    }

    public LoginUser parseToken(String token) {
        try {
            Claims claims = parseClaims(token);
            return LoginUser.builder()
                    .userId(claims.getSubject())
                    .username(claims.get("username", String.class))
                    .userType(claims.get("userType", String.class))
                    .jti(claims.getId())
                    .expireAt(claims.getExpiration() == null ? null : claims.getExpiration().toInstant())
                    .build();
        } catch (UnauthorizedException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new UnauthorizedException("Token 无效或已过期");
        }
    }

    public Duration remainingTtl(String token) {
        Claims claims = parseClaims(token);
        Date expiration = claims.getExpiration();
        if (expiration == null) {
            return Duration.ofSeconds(properties.getExpireSeconds());
        }
        long millis = expiration.getTime() - System.currentTimeMillis();
        return millis <= 0 ? Duration.ZERO : Duration.ofMillis(millis);
    }

    public String resolveToken(String headerValue) {
        if (headerValue == null || headerValue.isBlank()) {
            return null;
        }
        String prefix = properties.getTokenPrefix();
        String token = headerValue.startsWith(prefix) ? headerValue.substring(prefix.length()).trim() : headerValue.trim();
        return token.isEmpty() ? null : token;
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .requireIssuer(properties.getIssuer())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception ex) {
            throw new UnauthorizedException("Token 无效或已过期");
        }
    }
}
