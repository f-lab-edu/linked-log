package flab.Linkedlog.util;

import flab.Linkedlog.config.JwtProperties;
import flab.Linkedlog.entity.enums.MemberGrade;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    private final JwtProperties jwtProperties;
    private final Key key;

    @Autowired
    public JwtUtil(JwtProperties jwtProperties) {

        this.jwtProperties = jwtProperties;

        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecretKey().getBytes());
    }

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul")); // 기본 시간대 설정
    }

    public String generateToken(String username, MemberGrade roles) {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul")); // 현재 시간
        logger.info("Expiration Time from Properties: {}", jwtProperties.getExpirationTime());
        Duration expirationDuration = Duration.ofMillis(jwtProperties.getExpirationTime()); // 만료 기간

        // 만료 기간 로그로 출력
        logger.info("JWT Token generation initiated for username: {}", username);
        logger.info("Current Time (ZonedDateTime): {}", now);
        logger.info("Expiration Duration (toMillis): {} ms", expirationDuration.toMillis());
        logger.info("Expiration Duration (toMinutes): {} minutes", expirationDuration.toMinutes());
        logger.info("Expiration Duration (toHours): {} hours", expirationDuration.toHours());


        Date issuedAt = Date.from(now.toInstant()); // 발급 시간
        ZonedDateTime expirationZonedDateTime = now.plus(Duration.ofDays(1)); // 만료 시간 (ZonedDateTime)
        Date expiration = Date.from(expirationZonedDateTime.toInstant()); // 만료 시간 (Date)

        // 만료 시간 계산 과정 로그 출력
        logger.info("Expiration Time (ZonedDateTime): {}", expirationZonedDateTime);
        logger.info("Issued At (Date): {}", issuedAt);
        logger.info("Expiration Time (Date): {}", expiration);

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

            logger.info("Token Expiration Time: {}", claims.getExpiration());
            logger.info("Current Time: {}", new Date());

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
