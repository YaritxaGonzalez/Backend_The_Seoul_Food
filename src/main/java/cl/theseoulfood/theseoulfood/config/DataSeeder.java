package cl.theseoulfood.theseoulfood.config;

import cl.theseoulfood.theseoulfood.model.Rol;
import cl.theseoulfood.theseoulfood.repository.UsuarioRepository;
import cl.theseoulfood.theseoulfood.service.AuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

    private void crear(UsuarioRepository repo, AuthService auth,
            String nombre, String email, String pass, Rol rol) {
        if (!repo.existsByEmail(email)) {
            auth.crear(nombre, email, pass, rol);
        }
    }
}