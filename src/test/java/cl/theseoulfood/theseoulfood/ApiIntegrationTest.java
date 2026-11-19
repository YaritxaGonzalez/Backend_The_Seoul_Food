package cl.theseoulfood.theseoulfood;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ApiIntegrationTest {

    private static final String PASS = "Test12345!";

    @Autowired MockMvc mvc;

    // ---------- helpers ----------

    private String extraer(String json, String regex) {
        Matcher m = Pattern.compile(regex).matcher(json);
        assertTrue(m.find(), "No se encontró " + regex + " en: " + json);
        return m.group(1);
    }

    private String loginJson(String email) throws Exception {
        return mvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"" + PASS + "\"}"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
    }

    private String token(String email) throws Exception {
        return extraer(loginJson(email), "\"accessToken\":\"([^\"]+)\"");
    }

    private String bearer(String email) throws Exception {
        return "Bearer " + token(email);
    }

    private String productoJson(String nombre, int stock) {
        return "{\"nombre\":\"" + nombre + "\",\"descripcion\":\"test\","
            + "\"precio\":5000,\"categoria\":\"Test\",\"stock\":" + stock + "}";
    }

    private String crearProducto(String nombre, int stock) throws Exception {
        String res = mvc.perform(post("/api/v1/productos")
                .header("Authorization", bearer("admin@seoulfood.cl"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(productoJson(nombre, stock)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        return extraer(res, "\"id\":(\\d+)");
    }

    // ---------- autenticación ----------

    @Test
    void loginConCredencialesCorrectasDevuelveTokens() throws Exception {
        String res = loginJson("admin@seoulfood.cl");
        assertTrue(res.contains("accessToken") && res.contains("refreshToken"));
        assertTrue(res.contains("\"rol\":\"ADMIN\""));
    }

    @Test
    void loginConPasswordIncorrectaDa401() throws Exception {
        mvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"admin@seoulfood.cl\",\"password\":\"incorrecta\"}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void registroPublicoCreaSiempreUnCliente() throws Exception {
        String email = "u" + UUID.randomUUID().toString().substring(0, 8) + "@correo.cl";
        String res = mvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Nuevo\",\"email\":\"" + email + "\",\"password\":\"Clave12345\"}"))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        assertTrue(res.contains("\"rol\":\"CLIENTE\""));
    }

    @Test
    void registroConPasswordCortaDa400() throws Exception {
        mvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"X\",\"email\":\"x@correo.cl\",\"password\":\"123\"}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void refreshConRefreshTokenDa200() throws Exception {
        String refresh = extraer(loginJson("admin@seoulfood.cl"), "\"refreshToken\":\"([^\"]+)\"");
        mvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + refresh + "\"}"))
            .andExpect(status().isOk());
    }

    @Test
    void refreshConAccessTokenDa401() throws Exception {
        String access = token("admin@seoulfood.cl");
        mvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + access + "\"}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenManipuladoDa401() throws Exception {
        String t = token("admin@seoulfood.cl");
        mvc.perform(get("/api/v1/productos")
                .header("Authorization", "Bearer " + t.substring(0, t.length() - 2) + "xx"))
            .andExpect(status().isUnauthorized());
    }

    // ---------- permisos por rol ----------

    @Test
    void productosSinTokenDa401() throws Exception {
        mvc.perform(get("/api/v1/productos")).andExpect(status().isUnauthorized());
    }

    @Test
    void lostresRolesPuedenVerProductos() throws Exception {
        for (String email : new String[]{"admin@seoulfood.cl", "vendedor@seoulfood.cl", "cliente@seoulfood.cl"}) {
            mvc.perform(get("/api/v1/productos").header("Authorization", bearer(email)))
                .andExpect(status().isOk());
        }
    }

    @Test
    void soloAdminCreaProductos() throws Exception {
        String nombre = "Plato-" + UUID.randomUUID();
        for (String email : new String[]{"vendedor@seoulfood.cl", "cliente@seoulfood.cl"}) {
            mvc.perform(post("/api/v1/productos")
                    .header("Authorization", bearer(email))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(productoJson(nombre, 5)))
                .andExpect(status().isForbidden());
        }
        mvc.perform(post("/api/v1/productos")
                .header("Authorization", bearer("admin@seoulfood.cl"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(productoJson(nombre, 5)))
            .andExpect(status().isCreated());
    }

    @Test
    void soloAdminGestionaUsuarios() throws Exception {
        mvc.perform(get("/api/v1/usuarios").header("Authorization", bearer("admin@seoulfood.cl")))
            .andExpect(status().isOk());
        mvc.perform(get("/api/v1/usuarios").header("Authorization", bearer("vendedor@seoulfood.cl")))
            .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/usuarios").header("Authorization", bearer("cliente@seoulfood.cl")))
            .andExpect(status().isForbidden());
    }

    @Test
    void ordenesLasVenAdminYVendedorPeroNoElCliente() throws Exception {
        mvc.perform(get("/api/v1/ordenes").header("Authorization", bearer("admin@seoulfood.cl")))
            .andExpect(status().isOk());
        mvc.perform(get("/api/v1/ordenes").header("Authorization", bearer("vendedor@seoulfood.cl")))
            .andExpect(status().isOk());
        mvc.perform(get("/api/v1/ordenes").header("Authorization", bearer("cliente@seoulfood.cl")))
            .andExpect(status().isForbidden());
    }

    @Test
    void vendedorNoPuedeCrearOrdenes() throws Exception {
        mvc.perform(post("/api/v1/ordenes")
                .header("Authorization", bearer("vendedor@seoulfood.cl"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"items\":[{\"productoId\":1,\"cantidad\":1}]}"))
            .andExpect(status().isForbidden());
    }

    // ---------- flujo de negocio completo ----------

    @Test
    void compraDescuentaStockYRechazaExceso() throws Exception {
        String id = crearProducto("Compra-" + UUID.randomUUID(), 5);
        String cliente = bearer("cliente@seoulfood.cl");

        // compra válida: 2 de 5
        mvc.perform(post("/api/v1/ordenes")
                .header("Authorization", cliente)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"items\":[{\"productoId\":" + id + ",\"cantidad\":2}]}"))
            .andExpect(status().isCreated());

        // el stock quedó en 3
        String prod = mvc.perform(get("/api/v1/productos/" + id).header("Authorization", cliente))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertTrue(prod.contains("\"stock\":3"), prod);

        // exceso: pide 4 y solo hay 3
        mvc.perform(post("/api/v1/ordenes")
                .header("Authorization", cliente)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"items\":[{\"productoId\":" + id + ",\"cantidad\":4}]}"))
            .andExpect(status().isConflict());

        // el stock sigue en 3 (rollback)
        String prod2 = mvc.perform(get("/api/v1/productos/" + id).header("Authorization", cliente))
            .andReturn().getResponse().getContentAsString();
        assertTrue(prod2.contains("\"stock\":3"), prod2);
    }

    @Test
    void ordenVaciaDa400() throws Exception {
        mvc.perform(post("/api/v1/ordenes")
                .header("Authorization", bearer("cliente@seoulfood.cl"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"items\":[]}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void cancelarOrdenDevuelveStock() throws Exception {
        String id = crearProducto("Cancel-" + UUID.randomUUID(), 5);
        String cliente = bearer("cliente@seoulfood.cl");
        String admin = bearer("admin@seoulfood.cl");

        String orden = mvc.perform(post("/api/v1/ordenes")
                .header("Authorization", cliente)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"items\":[{\"productoId\":" + id + ",\"cantidad\":5}]}"))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String ordenId = extraer(orden, "^\\{\"id\":(\\d+)");

        mvc.perform(put("/api/v1/ordenes/" + ordenId + "/estado")
                .header("Authorization", admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"estado\":\"CANCELADA\"}"))
            .andExpect(status().isOk());

        String prod = mvc.perform(get("/api/v1/productos/" + id).header("Authorization", admin))
            .andReturn().getResponse().getContentAsString();
        assertTrue(prod.contains("\"stock\":5"), prod);
    }
}