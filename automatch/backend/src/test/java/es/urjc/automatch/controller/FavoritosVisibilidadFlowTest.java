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
 * Los favoritos respetan la visibilidad de los anuncios: no se puede marcar por
 * id un anuncio pendiente ajeno, y un favorito que vuelve a moderacion desaparece
 * del listado (en lugar de mostrar una tarjeta cuya ficha daria 404).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class FavoritosVisibilidadFlowTest {

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
        otroToken = register("compradora@automatch.es", "compra123456", "Compradora");
    }

    private String login(String email, String password) throws Exception {
        MvcResult r = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", email, "password", password))))
                .andExpect(status().isOk()).andReturn();
        return objectMapper.readTree(r.getResponse().getContentAsString()).get("token").asText();
    }

    private String register(String email, String password, String name) throws Exception {
        MvcResult r = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", email, "password", password, "name", name))))
                .andExpect(status().isOk()).andReturn();
        return objectMapper.readTree(r.getResponse().getContentAsString()).get("token").asText();
    }

    private Map<String, Object> coche() {
        Map<String, Object> c = new HashMap<>();
        c.put("brand", "Seat"); c.put("model", "León"); c.put("year", 2024); c.put("price", 20000);
        c.put("type", "COMPACTO"); c.put("fuel", "GASOLINA"); c.put("dgtLabel", "C");
        c.put("consumption", 5.5); c.put("powerCv", 150); c.put("trunkLiters", 380); c.put("seats", 5);
        c.put("description", "Anuncio para probar favoritos");
        return c;
    }

    private long crearYAprobar() throws Exception {
        MvcResult creacion = mockMvc.perform(post("/api/cars")
                        .header("Authorization", "Bearer " + demoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(coche())))
                .andExpect(status().isCreated()).andReturn();
        long id = objectMapper.readTree(creacion.getResponse().getContentAsString()).get("id").asLong();
        mockMvc.perform(put("/api/cars/" + id + "/approve")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
        return id;
    }

    @Test
    void noSePuedeMarcarFavoritoUnAnuncioPendienteAjeno() throws Exception {
        // Anuncio recien creado (PENDING), sin aprobar
        MvcResult creacion = mockMvc.perform(post("/api/cars")
                        .header("Authorization", "Bearer " + demoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(coche())))
                .andExpect(status().isCreated()).andReturn();
        long id = objectMapper.readTree(creacion.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(post("/api/favorites/" + id)
                        .header("Authorization", "Bearer " + otroToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void unFavoritoQueVuelveAModeracionDesapareceDelListado() throws Exception {
        long id = crearYAprobar();

        // La compradora lo marca como favorito y lo ve en su listado
        mockMvc.perform(post("/api/favorites/" + id)
                        .header("Authorization", "Bearer " + otroToken))
                .andExpect(status().isCreated());
        mockMvc.perform(get("/api/favorites").header("Authorization", "Bearer " + otroToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value((int) id));

        // El dueño edita el anuncio -> vuelve a PENDING
        mockMvc.perform(put("/api/cars/" + id)
                        .header("Authorization", "Bearer " + demoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(coche())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));

        // Ya no aparece en los favoritos de la compradora
        mockMvc.perform(get("/api/favorites").header("Authorization", "Bearer " + otroToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
