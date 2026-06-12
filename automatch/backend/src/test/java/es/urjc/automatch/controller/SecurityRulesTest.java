package es.urjc.automatch.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.urjc.automatch.model.CarStatus;
import es.urjc.automatch.repository.CarRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SecurityRulesTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CarRepository carRepository;

    private String demoToken;
    private String adminToken;

    @BeforeEach
    void login() throws Exception {
        demoToken = login("demo@automatch.es", "demo123");
        adminToken = login("admin@automatch.es", "admin123");
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

    private Map<String, Object> cocheValido() {
        return Map.ofEntries(
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
                Map.entry("description", "Coche de prueba"));
    }

    @Test
    void unUsuarioNormalRecibe403EnEndpointsDeAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/stats").header("Authorization", "Bearer " + demoToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void unAdminPuedeAccederAEndpointsDeAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/stats").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void unUsuarioNormalRecibe403AlAprobarOrechazarAnuncios() throws Exception {
        Long carId = carRepository.findByStatus(CarStatus.APPROVED).get(0).getId();

        mockMvc.perform(put("/api/cars/" + carId + "/approve").header("Authorization", "Bearer " + demoToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/cars/" + carId + "/reject").header("Authorization", "Bearer " + demoToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void unAdminPuedeAprobarYRechazarAnuncios() throws Exception {
        Long carId = carRepository.findByStatus(CarStatus.APPROVED).get(0).getId();

        mockMvc.perform(put("/api/cars/" + carId + "/approve").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/cars/" + carId + "/reject").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void unUsuarioNormalRecibe403AlEditarUnCoche() throws Exception {
        Long carId = carRepository.findByStatus(CarStatus.APPROVED).get(0).getId();

        mockMvc.perform(put("/api/cars/" + carId)
                        .header("Authorization", "Bearer " + demoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cocheValido())))
                .andExpect(status().isForbidden());
    }

    @Test
    void unAdminPuedeEditarUnCoche() throws Exception {
        Long carId = carRepository.findByStatus(CarStatus.APPROVED).get(0).getId();

        mockMvc.perform(put("/api/cars/" + carId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cocheValido())))
                .andExpect(status().isOk());
    }
}
