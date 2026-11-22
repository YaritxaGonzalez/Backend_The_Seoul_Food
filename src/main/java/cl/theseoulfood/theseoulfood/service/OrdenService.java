package cl.theseoulfood.theseoulfood.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import cl.theseoulfood.theseoulfood.dto.OrdenDtos.CrearOrdenRequest;
import cl.theseoulfood.theseoulfood.dto.OrdenDtos.DetalleResponse;
import cl.theseoulfood.theseoulfood.dto.OrdenDtos.ItemRequest;
import cl.theseoulfood.theseoulfood.dto.OrdenDtos.OrdenResponse;
import cl.theseoulfood.theseoulfood.model.DetalleOrden;
import cl.theseoulfood.theseoulfood.model.EstadoOrden;
import cl.theseoulfood.theseoulfood.model.Orden;
import cl.theseoulfood.theseoulfood.model.Producto;
import cl.theseoulfood.theseoulfood.model.Usuario;
import cl.theseoulfood.theseoulfood.repository.OrdenRepository;
import cl.theseoulfood.theseoulfood.repository.ProductoRepository;
import cl.theseoulfood.theseoulfood.repository.UsuarioRepository;

@Service
public class OrdenService {

    private final OrdenRepository ordenRepo;
    private final ProductoRepository productoRepo;
    private final UsuarioRepository usuarioRepo;

    public OrdenService(OrdenRepository ordenRepo, ProductoRepository productoRepo,
                        UsuarioRepository usuarioRepo) {
        this.ordenRepo = ordenRepo;
        this.productoRepo = productoRepo;
        this.usuarioRepo = usuarioRepo;
    }

    @Transactional
    public OrdenResponse crear(String email, CrearOrdenRequest req) {
        Usuario usuario = usuarioRepo.findByEmail(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no válido"));

        // Une líneas repetidas del mismo producto
        Map<Long, Integer> items = new LinkedHashMap<>();
        for (ItemRequest i : req.items()) {
            items.merge(i.productoId(), i.cantidad(), Integer::sum);
        }

        Orden orden = new Orden();
        orden.setUsuario(usuario);
        double total = 0;

        for (var e : items.entrySet()) {
            Producto p = productoRepo.findByIdForUpdate(e.getKey())
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Producto no encontrado: " + e.getKey()));
            int cant = e.getValue();
            if (p.getStock() == null || p.getStock() < cant) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Stock insuficiente para '" + p.getNombre() + "' (disponible: " + p.getStock() + ")");
            }
            p.setStock(p.getStock() - cant);

            DetalleOrden d = new DetalleOrden();
            d.setProducto(p);
            d.setCantidad(cant);
            d.setPrecioUnitario(p.getPrecio());
            orden.agregarDetalle(d);
            total += p.getPrecio() * cant;
        }
        orden.setTotal(total);
        return aDto(ordenRepo.save(orden));
    }

    @Transactional(readOnly = true)
    public List<OrdenResponse> listar() {
        return ordenRepo.findAll().stream().map(this::aDto).toList();
    }

    @Transactional(readOnly = true)
    public List<OrdenResponse> misOrdenes(String email) {
        return ordenRepo.findByUsuarioEmailOrderByFechaDesc(email).stream().map(this::aDto).toList();
    }

    @Transactional(readOnly = true)
    public OrdenResponse obtener(Long id) {
        return aDto(buscar(id));
    }

    @Transactional
    public OrdenResponse cambiarEstado(Long id, EstadoOrden nuevo) {
        Orden o = buscar(id);
        if (o.getEstado() == EstadoOrden.CANCELADA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Una orden cancelada no puede cambiar de estado");
        }
        if (nuevo == EstadoOrden.CANCELADA) {
            devolverStock(o);
        }
        o.setEstado(nuevo);
        return aDto(ordenRepo.save(o));
    }

    @Transactional
    public void eliminar(Long id) {
        Orden o = buscar(id);
        if (o.getEstado() != EstadoOrden.CANCELADA) {
            devolverStock(o);
        }
        ordenRepo.delete(o);
    }

    private void devolverStock(Orden o) {
        for (DetalleOrden d : o.getDetalles()) {
            Producto p = productoRepo.findByIdForUpdate(d.getProducto().getId()).orElseThrow();
            p.setStock(p.getStock() + d.getCantidad());
        }
    }

    private Orden buscar(Long id) {
        return ordenRepo.findById(id).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Orden no encontrada: " + id));
    }

    private OrdenResponse aDto(Orden o) {
        var detalles = o.getDetalles().stream().map(d -> new DetalleResponse(
            d.getProducto().getId(), d.getProducto().getNombre(), d.getCantidad(),
            d.getPrecioUnitario(), d.getPrecioUnitario() * d.getCantidad())).toList();
        return new OrdenResponse(o.getId(), o.getUsuario().getNombre(), o.getUsuario().getEmail(),
            o.getFecha(), o.getEstado(), o.getTotal(), detalles);
    }
}