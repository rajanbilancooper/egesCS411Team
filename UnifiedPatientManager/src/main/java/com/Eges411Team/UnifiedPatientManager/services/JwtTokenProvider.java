package com.Eges411Team.UnifiedPatientManager.services;

import com.Eges411Team.UnifiedPatientManager.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    // Single hardcoded Base64-encoded 64-byte (512-bit) secret.
    // Fine for a class project. Do not reuse in real deployments.
    private static final String SECRET_B64 =
        "VSbiywFGznU3Lt0LlmUHnyULekkic6l6ULmIupEuwZ1IkXHf/9abLE0xXwCFLm+K0ayxx1/ht5upEbK1xzghkA==";

    // Token lifetime: 24 hours (in seconds)
    private static final long TTL_SECONDS = 86_400L;

    // Build and reuse the HMAC key
    private final Key key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_B64));

    
    public String generateToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + TTL_SECONDS * 1000);

        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("userId", user.getId())
                .claim("role", user.getRole().name())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512) // JJWT 0.11.x
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)   // verify with the same key
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    public Integer getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("userId", Integer.class);
    }

    public Long getExpirationTime() {
        return TTL_SECONDS; // seconds
    }
}
