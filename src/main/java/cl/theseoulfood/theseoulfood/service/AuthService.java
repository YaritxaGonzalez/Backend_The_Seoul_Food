package cl.theseoulfood.theseoulfood.service;

import cl.theseoulfood.theseoulfood.dto.AuthDtos.*;
import cl.theseoulfood.theseoulfood.model.Rol;
import cl.theseoulfood.theseoulfood.model.Usuario;
import cl.theseoulfood.theseoulfood.repository.UsuarioRepository;
import cl.theseoulfood.theseoulfood.security.CustomUserDetailsService;
import cl.theseoulfood.theseoulfood.security.JwtService;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UsuarioRepository repo;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final CustomUserDetailsService uds;
    private final JwtService jwt;

    public AuthService(UsuarioRepository repo, PasswordEncoder encoder,
            AuthenticationManager authManager,
            CustomUserDetailsService uds, JwtService jwt) {
        this.repo = repo;
        this.encoder = encoder;
        this.authManager = authManager;
        this.uds = uds;
        this.jwt = jwt;
    }

    /** Registro público: SIEMPRE crea un CLIENTE. */
    public AuthResponse registrar(RegisterRequest r) {
        crear(r.nombre(), r.email(), r.password(), Rol.CLIENTE);
        return login(new LoginRequest(r.email(), r.password()));
    }

    /** Creación por el ADMIN con rol elegido. */
    public Usuario crear(String nombre, String email, String password, Rol rol) {
        if (repo.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El email ya está registrado");
        }
        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setEmail(email.toLowerCase());
        u.setPassword(encoder.encode(password));
        u.setRol(rol);
        return repo.save(u);
    }

    public AuthResponse login(LoginRequest r) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(r.email().toLowerCase(), r.password()));
        Usuario u = repo.findByEmail(r.email().toLowerCase()).orElseThrow();
        return respuesta(u);
    }

    public AuthResponse refrescar(String refreshToken) {
        try {
            String email = jwt.extraerEmail(refreshToken);
            UserDetails ud = uds.loadUserByUsername(email);
            if (!ud.isEnabled() || !jwt.esValido(refreshToken, ud, JwtService.REFRESH)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token inválido");
            }
            return respuesta(repo.findByEmail(email).orElseThrow());
        } catch (JwtException | IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token inválido o expirado");
        }
    }

    private AuthResponse respuesta(Usuario u) {
        UserDetails ud = uds.loadUserByUsername(u.getEmail());
        return new AuthResponse(jwt.generarAccessToken(ud), jwt.generarRefreshToken(ud),
                "Bearer", u.getNombre(), u.getEmail(), u.getRol());
    }
}