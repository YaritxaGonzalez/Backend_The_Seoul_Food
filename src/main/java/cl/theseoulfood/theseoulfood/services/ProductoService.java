package cl.theseoulfood.theseoulfood.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cl.theseoulfood.theseoulfood.dto.ProductoDTO;
import cl.theseoulfood.theseoulfood.model.Producto;
import cl.theseoulfood.theseoulfood.repository.ProductoRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    public ProductoDTO crearProducto(Producto producto) {
        log.info("Iniciando registro de nuevo producto: {}", producto.getNombre_producto());
        producto.setId_producto(null); // el id lo genera la BD, el cliente no puede forzarlo
        Producto guardado = productoRepository.save(producto);
        log.info("Producto registrado exitosamente. ID: {}, Stock inicial: {}", guardado.getId_producto(), guardado.getStock());
        return convertirADTO(guardado);
    }

    public List<ProductoDTO> obtenerTodos() {
        log.info("Consultando listado de productos");
        return productoRepository.findAll().stream()
                .sorted(Comparator.comparing(Producto::getId_producto))
                .map(this::convertirADTO)
                .toList();
    }

    public ProductoDTO buscarPorId(Integer id) {
        return convertirADTO(buscarProducto(id));
    }

    public ProductoDTO actualizarProducto(Integer id, Producto datosNuevos) {
        log.info("Actualizando información del producto ID: {}", id);

        Producto producto = buscarProducto(id);

        producto.setNombre_producto(datosNuevos.getNombre_producto());
        producto.setImagen_producto(datosNuevos.getImagen_producto());
        producto.setDescripcion_producto(datosNuevos.getDescripcion_producto());
        producto.setPrecio_producto(datosNuevos.getPrecio_producto());
        producto.setStock(datosNuevos.getStock());

        if (producto.getStock() < 5) {
            log.warn("¡ALERTA! El producto '{}' (ID: {}) tiene stock bajo: {}",
                    producto.getNombre_producto(), id, producto.getStock());
        }
        return convertirADTO(producto);
    }

    public void eliminar(Integer id) {
        log.warn("Solicitud para eliminar producto ID: {}", id);
        Producto producto = buscarProducto(id);
        productoRepository.delete(producto);
        log.info("Producto ID: {} ('{}') eliminado definitivamente", id, producto.getNombre_producto());
    }

    private Producto buscarProducto(Integer id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("¡Producto no encontrado!"));
    }

    private ProductoDTO convertirADTO(Producto producto) {
        ProductoDTO dto = new ProductoDTO();
        dto.setId_producto(producto.getId_producto());
        dto.setNombre_producto(producto.getNombre_producto());
        dto.setImagen_producto(producto.getImagen_producto());
        dto.setDescripcion_producto(producto.getDescripcion_producto());
        dto.setPrecio_producto(producto.getPrecio_producto());
        dto.setStock(producto.getStock());
        return dto;
    }
}