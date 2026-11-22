package cl.theseoulfood.theseoulfood.controller;

import cl.theseoulfood.theseoulfood.dto.AuthDtos.AuthResponse;
import cl.theseoulfood.theseoulfood.dto.AuthDtos.LoginRequest;
import cl.theseoulfood.theseoulfood.dto.AuthDtos.RefreshRequest;
import cl.theseoulfood.theseoulfood.dto.AuthDtos.RegisterRequest;
import cl.theseoulfood.theseoulfood.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticación")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @Operation(summary = "Registro público (crea un CLIENTE)")
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrar(r));
    }

    @Operation(summary = "Iniciar sesión")
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest r) {
        return service.login(r);
    }

    @Operation(summary = "Renovar tokens con el refresh token")
    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest r) {
        return service.refrescar(r.refreshToken());
    }
}