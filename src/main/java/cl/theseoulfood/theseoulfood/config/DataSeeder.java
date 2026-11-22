package cl.theseoulfood.theseoulfood.config;
import cl.theseoulfood.theseoulfood.model.Producto;
import cl.theseoulfood.theseoulfood.model.Rol;
import cl.theseoulfood.theseoulfood.repository.ProductoRepository;
import cl.theseoulfood.theseoulfood.repository.UsuarioRepository;
import cl.theseoulfood.theseoulfood.service.AuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedUsuarios(UsuarioRepository repo, AuthService auth,
            @Value("${seed.password}") String pass) {
        return args -> {
            crear(repo, auth, "Administrador", "admin@seoulfood.cl", pass, Rol.ADMIN);
            crear(repo, auth, "Vendedor", "vendedor@seoulfood.cl", pass, Rol.VENDEDOR);
            crear(repo, auth, "Cliente", "cliente@seoulfood.cl", pass, Rol.CLIENTE);
        };
    }

    @Bean
    CommandLineRunner seedProductos(ProductoRepository repo) {
        return args -> {
            List<Producto> iniciales = List.of(
                producto("Nongshim Chapagetti", "/imagenes/Nongshim-Chapagetti-Exp.webp", 3990.0,
                        "Fideos estilo jjajangmyeon con salsa de pasta de soja negra.", 10),
                producto("Nongshim Sarigomtang", "/imagenes/Nongshim-Sarigomtang-Ramyeon.webp", 3990.0,
                        "Ramyeon con sabor a caldo de huesos, suave y reconfortante.", 10),
                producto("Ottogi Jin Veggie", "/imagenes/Otoki-Jin-Veggie.webp", 3790.0,
                        "Versión vegetariana del clásico Jin Ramyeon.", 10),
                producto("Nongshim Shin Ramyeon", "/imagenes/Nongshim-Shin-Ramyeon-Ramyeon-1.webp", 3990.0,
                        "El ramyeon picante más icónico de Corea.", 10),
                producto("Nongshim Yukgaejang", "/imagenes/Nongshim-Yukgaejang-Ramyeon.webp", 3990.0,
                        "Sabor a sopa picante de res deshebrada.", 10),
                producto("Ottogi Ramyeon", "/imagenes/Ottogi-Fideos-de-Ramyeon.webp", 3790.0,
                        "Fideos clásicos estilo Ottogi.", 10),
                producto("Ottogi Jin Ramyeon Chicken", "/imagenes/Ottogi-Jin-Ramyeon-Chicken-1-1024x946.webp", 3790.0,
                        "Ramyeon con sabor a pollo, suave y aromático.", 10),
                producto("Ottogi Jin Ramyeon Spicy", "/imagenes/Ottogi-Jin-Ramyeon-Spicy-BTS.webp", 3790.0,
                        "Versión picante del clásico Jin Ramyeon.", 10),
                producto("Ottogi Kimchi Ramyeon", "/imagenes/Ottogi-Kimchi-Ramyeon.webp", 3790.0,
                        "Ramyeon con sabor a kimchi fermentado.", 10),
                producto("Sachun Chapagetti", "/imagenes/Sachun-chapagetti.webp", 3690.0,
                        "Fideos estilo jjajangmyeon, alternativa a Chapagetti.", 10),
                producto("Samyang Buldak Bokkeum Myeon", "/imagenes/Samyang-Buldak-Bokkeum-Myeon-Ramyeon-1.webp", 4290.0,
                        "El clásico ramyeon extra picante Buldak.", 10),
                producto("Samyang Buldak Quattro Cheese", "/imagenes/Samyang-Buldak-Quattro-Cheese.webp", 4390.0,
                        "Buldak picante con mezcla de cuatro quesos.", 10),
                producto("Samyang Cheese Buldak", "/imagenes/Samyang-Cheese-Buldak-Ramyeon.webp", 4290.0,
                        "Buldak picante con un toque cremoso de queso.", 10),
                producto("Samyang Cream Carbo Buldak", "/imagenes/Samyang-Cream-Carbo-Buldak-Ramyeon.webp", 4390.0,
                        "Versión carbonara cremosa del Buldak picante.", 10),
                producto("Samyang Hek Buldak Bokkeum Myeon", "/imagenes/Samyang-Hek-Buldak-Bokkeum-Myeon-1.webp", 4290.0,
                        "Buldak picante edición Hek, sabor intenso.", 10),
                producto("Samyang Suegogui Myeon", "/imagenes/Samyang-Suegogui-Myeon.webp", 3990.0,
                        "Ramyeon con sabor a carne de res.", 0)
            );
            for (Producto p : iniciales) {
                if (!repo.existsByNombreIgnoreCase(p.getNombre())) {
                    repo.save(p);
                }
            }
        };
    }

    private void crear(UsuarioRepository repo, AuthService auth,
            String nombre, String email, String pass, Rol rol) {
        if (!repo.existsByEmail(email)) {
            auth.crear(nombre, email, pass, rol);
        }
    }

    private Producto producto(String nombre, String imagen, Double precio,
            String descripcion, int stock) {
        Producto p = new Producto();
        p.setNombre(nombre);
        p.setImagen(imagen);
        p.setPrecio(precio);
        p.setDescripcion(descripcion);
        p.setCategoria("Ramyeon");
        p.setStock(stock);
        return p;
    }
}