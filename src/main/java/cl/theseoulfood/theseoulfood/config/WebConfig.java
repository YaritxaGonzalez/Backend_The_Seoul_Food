package cl.theseoulfood.theseoulfood.config;

import cl.theseoulfood.theseoulfood.service.ImagenStorageService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final ImagenStorageService storage;

    public WebConfig(ImagenStorageService storage) {
        this.storage = storage;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(ImagenStorageService.URL_BASE + "**")
            .addResourceLocations(storage.getDirectorio().toUri().toString());
    }
}