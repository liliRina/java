package car.leasing.auth;

import com.fasterxml.jackson.core.type.TypeReference;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JwtUtil {
    private static final String SECRET = "my-very-big-secret_2561234567890";
    private static final Key KEY = Keys.hmacShaKeyFor(SECRET.getBytes());
    private static final long EXPIRATION = 864_000_000; // 10 дней

    public static String generateToken(String login, List<String> roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(login)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }


    public static String extractLogin(String token) {
        return extractAllClaims(token).getSubject();
    }

    public static boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }


    public static boolean isValidToken(String token, String login) {
        return login.equals(extractLogin(token)) && !isTokenExpired(token);
    }

    private static Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public static List<String> extractRoles(String token) {
        return (List<String>)extractAllClaims(token).get("roles", List.class);
    }
}
