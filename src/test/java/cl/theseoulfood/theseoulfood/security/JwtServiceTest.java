package cl.theseoulfood.theseoulfood.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import io.jsonwebtoken.JwtException;


class JwtServiceTest {

    private static final String SECRET =
        "dGVzdC1zZWNyZXQtdGVzdC1zZWNyZXQtdGVzdC1zZWNyZXQtMTIzNDU2";

    private final JwtService jwt = new JwtService(SECRET, 900_000, 604_800_000);

    private UserDetails usuario() {
        return User.withUsername("admin@seoulfood.cl")
            .password("x").authorities("ROLE_ADMIN").build();
    }

    @Test
    void accessTokenEsValidoParaElUsuario() {
        String token = jwt.generarAccessToken(usuario());
        assertEquals("admin@seoulfood.cl", jwt.extraerEmail(token));
        assertTrue(jwt.esValido(token, usuario(), JwtService.ACCESS));
    }

    @Test
    void accessTokenNoSirveComoRefresh() {
        String token = jwt.generarAccessToken(usuario());
        assertFalse(jwt.esValido(token, usuario(), JwtService.REFRESH));
    }

    @Test
    void refreshTokenNoSirveComoAccess() {
        String token = jwt.generarRefreshToken(usuario());
        assertFalse(jwt.esValido(token, usuario(), JwtService.ACCESS));
    }

    @Test
    void tokenManipuladoLanzaExcepcion() {
        String token = jwt.generarAccessToken(usuario());
        String alterado = token.substring(0, token.length() - 2) + "xx";
        assertThrows(JwtException.class, () -> jwt.extraerEmail(alterado));
    }

    @Test
    void tokenExpiradoLanzaExcepcion() {
        JwtService corto = new JwtService(SECRET, -1000, -1000);
        String token = corto.generarAccessToken(usuario());
        assertThrows(JwtException.class, () -> corto.extraerEmail(token));
    }

    @Test
    void tokenDeOtroUsuarioNoEsValido() {
        String token = jwt.generarAccessToken(usuario());
        UserDetails otro = User.withUsername("otro@seoulfood.cl")
            .password("x").authorities("ROLE_CLIENTE").build();
        assertFalse(jwt.esValido(token, otro, JwtService.ACCESS));
    }
}