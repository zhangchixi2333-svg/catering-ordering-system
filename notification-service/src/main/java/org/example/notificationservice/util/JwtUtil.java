package org.example.notificationservice.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private static String secret;

    @Value("${jwt.secret}")
    public void setSecret(String secret) {
        JwtUtil.secret = secret;
    }

    private static SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public static Claims getClaimsFromToken(String token) {
        try {
            if (token == null || token.isBlank() || secret == null || secret.isBlank()) {
                return null;
            }
            return Jwts.parserBuilder()
                    .setSigningKey(getSecretKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            return null;
        }
    }

    public static Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims == null) {
            return null;
        }
        Object userId = claims.get("userId");
        if (userId instanceof Number number) {
            return number.longValue();
        }
        if (userId instanceof String text) {
            return Long.parseLong(text);
        }
        return null;
    }

    public static boolean validateTokenForUser(String token, Long userId) {
        Claims claims = getClaimsFromToken(token);
        if (claims == null || claims.getExpiration() == null || claims.getExpiration().before(new Date())) {
            return false;
        }
        Long tokenUserId = getUserIdFromToken(token);
        return userId != null && userId.equals(tokenUserId);
    }
}
