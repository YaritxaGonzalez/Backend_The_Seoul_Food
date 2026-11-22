package cl.theseoulfood.theseoulfood.controller;

import cl.theseoulfood.theseoulfood.dto.OrdenDtos.CambiarEstadoRequest;
import cl.theseoulfood.theseoulfood.dto.OrdenDtos.CrearOrdenRequest;
import cl.theseoulfood.theseoulfood.dto.OrdenDtos.OrdenResponse;
import cl.theseoulfood.theseoulfood.service.OrdenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ordenes")
@Tag(name = "Órdenes")
public class OrdenController {

    private final OrdenService service;

    public OrdenController(OrdenService service) {
        this.service = service;
    }

    @Operation(summary = "Crear una orden (CLIENTE o ADMIN). Descuenta stock y calcula el total")
    @PostMapping
    public ResponseEntity<OrdenResponse> crear(@Valid @RequestBody CrearOrdenRequest req, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(auth.getName(), req));
    }

    @Operation(summary = "Mis órdenes (usuario autenticado)")
    @GetMapping("/mias")
    public List<OrdenResponse> mias(Authentication auth) {
        return service.misOrdenes(auth.getName());
    }

    @Operation(summary = "Listar todas las órdenes (ADMIN y VENDEDOR)")
    @GetMapping
    public List<OrdenResponse> listar() {
        return service.listar();
    }

    @Operation(summary = "Detalle de una orden (ADMIN y VENDEDOR)")
    @GetMapping("/{id}")
    public OrdenResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @Operation(summary = "Cambiar estado (ADMIN). Si es CANCELADA, devuelve el stock")
    @PutMapping("/{id}/estado")
    public OrdenResponse estado(@PathVariable Long id, @Valid @RequestBody CambiarEstadoRequest req) {
        return service.cambiarEstado(id, req.estado());
    }

    @Operation(summary = "Eliminar una orden (ADMIN)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}