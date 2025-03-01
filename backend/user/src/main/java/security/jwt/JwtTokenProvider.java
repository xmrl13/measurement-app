package security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private Key key;

    @Value("${jwt.secret}")
    String secret;

    @PostConstruct
    public void init() {
        byte[] secretBytes = Base64.getDecoder().decode(secret);
        this.key = Keys.hmacShaKeyFor(secretBytes);
    }

    public String generateToken(String email, String role) {
        Claims claims = Jwts.claims().setSubject(email);
        claims.put("role", role);

        Date now = new Date();
        long validityInMilliseconds = 1000 * 60 * 60 * 24; // 24 horas
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key)
                .compact();
    }

    /**
     * Extrai o campo "role" do token JWT.
     * Retorna null se não houver role ou se o token for inválido.
     */
    public String getRoleFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.get("role", String.class);
        } catch (Exception e) {
            // Em caso de token inválido ou expiração
            return null;
        }
    }
}
