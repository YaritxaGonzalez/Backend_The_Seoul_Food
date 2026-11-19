package cl.theseoulfood.theseoulfood.service;

import cl.theseoulfood.theseoulfood.dto.OrdenDtos.*;
import cl.theseoulfood.theseoulfood.model.*;
import cl.theseoulfood.theseoulfood.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrdenServiceTest {

    @Mock OrdenRepository ordenRepo;
    @Mock ProductoRepository productoRepo;
    @Mock UsuarioRepository usuarioRepo;
    @InjectMocks OrdenService service;

    private Usuario cliente;
    private Producto bibimbap;

    @BeforeEach
    void preparar() {
        cliente = new Usuario();
        cliente.setNombre("Cliente");
        cliente.setEmail("cliente@seoulfood.cl");
        cliente.setRol(Rol.CLIENTE);

        bibimbap = new Producto();
        bibimbap.setId(1L);
        bibimbap.setNombre("Bibimbap");
        bibimbap.setPrecio(8990.0);
        bibimbap.setStock(10);
    }

    @Test
    void crearOrdenDescuentaStockYCalculaTotal() {
        when(usuarioRepo.findByEmail("cliente@seoulfood.cl")).thenReturn(Optional.of(cliente));
        when(productoRepo.findByIdForUpdate(1L)).thenReturn(Optional.of(bibimbap));
        when(ordenRepo.save(any(Orden.class))).thenAnswer(i -> i.getArgument(0));

        var req = new CrearOrdenRequest(List.of(new ItemRequest(1L, 3)));
        OrdenResponse r = service.crear("cliente@seoulfood.cl", req);

        assertEquals(8990.0 * 3, r.total());
        assertEquals(EstadoOrden.PENDIENTE, r.estado());
        assertEquals(7, bibimbap.getStock());
        assertEquals(1, r.detalles().size());
    }

    @Test
    void lineasRepetidasDelMismoProductoSeUnen() {
        when(usuarioRepo.findByEmail(any())).thenReturn(Optional.of(cliente));
        when(productoRepo.findByIdForUpdate(1L)).thenReturn(Optional.of(bibimbap));
        when(ordenRepo.save(any(Orden.class))).thenAnswer(i -> i.getArgument(0));

        var req = new CrearOrdenRequest(List.of(new ItemRequest(1L, 2), new ItemRequest(1L, 3)));
        OrdenResponse r = service.crear("cliente@seoulfood.cl", req);

        assertEquals(1, r.detalles().size());
        assertEquals(5, r.detalles().get(0).cantidad());
        assertEquals(5, bibimbap.getStock());
    }

    @Test
    void stockInsuficienteLanza409YNoGuarda() {
        when(usuarioRepo.findByEmail(any())).thenReturn(Optional.of(cliente));
        when(productoRepo.findByIdForUpdate(1L)).thenReturn(Optional.of(bibimbap));

        var req = new CrearOrdenRequest(List.of(new ItemRequest(1L, 11)));
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> service.crear("cliente@seoulfood.cl", req));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertEquals(10, bibimbap.getStock());
        verify(ordenRepo, never()).save(any());
    }

    @Test
    void productoInexistenteLanza404() {
        when(usuarioRepo.findByEmail(any())).thenReturn(Optional.of(cliente));
        when(productoRepo.findByIdForUpdate(99L)).thenReturn(Optional.empty());

        var req = new CrearOrdenRequest(List.of(new ItemRequest(99L, 1)));
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> service.crear("cliente@seoulfood.cl", req));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    private Orden ordenExistente(EstadoOrden estado, int cantidad) {
        bibimbap.setStock(10 - cantidad);
        DetalleOrden d = new DetalleOrden();
        d.setProducto(bibimbap);
        d.setCantidad(cantidad);
        d.setPrecioUnitario(bibimbap.getPrecio());
        Orden o = new Orden();
        o.setUsuario(cliente);
        o.setEstado(estado);
        o.setTotal(bibimbap.getPrecio() * cantidad);
        o.agregarDetalle(d);
        return o;
    }

    @Test
    void cancelarOrdenDevuelveElStock() {
        Orden o = ordenExistente(EstadoOrden.PENDIENTE, 4);
        when(ordenRepo.findById(5L)).thenReturn(Optional.of(o));
        when(productoRepo.findByIdForUpdate(1L)).thenReturn(Optional.of(bibimbap));
        when(ordenRepo.save(any(Orden.class))).thenAnswer(i -> i.getArgument(0));

        OrdenResponse r = service.cambiarEstado(5L, EstadoOrden.CANCELADA);

        assertEquals(EstadoOrden.CANCELADA, r.estado());
        assertEquals(10, bibimbap.getStock());
    }

    @Test
    void ordenCanceladaNoPuedeCambiarDeEstado() {
        Orden o = ordenExistente(EstadoOrden.CANCELADA, 2);
        when(ordenRepo.findById(5L)).thenReturn(Optional.of(o));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> service.cambiarEstado(5L, EstadoOrden.PAGADA));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void cambiarAPagadaNoTocaElStock() {
        Orden o = ordenExistente(EstadoOrden.PENDIENTE, 2);
        when(ordenRepo.findById(5L)).thenReturn(Optional.of(o));
        when(ordenRepo.save(any(Orden.class))).thenAnswer(i -> i.getArgument(0));

        service.cambiarEstado(5L, EstadoOrden.PAGADA);

        assertEquals(8, bibimbap.getStock());
        verify(productoRepo, never()).findByIdForUpdate(any());
    }

    @Test
    void ordenInexistenteLanza404() {
        when(ordenRepo.findById(77L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> service.obtener(77L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }
}