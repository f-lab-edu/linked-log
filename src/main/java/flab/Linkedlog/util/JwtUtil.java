package flab.Linkedlog.util;

import flab.Linkedlog.config.JwtProperties;
import flab.Linkedlog.entity.enums.MemberGrade;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.TimeZone;

@Component
public class JwtUtil {

    private final JwtProperties jwtProperties;
    private final Key key;

    @Autowired
    public JwtUtil(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecretKey().getBytes());
    }

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
    }

    public String generateToken(String username, MemberGrade roles) {

        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        Duration expirationDuration = Duration.ofMillis(jwtProperties.getExpirationTime());
        Date issuedAt = Date.from(now.toInstant());
        ZonedDateTime expirationZonedDateTime = now.plus(expirationDuration);
        Date expiration = Date.from(expirationZonedDateTime.toInstant());

        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles)
                .setIssuedAt(issuedAt)
                .setExpiration(expiration)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String validateToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.getSubject();
        } catch (JwtException e) {
            e.printStackTrace();
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    public Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

}
