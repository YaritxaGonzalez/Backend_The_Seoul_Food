package cl.theseoulfood.theseoulfood.repository;

import cl.theseoulfood.theseoulfood.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
}