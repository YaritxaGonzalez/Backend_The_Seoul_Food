package cl.theseoulfood.theseoulfood.service;

import cl.theseoulfood.theseoulfood.model.Producto;
import cl.theseoulfood.theseoulfood.repository.ProductoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ProductoService {

    private final ProductoRepository repository;
    private final ImagenStorageService imagenes;

    public ProductoService(ProductoRepository repository, ImagenStorageService imagenes) {
        this.repository = repository;
        this.imagenes = imagenes;
    }

    public List<Producto> listar() {
        return repository.findAll();
    }

    public Producto obtener(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Producto no encontrado: " + id));
    }

    public Producto crear(Producto p) {
        // Regla de negocio: no permitir nombres duplicados
        boolean existe = repository.findAll().stream()
            .anyMatch(x -> x.getNombre().equalsIgnoreCase(p.getNombre()));
        if (existe) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT, "Ya existe un producto con ese nombre");
        }
        return repository.save(p);
    }

    public Producto actualizar(Long id, Producto datos) {
        Producto p = obtener(id);
        p.setNombre(datos.getNombre());
        p.setDescripcion(datos.getDescripcion());
        p.setPrecio(datos.getPrecio());
        p.setCategoria(datos.getCategoria());
        p.setStock(datos.getStock());
        if (datos.getImagen() != null) {
            p.setImagen(datos.getImagen());
        }
        return repository.save(p);
    }

    public Producto actualizarImagen(Long id, MultipartFile archivo) {
        Producto p = obtener(id);
        String nuevaUrl = imagenes.guardar(archivo);   // valida formato y guarda
        String anterior = p.getImagen();
        p.setImagen(nuevaUrl);
        Producto guardado = repository.save(p);
        imagenes.eliminar(anterior);               
        return guardado;
    }

    public void eliminar(Long id) {
        Producto p = obtener(id);
        repository.delete(p);
        imagenes.eliminar(p.getImagen());
    }
}