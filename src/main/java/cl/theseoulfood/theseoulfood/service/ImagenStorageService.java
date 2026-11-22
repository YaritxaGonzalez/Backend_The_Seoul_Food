package cl.theseoulfood.theseoulfood.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ImagenStorageService {

    public static final String URL_BASE = "/uploads/";

    private static final Map<String, String> EXTENSIONES = Map.of(
        "image/jpeg", ".jpg",
        "image/png", ".png",
        "image/webp", ".webp"
    );

    private final Path directorio;

    public ImagenStorageService(@Value("${app.uploads.dir:uploads}") String dir) {
        this.directorio = Paths.get(dir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(directorio);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo crear la carpeta de imágenes", e);
        }
    }

    public Path getDirectorio() {
        return directorio;
    }

    public String guardar(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debes enviar una imagen");
        }
        String extension = EXTENSIONES.get(archivo.getContentType());
        if (extension == null) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "Formato no permitido. Usa JPG, PNG o WEBP");
        }
        String nombre = UUID.randomUUID() + extension;
        try (InputStream in = archivo.getInputStream()) {
            Files.copy(in, directorio.resolve(nombre), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                "No se pudo guardar la imagen");
        }
        return URL_BASE + nombre;
    }

    /** Borra el archivo solo si fue subido por el backend. */
    public void eliminar(String url) {
        if (url == null || !url.startsWith(URL_BASE)) return;
        String nombre = Paths.get(url.substring(URL_BASE.length())).getFileName().toString();
        try {
            Files.deleteIfExists(directorio.resolve(nombre));
        } catch (IOException ignored) {
            // si no se pudo borrar, no bloqueamos la operación del usuario
        }
    }
}