package cl.theseoulfood.theseoulfood.dto;

import cl.theseoulfood.theseoulfood.model.Rol;
import jakarta.validation.constraints.*;

public class AuthDtos {

    public record LoginRequest(
        @NotBlank @Email String email,
        @NotBlank String password) {}

    public record RegisterRequest(
        @NotBlank @Size(max = 120) String nombre,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, message = "Mínimo 8 caracteres") String password) {}

    public record RefreshRequest(@NotBlank String refreshToken) {}

    public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tipo,
        String nombre,
        String email,
        Rol rol) {}

    // Solo para el ADMIN: puede elegir el rol
    public record CrearUsuarioRequest(
        @NotBlank @Size(max = 120) String nombre,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8) String password,
        @NotNull Rol rol) {}

    public record CambiarRolRequest(@NotNull Rol rol) {}

    public record UsuarioResponse(Long id, String nombre, String email, Rol rol, boolean activo) {}
}