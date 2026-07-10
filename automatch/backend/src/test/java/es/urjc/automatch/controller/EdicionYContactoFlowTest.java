package es.urjc.automatch.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Flujos de producto sobre anuncios de usuario:
 *  - Edicion por el dueño con re-moderacion (APPROVED -> editar -> PENDING).
 *  - El admin edita sin alterar el estado.
 *  - Contacto del vendedor: solo autenticados y solo en anuncios aprobados.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class EdicionYContactoFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String demoToken;
    private String adminToken;
    private String otroToken;

    @BeforeEach
    void setUp() throws Exception {
        demoToken = login("demo@automatch.es", "demo123");
        adminToken = login("admin@automatch.es", "admin123");
        otroToken = register("otro@automatch.es", "otro123456", "Otro Usuario");
    }

    private String login(String email, String password) throws Exception {
        Map<String, String> request = Map.of("email", email, "password", password);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    private String register(String email, String password, String name) throws Exception {
        Map<String, String> request = Map.of("email", email, "password", password, "name", name);
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    private Map<String, Object> cocheValido() {
        Map<String, Object> car = new HashMap<>();
        car.put("brand", "Marca Editable");
        car.put("model", "Modelo Editable");
        car.put("year", 2025);
        car.put("price", 18000);
        car.put("type", "COMPACTO");
        car.put("fuel", "GASOLINA");
        car.put("dgtLabel", "C");
        car.put("consumption", 5.5);
        car.put("powerCv", 120);
        car.put("trunkLiters", 400);
        car.put("seats", 5);
        car.put("reliability", 7);
        car.put("safety", 7);
        car.put("equipment", 7);
        car.put("maintenancePerYear", 350);
        car.put("depreciation5y", 0.5);
        car.put("description", "Anuncio de prueba para el flujo de edicion");
        return car;
    }

    private long crearAnuncioDemo() throws Exception {
        MvcResult creacion = mockMvc.perform(post("/api/cars")
                        .header("Authorization", "Bearer " + demoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cocheValido())))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(creacion.getResponse().getContentAsString()).get("id").asLong();
    }

    private void aprobar(long carId) throws Exception {
        mockMvc.perform(put("/api/cars/" + carId + "/approve")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void elDuenoEditaSuAnuncioAprobadoYVuelveAModeracion() throws Exception {
        long carId = crearAnuncioDemo();
        aprobar(carId);

        // Un tercero autenticado no puede editar el anuncio ajeno
        mockMvc.perform(put("/api/cars/" + carId)
                        .header("Authorization", "Bearer " + otroToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cocheValido())))
                .andExpect(status().isForbidden());

        // El dueño edita: el precio cambia y el anuncio vuelve a PENDING
        Map<String, Object> editado = cocheValido();
        editado.put("price", 16500);
        mockMvc.perform(put("/api/cars/" + carId)
                        .header("Authorization", "Bearer " + demoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(editado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(16500.0))
                .andExpect(jsonPath("$.status").value("PENDING"));

        // Al estar pendiente de nuevo, desaparece del catalogo publico por id para terceros
        mockMvc.perform(get("/api/cars/" + carId)
                        .header("Authorization", "Bearer " + otroToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void elAdminEditaSinCambiarElEstado() throws Exception {
        long carId = crearAnuncioDemo();
        aprobar(carId);

        Map<String, Object> editado = cocheValido();
        editado.put("price", 17000);
        mockMvc.perform(put("/api/cars/" + carId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(editado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(17000.0))
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void elContactoDelVendedorSoloEstaDisponibleEnAnunciosAprobados() throws Exception {
        long carId = crearAnuncioDemo();

        // Anonimo -> 401
        mockMvc.perform(get("/api/cars/" + carId + "/contact"))
                .andExpect(status().isUnauthorized());

        // Autenticado pero anuncio PENDING -> 404
        mockMvc.perform(get("/api/cars/" + carId + "/contact")
                        .header("Authorization", "Bearer " + otroToken))
                .andExpect(status().isNotFound());

        aprobar(carId);

        // Aprobado -> devuelve nombre y email del vendedor
        mockMvc.perform(get("/api/cars/" + carId + "/contact")
                        .header("Authorization", "Bearer " + otroToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Usuario Demo"))
                .andExpect(jsonPath("$.email").value("demo@automatch.es"));
    }

    @Test
    void losCochesDelCatalogoOficialNoTienenContacto() throws Exception {
        // El coche 1 es del catalogo seed (sin ownerId)
        mockMvc.perform(get("/api/cars/1/contact")
                        .header("Authorization", "Bearer " + otroToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void elJsonPublicoDelCocheNoExponeElEmailDelVendedor() throws Exception {
        long carId = crearAnuncioDemo();
        aprobar(carId);

        MvcResult ficha = mockMvc.perform(get("/api/cars/" + carId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userListing").value(true))
                .andReturn();

        String json = ficha.getResponse().getContentAsString();
        org.assertj.core.api.Assertions.assertThat(json)
                .doesNotContain("demo@automatch.es")
                .doesNotContain("ownerId");
    }
}
