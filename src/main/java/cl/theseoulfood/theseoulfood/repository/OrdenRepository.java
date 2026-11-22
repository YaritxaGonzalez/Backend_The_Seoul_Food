package cl.theseoulfood.theseoulfood.repository;

import cl.theseoulfood.theseoulfood.model.Orden;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrdenRepository extends JpaRepository<Orden, Long> {
    List<Orden> findByUsuarioEmailOrderByFechaDesc(String email);
}