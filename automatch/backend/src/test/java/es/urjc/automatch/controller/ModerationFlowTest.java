package es.urjc.automatch.controller;

import com.fasterxml.jackson.databind.JsonNode;
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

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ModerationFlowTest {

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
        return Map.ofEntries(
                Map.entry("brand", "Marca Nueva"),
                Map.entry("model", "Modelo Nuevo"),
                Map.entry("year", 2025),
                Map.entry("price", 18000),
                Map.entry("type", "COMPACTO"),
                Map.entry("fuel", "GASOLINA"),
                Map.entry("dgtLabel", "C"),
                Map.entry("consumption", 5.5),
                Map.entry("powerCv", 120),
                Map.entry("trunkLiters", 400),
                Map.entry("seats", 5),
                Map.entry("reliability", 7),
                Map.entry("safety", 7),
                Map.entry("equipment", 7),
                Map.entry("maintenancePerYear", 350),
                Map.entry("depreciation5y", 0.5),
                Map.entry("description", "Anuncio de prueba creado por el usuario demo"));
    }

    private boolean catalogoContiene(MvcResult result, long carId) throws Exception {
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        // GET /api/cars devuelve { content: [...] }; mine y pending devuelven array plano
        JsonNode array = root.isArray() ? root : root.get("content");
        for (JsonNode node : array) {
            if (node.get("id").asLong() == carId) {
                return true;
            }
        }
        return false;
    }

    @Test
    void flujoCompletoDeModeracionDeUnAnuncio() throws Exception {
        // 1. El usuario demo crea un anuncio -> queda PENDING
        MvcResult creacion = mockMvc.perform(post("/api/cars")
                        .header("Authorization", "Bearer " + demoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cocheValido())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn();

        long carId = objectMapper.readTree(creacion.getResponse().getContentAsString()).get("id").asLong();

        // 2. No aparece en el catalogo publico (size=50 garantiza que caben todos los coches seed)
        MvcResult catalogoAntes = mockMvc.perform(get("/api/cars?size=50"))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(catalogoContiene(catalogoAntes, carId)).isFalse();

        // 3. No es accesible por id para terceros (404), tampoco anonimo
        mockMvc.perform(get("/api/cars/" + carId)
                        .header("Authorization", "Bearer " + otroToken))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/cars/" + carId))
                .andExpect(status().isNotFound());

        // 4. Aparece en "mis anuncios" del propietario
        MvcResult misAnuncios = mockMvc.perform(get("/api/cars/mine")
                        .header("Authorization", "Bearer " + demoToken))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(catalogoContiene(misAnuncios, carId)).isTrue();

        // 5. El admin lo ve en los anuncios pendientes de moderacion
        MvcResult pendientes = mockMvc.perform(get("/api/cars/pending")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(catalogoContiene(pendientes, carId)).isTrue();

        // 6. El admin lo aprueba
        mockMvc.perform(put("/api/cars/" + carId + "/approve")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        // 7. Ahora si aparece en el catalogo publico
        MvcResult catalogoDespues = mockMvc.perform(get("/api/cars?size=50"))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(catalogoContiene(catalogoDespues, carId)).isTrue();

        // 8. Un tercero autenticado no puede borrarlo
        mockMvc.perform(delete("/api/cars/" + carId)
                        .header("Authorization", "Bearer " + otroToken))
                .andExpect(status().isForbidden());

        // 9. El dueño si puede borrarlo
        mockMvc.perform(delete("/api/cars/" + carId)
                        .header("Authorization", "Bearer " + demoToken))
                .andExpect(status().isNoContent());
    }
}
