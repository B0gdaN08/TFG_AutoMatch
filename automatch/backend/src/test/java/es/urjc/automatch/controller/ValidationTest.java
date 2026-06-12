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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String demoToken;

    @BeforeEach
    void login() throws Exception {
        Map<String, String> request = Map.of("email", "demo@automatch.es", "password", "demo123");
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();
        demoToken = objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    private Map<String, Object> cocheValido() {
        return new HashMap<>(Map.ofEntries(
                Map.entry("brand", "Marca"),
                Map.entry("model", "Modelo"),
                Map.entry("year", 2025),
                Map.entry("price", 20000),
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
                Map.entry("description", "Coche de prueba")));
    }

    @Test
    void unCocheConPrecioNegativoDevuelve400ConMensaje() throws Exception {
        Map<String, Object> coche = cocheValido();
        coche.put("price", -100);

        mockMvc.perform(post("/api/cars")
                        .header("Authorization", "Bearer " + demoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(coche)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void unCocheConAnioFueraDeRangoDevuelve400ConMensaje() throws Exception {
        Map<String, Object> coche = cocheValido();
        coche.put("year", 3000);

        mockMvc.perform(post("/api/cars")
                        .header("Authorization", "Bearer " + demoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(coche)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }
}
