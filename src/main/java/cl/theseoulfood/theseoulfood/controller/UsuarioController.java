package cl.theseoulfood.theseoulfood.controller;

import cl.theseoulfood.theseoulfood.dto.AuthDtos.CambiarRolRequest;
import cl.theseoulfood.theseoulfood.dto.AuthDtos.CrearUsuarioRequest;
import cl.theseoulfood.theseoulfood.dto.AuthDtos.UsuarioResponse;
import cl.theseoulfood.theseoulfood.model.Usuario;
import cl.theseoulfood.theseoulfood.repository.UsuarioRepository;
import cl.theseoulfood.theseoulfood.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/usuarios")
@Tag(name = "Usuarios (solo ADMIN)")
public class UsuarioController {

    private final UsuarioRepository repo;
    private final AuthService authService;

    public UsuarioController(UsuarioRepository repo, AuthService authService) {
        this.repo = repo;
        this.authService = authService;
    }

    private UsuarioResponse aDto(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getNombre(), u.getEmail(), u.getRol(), u.isActivo());
    }

    @Operation(summary = "Listar usuarios")
    @GetMapping
    public List<UsuarioResponse> listar() {
        return repo.findAll().stream().map(this::aDto).toList();
    }

    @Operation(summary = "Crear usuario con rol (ADMIN, VENDEDOR o CLIENTE)")
    @PostMapping
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody CrearUsuarioRequest r) {
        Usuario u = authService.crear(r.nombre(), r.email(), r.password(), r.rol());
        return ResponseEntity.status(HttpStatus.CREATED).body(aDto(u));
    }

    @Operation(summary = "Cambiar el rol de un usuario")
    @PutMapping("/{id}/rol")
    public UsuarioResponse cambiarRol(@PathVariable Long id, @Valid @RequestBody CambiarRolRequest r) {
        Usuario u = repo.findById(id).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        u.setRol(r.rol());
        return aDto(repo.save(u));
    }

    @Operation(summary = "Activar o desactivar un usuario")
    @PutMapping("/{id}/activo")
    public UsuarioResponse activar(@PathVariable Long id, @RequestParam boolean valor) {
        Usuario u = repo.findById(id).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        u.setActivo(valor);
        return aDto(repo.save(u));
    }
}