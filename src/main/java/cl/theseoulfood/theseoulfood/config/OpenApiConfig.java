package cl.theseoulfood.theseoulfood.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        final String scheme = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("The Seoul Food API")
                        .version("1.0")
                        .description("API REST de The Seoul Food. Para probar los endpoints protegidos: "
                                + "1) inicia sesión en POST /api/v1/auth/login, 2) copia el accessToken, "
                                + "3) pulsa Authorize y pégalo. Roles: ADMIN (acceso total), "
                                + "VENDEDOR (ver productos y órdenes), CLIENTE (tienda y sus compras)."))
                .addSecurityItem(new SecurityRequirement().addList(scheme))
                .components(new Components().addSecuritySchemes(scheme,
                        new SecurityScheme()
                                .name(scheme)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}