package com.event_service.event_service.utilities;

import com.event_service.event_service.exceptions.InvalidJWTTokenException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;


@Component
public class JWTUtil {

    @Value("${application.security.jwt.secret-key}")
    private String secret;


    private Key key;

    @PostConstruct
    public void init() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        key = new SecretKeySpec(keyBytes, 0, keyBytes.length, "HmacSHA256");
    }

    public void validateToken(String token){
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
        } catch (ExpiredJwtException e) {
            throw new InvalidJWTTokenException("Expired JWT token");
        } catch (UnsupportedJwtException | MalformedJwtException | SignatureException exception ) {
            throw new InvalidJWTTokenException("Invalid JWT token");
        }
    }

    public Claims parseToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }
}
