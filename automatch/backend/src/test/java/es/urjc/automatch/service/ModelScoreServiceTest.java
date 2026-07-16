package es.urjc.automatch.service;

import es.urjc.automatch.model.ModelScore;
import es.urjc.automatch.repository.ModelScoreRepository;
import es.urjc.automatch.service.ModelScoreService.ResolvedScores;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ModelScoreServiceTest {

    @Mock
    private ModelScoreRepository repository;

    private ModelScoreService service;

    @BeforeEach
    void setUp() {
        service = new ModelScoreService(repository);
    }

    private ModelScore ficha(String brand, String model, int year,
                              double rel, double saf, double eq, double maint, double dep) {
        return new ModelScore(brand, model, year, rel, saf, eq, maint, dep);
    }

    @Test
    void coincidenciaExactaDeMarcaYModelo() {
        when(repository.findAll()).thenReturn(List.of(
                ficha("Toyota", "Corolla 140H", 2025, 9.5, 9.0, 7.5, 310, 0.40),
                ficha("Toyota", "Yaris Hybrid", 2025, 9.5, 8.5, 7.0, 290, 0.42)));

        ResolvedScores scores = service.resolve("Toyota", "Corolla 140H", 2025);

        assertThat(scores.reliability()).isEqualTo(9.5);
        assertThat(scores.safety()).isEqualTo(9.0);
        assertThat(scores.source()).isEqualTo("MODELO");
    }

    @Test
    void elMatchingIgnoraMayusculasYAcentos() {
        when(repository.findAll()).thenReturn(List.of(
                ficha("Seat", "León", 2025, 7.5, 8.5, 7.5, 400, 0.50)));

        ResolvedScores scores = service.resolve("SEAT", "leon", 2024);

        assertThat(scores.reliability()).isEqualTo(7.5);
        assertThat(scores.source()).isEqualTo("MODELO");
    }

    @Test
    void elModeloDelVendedorPuedeSerMasEspecificoQueLaFicha() {
        when(repository.findAll()).thenReturn(List.of(
                ficha("Volkswagen", "Golf", 2025, 7.5, 9.0, 8.0, 450, 0.46)));

        // "Golf GTI 2.0 TSI" contiene "Golf" -> encaja con la ficha generica
        ResolvedScores scores = service.resolve("Volkswagen", "Golf GTI 2.0 TSI", 2023);

        assertThat(scores.safety()).isEqualTo(9.0);
        assertThat(scores.source()).isEqualTo("MODELO");
    }

    @Test
    void conVariasFichasCandidatasGanaLaMasEspecificaYLuegoElAnioMasCercano() {
        when(repository.findAll()).thenReturn(List.of(
                ficha("Toyota", "Corolla", 2020, 9.0, 8.5, 7.0, 320, 0.45),
                ficha("Toyota", "Corolla 140H", 2025, 9.5, 9.0, 7.5, 310, 0.40)));

        // "Corolla 140H Touring" encaja con ambas; gana la ficha mas especifica
        ResolvedScores scores = service.resolve("Toyota", "Corolla 140H Touring", 2025);

        assertThat(scores.reliability()).isEqualTo(9.5);
    }

    @Test
    void sinModeloConocidoSeUsaLaMediaDeLaMarca() {
        when(repository.findAll()).thenReturn(List.of(
                ficha("Dacia", "Sandero", 2025, 7.5, 6.0, 5.5, 320, 0.45),
                ficha("Dacia", "Duster", 2025, 7.5, 6.5, 6.0, 350, 0.42),
                ficha("Tesla", "Model 3", 2025, 7.0, 9.5, 9.0, 250, 0.50)));

        ResolvedScores scores = service.resolve("Dacia", "Spring", 2025);

        assertThat(scores.reliability()).isEqualTo(7.5);
        assertThat(scores.safety()).isEqualTo(6.3);        // (6.0+6.5)/2 = 6.25 -> 6.3
        assertThat(scores.maintenancePerYear()).isEqualTo(335.0);
        assertThat(scores.source()).isEqualTo("MARCA");
    }

    @Test
    void sinMarcaConocidaSeUsaLaMediaGlobal() {
        when(repository.findAll()).thenReturn(List.of(
                ficha("Dacia", "Sandero", 2025, 7.0, 6.0, 5.0, 300, 0.40),
                ficha("Tesla", "Model 3", 2025, 9.0, 10.0, 9.0, 500, 0.60)));

        ResolvedScores scores = service.resolve("Lada", "Niva", 1995);

        assertThat(scores.reliability()).isEqualTo(8.0);
        assertThat(scores.safety()).isEqualTo(8.0);
        assertThat(scores.depreciation5y()).isEqualTo(0.5);
        assertThat(scores.source()).isEqualTo("GLOBAL");
    }

    @Test
    void conLaTablaVaciaDevuelveValoresNeutros() {
        when(repository.findAll()).thenReturn(List.of());

        ResolvedScores scores = service.resolve("Marca", "Modelo", 2025);

        assertThat(scores.reliability()).isBetween(1.0, 10.0);
        assertThat(scores.source()).isEqualTo("NEUTRO");
    }
}
