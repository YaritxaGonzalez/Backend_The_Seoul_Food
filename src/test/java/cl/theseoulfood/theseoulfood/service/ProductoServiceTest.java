package cl.theseoulfood.theseoulfood.service;

import cl.theseoulfood.theseoulfood.model.Producto;
import cl.theseoulfood.theseoulfood.repository.ProductoRepository;
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
class ProductoServiceTest {

    @Mock ProductoRepository repo;
    @Mock ImagenStorageService imagenes;
    @InjectMocks ProductoService service;

    private Producto producto(String nombre) {
        Producto p = new Producto();
        p.setNombre(nombre);
        p.setPrecio(1000.0);
        p.setStock(10);
        return p;
    }

    @Test
    void crearGuardaProductoNuevo() {
        when(repo.findAll()).thenReturn(List.of());
        when(repo.save(any(Producto.class))).thenAnswer(i -> i.getArgument(0));

        Producto creado = service.crear(producto("Bibimbap"));

        assertEquals("Bibimbap", creado.getNombre());
        verify(repo).save(any(Producto.class));
    }

    @Test
    void crearRechazaNombreDuplicadoSinImportarMayusculas() {
        when(repo.findAll()).thenReturn(List.of(producto("Bibimbap")));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> service.crear(producto("BIBIMBAP")));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(repo, never()).save(any());
    }

    @Test
    void obtenerLanza404SiNoExiste() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> service.obtener(99L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void actualizarCambiaLosCampos() {
        Producto existente = producto("Kimchi");
        when(repo.findById(1L)).thenReturn(Optional.of(existente));
        when(repo.save(any(Producto.class))).thenAnswer(i -> i.getArgument(0));

        Producto datos = producto("Kimchi Picante");
        datos.setPrecio(2500.0);

        Producto r = service.actualizar(1L, datos);

        assertEquals("Kimchi Picante", r.getNombre());
        assertEquals(2500.0, r.getPrecio());
    }

    @Test
    void eliminarBorraElProducto() {
        Producto p = producto("Kimchi");
        when(repo.findById(1L)).thenReturn(Optional.of(p));

        service.eliminar(1L);

        verify(repo).delete(p);
    }
}