package com.example.auth_service.security;

import com.example.auth_service.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${JWT_SECRET}")
    private String jwtSecret;

    @Value("${application.security.jwt.expiration}")
    private long jwtAccessExpirationMs;

    @Value("${application.security.jwt.refresh-token.expiration}")
    private long jwtRefreshExpirationMs;

    private SecretKey getKey(){
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private String generateToken(String email, long expiration){
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiration);
        return Jwts.builder()
                .subject(email)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getKey())
                .compact();
    }

    public String generateAccessToken(String email){
        return generateToken(email, jwtAccessExpirationMs);
    }

    public String generateRefreshToken(String email){
        return generateToken(email, jwtRefreshExpirationMs);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUsername(String token){
        return extractAllClaims(token).getSubject();
    }

    public boolean validateToken(String token, UserDetails userDetails){
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !extractAllClaims(token).getExpiration().before(new Date());
    }

}
