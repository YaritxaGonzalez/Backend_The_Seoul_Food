package cl.theseoulfood.theseoulfood.dto;

import cl.theseoulfood.theseoulfood.model.EstadoOrden;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.List;

public class OrdenDtos {

    public record ItemRequest(
        @NotNull Long productoId,
        @NotNull @Min(1) Integer cantidad) {}

    public record CrearOrdenRequest(
        @NotEmpty(message = "La orden debe tener al menos un producto")
        List<@Valid ItemRequest> items) {}

    public record CambiarEstadoRequest(@NotNull EstadoOrden estado) {}

    public record DetalleResponse(
        Long productoId, String producto, Integer cantidad,
        Double precioUnitario, Double subtotal) {}

    public record OrdenResponse(
        Long id, String clienteNombre, String clienteEmail,
        LocalDateTime fecha, EstadoOrden estado, Double total,
        List<DetalleResponse> detalles) {}
}