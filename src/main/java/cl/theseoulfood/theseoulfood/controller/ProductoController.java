package cl.theseoulfood.theseoulfood.controller;

import cl.theseoulfood.theseoulfood.model.Producto;
import cl.theseoulfood.theseoulfood.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/productos")
@Tag(name = "Productos", description = "CRUD de productos")
public class ProductoController {

    private final ProductoService service;

    public ProductoController(ProductoService service) {
        this.service = service;
    }

    @Operation(summary = "Listar todos los productos")
    @GetMapping
    public List<Producto> listar() {
        return service.listar();
    }

    @Operation(summary = "Obtener un producto por ID")
    @GetMapping("/{id}")
    public Producto obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @Operation(summary = "Crear un producto")
    @PostMapping
    public ResponseEntity<Producto> crear(@Valid @RequestBody Producto p) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(p));
    }

    @Operation(summary = "Actualizar un producto")
    @PutMapping("/{id}")
    public Producto actualizar(@PathVariable Long id, @Valid @RequestBody Producto p) {
        return service.actualizar(id, p);
    }

    @Operation(summary = "Subir o reemplazar la imagen de un producto (JPG, PNG o WEBP, máx. 2 MB)")
    @PostMapping(value = "/{id}/imagen", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Producto subirImagen(@PathVariable Long id, @RequestPart("archivo") MultipartFile archivo) {
        return service.actualizarImagen(id, archivo);
    }

    @Operation(summary = "Eliminar un producto")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}