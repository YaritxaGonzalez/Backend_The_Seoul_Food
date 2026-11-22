package cl.theseoulfood.theseoulfood.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    public static final String ACCESS = "access";
    public static final String REFRESH = "refresh";

    private final SecretKey key;
    private final long accessExpMs;
    private final long refreshExpMs;

    public JwtService(@Value("${jwt.secret}") String secret,
            @Value("${jwt.access-expiration-ms}") long accessExpMs,
            @Value("${jwt.refresh-expiration-ms}") long refreshExpMs) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.accessExpMs = accessExpMs;
        this.refreshExpMs = refreshExpMs;
    }

    public String generarAccessToken(UserDetails user) {
        String rol = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).findFirst().orElse("");
        return construir(user.getUsername(), ACCESS, accessExpMs, rol);
    }

    public String generarRefreshToken(UserDetails user) {
        return construir(user.getUsername(), REFRESH, refreshExpMs, null);
    }

    private String construir(String subject, String tipo, long expMs, String rol) {
        Date ahora = new Date();
        var b = Jwts.builder()
                .subject(subject)
                .claim("type", tipo)
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + expMs));
        if (rol != null)
            b.claim("role", rol);
        return b.signWith(key).compact();
    }

    private Claims claims(String token) {
        return Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload();
    }

    public String extraerEmail(String token) {
        return claims(token).getSubject();
    }

    /**
     * Valida firma, expiración, usuario y que sea del tipo esperado (access o
     * refresh).
     */
    public boolean esValido(String token, UserDetails user, String tipoEsperado) {
        Claims c = claims(token); // lanza JwtException si es inválido o expiró
        return c.getSubject().equals(user.getUsername())
                && tipoEsperado.equals(c.get("type", String.class))
                && c.getExpiration().after(new Date());
    }
}