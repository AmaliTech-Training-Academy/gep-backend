package com.example.common_libraries.utils;

import com.example.common_libraries.exception.InvalidJWTTokenException;
import com.example.common_libraries.interfaces.JwtUserDetails;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
@ConditionalOnProperty(prefix = "application.security.jwt", name = "secret-key")
public class JWTUtil {
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

    private String generateToken(String email, long expiration, Map<String, Object> claims){
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiration);
        return Jwts.builder()
                .claims(claims)
                .subject(email)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getKey())
                .compact();
    }

    private String generateToken(String email, long expiration){
        return generateToken(email, expiration, new HashMap<>());
    }

    public String generateAccessToken(JwtUserDetails user){
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRoles());
        claims.put("userId", user.getId());
        claims.put("fullName", user.getFullName());
        return generateToken(user.getEmail(), jwtAccessExpirationMs, claims);
    }

    public String generateAccessToken(String email){
        return generateToken(email, jwtAccessExpirationMs);
    }


    public String generateRefreshToken(String email){
        return generateToken(email, jwtRefreshExpirationMs);
    }

    public String generateRefreshToken(JwtUserDetails user){
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("role", user.getRoles());
        claims.put("fullName", user.getFullName());
        return generateToken(user.getEmail(), jwtRefreshExpirationMs, claims);
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
        return username.equals(userDetails.getUsername()) && !extractAllClaims(token).getExpiration().before(new Date() ) && userDetails.isEnabled();
    }

    public void validateToken(String token){
        try {
            var parser = Jwts.parser().verifyWith(getKey()).build();
            parser.parseSignedClaims(token);
        } catch (ExpiredJwtException e) {
            throw new InvalidJWTTokenException("Expired JWT token");
        } catch (JwtException exception ) {
            throw new InvalidJWTTokenException("Invalid JWT token");
        }
    }

    public Claims parseToken(String token) {
        var jws = Jwts.parser().verifyWith(getKey()).build().parseSignedClaims(token);
        return jws.getPayload();
    }

    public Long extractUserId(String token){
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = parseToken(token);
        return claimsResolver.apply(claims);
    }
}
