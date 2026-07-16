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
 * Las puntuaciones expertas (fiabilidad, seguridad, equipamiento, mantenimiento,
 * depreciacion) las asigna el sistema desde la base de conocimiento (V3), no el
 * vendedor: aunque el cliente las envie infladas, se ignoran.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PuntuacionesAutomaticasFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String demoToken;
    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        demoToken = login("demo@automatch.es", "demo123");
        adminToken = login("admin@automatch.es", "admin123");
    }

    private String login(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", email, "password", password))))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    /** Anuncio con las puntuaciones infladas a 10 (el vendedor intenta hacer trampas). */
    private Map<String, Object> anuncioInflado(String brand, String model) {
        Map<String, Object> car = new HashMap<>();
        car.put("brand", brand);
        car.put("model", model);
        car.put("year", 2024);
        car.put("price", 21000);
        car.put("type", "COMPACTO");
        car.put("fuel", "HIBRIDO");
        car.put("dgtLabel", "ECO");
        car.put("consumption", 4.5);
        car.put("powerCv", 140);
        car.put("trunkLiters", 360);
        car.put("seats", 5);
        car.put("reliability", 10);
        car.put("safety", 10);
        car.put("equipment", 10);
        car.put("maintenancePerYear", 1);
        car.put("depreciation5y", 0.01);
        car.put("description", "Anuncio con puntuaciones infladas");
        return car;
    }

    @Test
    void elVendedorNoPuedeElegirLasPuntuaciones() throws Exception {
        // "Corolla" encaja con la ficha "Corolla 140H" de V3: 9.5 / 9.0 / 7.5
        mockMvc.perform(post("/api/cars")
                        .header("Authorization", "Bearer " + demoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(anuncioInflado("Toyota", "Corolla"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reliability").value(9.5))
                .andExpect(jsonPath("$.safety").value(9.0))
                .andExpect(jsonPath("$.equipment").value(7.5))
                .andExpect(jsonPath("$.maintenancePerYear").value(310.0))
                .andExpect(jsonPath("$.depreciation5y").value(0.40));
    }

    @Test
    void losCamposExpertosSonOpcionalesParaElVendedor() throws Exception {
        Map<String, Object> sinPuntuaciones = anuncioInflado("Seat", "León FR");
        sinPuntuaciones.remove("reliability");
        sinPuntuaciones.remove("safety");
        sinPuntuaciones.remove("equipment");
        sinPuntuaciones.remove("maintenancePerYear");
        sinPuntuaciones.remove("depreciation5y");

        // "León FR" contiene "León" -> ficha del Seat León: 7.5 / 8.5 / 7.5
        mockMvc.perform(post("/api/cars")
                        .header("Authorization", "Bearer " + demoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sinPuntuaciones)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reliability").value(7.5))
                .andExpect(jsonPath("$.safety").value(8.5))
                .andExpect(jsonPath("$.equipment").value(7.5));
    }

    @Test
    void unaMarcaDesconocidaRecibeLaMediaGlobalNoLosValoresInflados() throws Exception {
        MvcResult creacion = mockMvc.perform(post("/api/cars")
                        .header("Authorization", "Bearer " + demoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(anuncioInflado("MarcaInventada", "ModeloInventado"))))
                .andExpect(status().isCreated())
                .andReturn();

        var json = objectMapper.readTree(creacion.getResponse().getContentAsString());
        // No conserva los valores inflados: recibe medias razonables de la base de conocimiento
        org.assertj.core.api.Assertions.assertThat(json.get("reliability").asDouble()).isNotEqualTo(10.0).isBetween(5.0, 9.0);
        org.assertj.core.api.Assertions.assertThat(json.get("safety").asDouble()).isNotEqualTo(10.0).isBetween(5.0, 9.5);
        org.assertj.core.api.Assertions.assertThat(json.get("maintenancePerYear").asDouble()).isGreaterThan(100);
    }

    @Test
    void alEditarElAnuncioTampocoSePuedenInflar() throws Exception {
        MvcResult creacion = mockMvc.perform(post("/api/cars")
                        .header("Authorization", "Bearer " + demoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(anuncioInflado("Toyota", "Corolla"))))
                .andExpect(status().isCreated())
                .andReturn();
        long carId = objectMapper.readTree(creacion.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(put("/api/cars/" + carId)
                        .header("Authorization", "Bearer " + demoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(anuncioInflado("Toyota", "Corolla"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reliability").value(9.5))
                .andExpect(jsonPath("$.safety").value(9.0));
    }

    @Test
    void elCatalogoDeMarcasYModelosEsPublicoYVieneDeLaBaseDeConocimiento() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/cars/models"))
                .andExpect(status().isOk())
                .andReturn();

        var json = objectMapper.readTree(result.getResponse().getContentAsString());
        org.assertj.core.api.Assertions.assertThat(json.isArray()).isTrue();
        org.assertj.core.api.Assertions.assertThat(json.size()).isGreaterThanOrEqualTo(30); // 35 marcas con V4

        // Toyota debe existir y contener el Corolla 140H de la base de conocimiento
        boolean toyotaConCorolla = false;
        for (var marca : json) {
            if ("Toyota".equals(marca.get("brand").asText())) {
                for (var modelo : marca.get("models")) {
                    if ("Corolla 140H".equals(modelo.asText())) toyotaConCorolla = true;
                }
            }
        }
        org.assertj.core.api.Assertions.assertThat(toyotaConCorolla).isTrue();
    }

    @Test
    void elAdminSiPuedeFijarLasPuntuacionesManualmente() throws Exception {
        Map<String, Object> catalogo = anuncioInflado("Marca Curada", "Modelo Curado");
        catalogo.put("reliability", 8.2);
        catalogo.put("safety", 8.8);
        catalogo.put("equipment", 7.9);
        catalogo.put("maintenancePerYear", 415);
        catalogo.put("depreciation5y", 0.44);

        mockMvc.perform(post("/api/cars/catalog")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(catalogo)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reliability").value(8.2))
                .andExpect(jsonPath("$.safety").value(8.8))
                .andExpect(jsonPath("$.equipment").value(7.9));
    }
}
