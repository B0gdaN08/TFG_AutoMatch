package es.urjc.automatch.service;

import es.urjc.automatch.dto.CarResponse;
import es.urjc.automatch.dto.RecommendationRequest;
import es.urjc.automatch.dto.RecommendationResult;
import es.urjc.automatch.model.Car;
import es.urjc.automatch.model.CarStatus;
import es.urjc.automatch.model.CarType;
import es.urjc.automatch.model.FuelType;
import es.urjc.automatch.repository.CarRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private CarRepository carRepository;

    @Mock
    private CarService carService;

    private RecommendationService recommendationService;

    private long nextId;

    @BeforeEach
    void setUp() {
        recommendationService = new RecommendationService(carRepository, carService);
        nextId = 1;
        lenient().when(carService.toResponse(any(Car.class), any(), anyBoolean()))
                .thenAnswer(invocation -> CarResponse.from(invocation.getArgument(0), null, false));
    }

    private Car baseCar() {
        Car car = new Car("Marca", "Modelo", 2025, 20000, CarType.COMPACTO, FuelType.GASOLINA,
                "C", 5.0, 120, 400, 5, 7.0, 7.0, 7.0, 300, 0.5, "Coche de prueba");
        car.setId(nextId++);
        car.setStatus(CarStatus.APPROVED);
        return car;
    }

    private RecommendationRequest request(Double maxPrice, List<CarType> types, List<FuelType> fuels,
                                            Integer minSeats, Map<String, Double> weights) {
        return new RecommendationRequest(maxPrice, types, fuels, minSeats, weights, null);
    }

    private double criteriaScoreFor(List<RecommendationResult> resultados, Long carId, String criterio) {
        return resultados.stream()
                .filter(r -> r.car().id().equals(carId))
                .findFirst().orElseThrow()
                .criteriaScores().get(criterio);
    }

    private double scoreFor(List<RecommendationResult> resultados, Long carId) {
        return resultados.stream()
                .filter(r -> r.car().id().equals(carId))
                .findFirst().orElseThrow()
                .score();
    }

    private List<Long> idsOf(List<RecommendationResult> resultados) {
        return resultados.stream().map(r -> r.car().id()).toList();
    }

    @Test
    void laNormalizacionMinMaxEsCorrectaParaUnCriterioNoDeCoste() {
        Car coche4 = baseCar();
        coche4.setReliability(4);
        Car coche6 = baseCar();
        coche6.setReliability(6);
        Car coche10 = baseCar();
        coche10.setReliability(10);

        when(carRepository.findByStatus(CarStatus.APPROVED)).thenReturn(List.of(coche4, coche6, coche10));

        List<RecommendationResult> resultados = recommendationService.recommend(
                request(null, null, null, null, null), null, false);

        assertThat(criteriaScoreFor(resultados, coche4.getId(), "reliability")).isEqualTo(0.0);
        assertThat(criteriaScoreFor(resultados, coche6.getId(), "reliability")).isEqualTo(0.33);
        assertThat(criteriaScoreFor(resultados, coche10.getId(), "reliability")).isEqualTo(1.0);
    }

    @Test
    void elCriterioDePrecioSeInvierteElCocheMasBaratoPuntuaMasAltoEnPrice() {
        Car cocheBarato = baseCar();
        cocheBarato.setPrice(10000);
        Car cocheCaro = baseCar();
        cocheCaro.setPrice(30000);

        when(carRepository.findByStatus(CarStatus.APPROVED)).thenReturn(List.of(cocheBarato, cocheCaro));

        List<RecommendationResult> resultados = recommendationService.recommend(
                request(null, null, null, null, null), null, false);

        assertThat(criteriaScoreFor(resultados, cocheBarato.getId(), "price")).isEqualTo(1.0);
        assertThat(criteriaScoreFor(resultados, cocheCaro.getId(), "price")).isEqualTo(0.0);
    }

    @Test
    void siTodosLosPesosSonCeroSeUsanPesosUniformes() {
        Car coche1 = baseCar();
        coche1.setReliability(4);
        coche1.setPrice(15000);
        Car coche2 = baseCar();
        coche2.setReliability(8);
        coche2.setPrice(25000);

        when(carRepository.findByStatus(CarStatus.APPROVED)).thenReturn(List.of(coche1, coche2));

        Map<String, Double> pesosCero = new HashMap<>();
        Map<String, Double> pesosUniformes = new HashMap<>();
        for (String criterio : RecommendationService.CRITERIA) {
            pesosCero.put(criterio, 0.0);
            pesosUniformes.put(criterio, 1.0);
        }

        List<RecommendationResult> conPesosCero = recommendationService.recommend(
                request(null, null, null, null, pesosCero), null, false);
        List<RecommendationResult> conPesosUniformes = recommendationService.recommend(
                request(null, null, null, null, pesosUniformes), null, false);

        assertThat(scoreFor(conPesosCero, coche1.getId())).isEqualTo(scoreFor(conPesosUniformes, coche1.getId()));
        assertThat(scoreFor(conPesosCero, coche2.getId())).isEqualTo(scoreFor(conPesosUniformes, coche2.getId()));
    }

    @Test
    void elFiltroDePrecioMaximoExcluyeCochesPorEncimaDelLimite() {
        Car cocheBarato = baseCar();
        cocheBarato.setPrice(15000);
        Car cocheCaro = baseCar();
        cocheCaro.setPrice(35000);

        when(carRepository.findByStatus(CarStatus.APPROVED)).thenReturn(List.of(cocheBarato, cocheCaro));

        List<RecommendationResult> resultados = recommendationService.recommend(
                request(20000.0, null, null, null, null), null, false);

        assertThat(idsOf(resultados)).containsExactly(cocheBarato.getId());
    }

    @Test
    void elFiltroDeTipoExcluyeCochesDeOtroTipo() {
        Car suv = baseCar();
        suv.setType(CarType.SUV);
        Car compacto = baseCar();
        compacto.setType(CarType.COMPACTO);

        when(carRepository.findByStatus(CarStatus.APPROVED)).thenReturn(List.of(suv, compacto));

        List<RecommendationResult> resultados = recommendationService.recommend(
                request(null, List.of(CarType.SUV), null, null, null), null, false);

        assertThat(idsOf(resultados)).containsExactly(suv.getId());
    }

    @Test
    void elFiltroDeCombustibleExcluyeCochesDeOtroCombustible() {
        Car electrico = baseCar();
        electrico.setFuel(FuelType.ELECTRICO);
        Car gasolina = baseCar();
        gasolina.setFuel(FuelType.GASOLINA);

        when(carRepository.findByStatus(CarStatus.APPROVED)).thenReturn(List.of(electrico, gasolina));

        List<RecommendationResult> resultados = recommendationService.recommend(
                request(null, null, List.of(FuelType.ELECTRICO), null, null), null, false);

        assertThat(idsOf(resultados)).containsExactly(electrico.getId());
    }

    @Test
    void elFiltroDePlazasMinimasExcluyeCochesConMenosPlazas() {
        Car cincoPlazas = baseCar();
        cincoPlazas.setSeats(5);
        Car sietePlazas = baseCar();
        sietePlazas.setSeats(7);

        when(carRepository.findByStatus(CarStatus.APPROVED)).thenReturn(List.of(cincoPlazas, sietePlazas));

        List<RecommendationResult> resultados = recommendationService.recommend(
                request(null, null, null, 7, null), null, false);

        assertThat(idsOf(resultados)).containsExactly(sietePlazas.getId());
    }

    @Test
    void soloEntranCochesAprobadosEnLaRecomendacion() {
        when(carRepository.findByStatus(CarStatus.APPROVED)).thenReturn(List.of());

        List<RecommendationResult> resultados = recommendationService.recommend(
                request(null, null, null, null, null), null, false);

        assertThat(resultados).isEmpty();
        verify(carRepository).findByStatus(CarStatus.APPROVED);
        verify(carRepository, never()).findByStatus(CarStatus.PENDING);
        verify(carRepository, never()).findByStatus(CarStatus.REJECTED);
    }

    @Test
    void devuelveComoMaximo10ResultadosOrdenadosPorPuntuacionDescendente() {
        List<Car> coches = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            Car car = baseCar();
            car.setReliability(i);
            coches.add(car);
        }

        when(carRepository.findByStatus(CarStatus.APPROVED)).thenReturn(coches);

        List<RecommendationResult> resultados = recommendationService.recommend(
                request(null, null, null, null, null), null, false);

        assertThat(resultados).hasSize(10);
        for (int i = 0; i < resultados.size() - 1; i++) {
            assertThat(resultados.get(i).score()).isGreaterThanOrEqualTo(resultados.get(i + 1).score());
        }

        List<Long> idsDevueltos = idsOf(resultados);
        assertThat(idsDevueltos).doesNotContain(coches.get(0).getId(), coches.get(1).getId());
    }
}
