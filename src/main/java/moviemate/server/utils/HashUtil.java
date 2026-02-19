package moviemate.server.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import moviemate.server.model.User;

import java.security.Key;
import java.util.Date;
import org.springframework.stereotype.Component;

@Component
public class HashUtil {

    // Ideally store this in application.properties and inject
    // private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    // private final long expiration = 1000 * 60 * 60 * 10; // 10 hours

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    private Key key;

    @PostConstruct
    public void init() {
        key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("userId", user.getId())
                .claim("roles", user.getRoles()) // optional
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key)
                .compact();
    }
}
