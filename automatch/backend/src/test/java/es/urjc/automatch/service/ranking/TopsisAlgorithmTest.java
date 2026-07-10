package es.urjc.automatch.service.ranking;

import es.urjc.automatch.dto.CarResponse;
import es.urjc.automatch.dto.RecommendationResult;
import es.urjc.automatch.model.Car;
import es.urjc.automatch.model.CarStatus;
import es.urjc.automatch.model.CarType;
import es.urjc.automatch.model.FuelType;
import es.urjc.automatch.service.CarService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.lenient;

/**
 * Verificacion matematica de TopsisAlgorithm contra un ejemplo calculado a mano
 * siguiendo la formulacion de Hwang y Yoon (1981).
 *
 * Ejemplo de referencia (3 coches, 2 criterios con peso: price [coste] y reliability [beneficio],
 * pesos brutos 1 y 1 -> normalizados a 0.5 y 0.5):
 *
 *   Coche | price  | reliability
 *   ------|--------|------------
 *   A     | 10000  | 4
 *   B     | 20000  | 6
 *   C     | 30000  | 10
 *
 * Paso 2 (normalizacion vectorial):
 *   ||price|| = 10000*sqrt(1+4+9) = 10000*sqrt(14)   -> r_price = (0.26726, 0.53452, 0.80178)
 *   ||rel||   = sqrt(16+36+100)   = sqrt(152)        -> r_rel   = (0.32444, 0.48666, 0.81111)
 *
 * Paso 3 (v = 0.5 * r):
 *   v_price = (0.13363, 0.26726, 0.40089)
 *   v_rel   = (0.16222, 0.24333, 0.40555)
 *
 * Paso 4: price es coste -> A+_price = 0.13363 (min), A-_price = 0.40089 (max)
 *         reliability es beneficio -> A+_rel = 0.40555, A-_rel = 0.16222
 *
 * Paso 5-6:
 *   A: D+ = 0.24333, D- = 0.26726 -> Ci = 0.26726/0.51059 = 0.52343 -> score 52.3
 *   B: D+ = 0.21017, D- = 0.15632 -> Ci = 0.15632/0.36649 = 0.42653 -> score 42.7
 *   C: D+ = 0.26726, D- = 0.24333 -> Ci = 0.24333/0.51059 = 0.47657 -> score 47.7
 *
 * Ranking esperado: A (52.3) > C (47.7) > B (42.7).
 * Propiedad de control: A y C son los extremos de ambos criterios, por lo que
 * comparten denominador y Ci(A) + Ci(C) = 1 -> sus scores suman 100.0.
 */
@ExtendWith(MockitoExtension.class)
class TopsisAlgorithmTest {

    @Mock
    private CarService carService;

    private TopsisAlgorithm topsis;

    private long nextId;

    @BeforeEach
    void setUp() {
        topsis = new TopsisAlgorithm(carService);
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

    /** Pesos brutos para los 7 criterios (el algoritmo los normaliza a suma 1). */
    private Map<String, Double> pesos(double price, double efficiency, double reliability,
                                       double safety, double space, double performance,
                                       double equipment) {
        Map<String, Double> w = new LinkedHashMap<>();
        w.put("price", price);
        w.put("efficiency", efficiency);
        w.put("reliability", reliability);
        w.put("safety", safety);
        w.put("space", space);
        w.put("performance", performance);
        w.put("equipment", equipment);
        return w;
    }

    private double scoreFor(List<RecommendationResult> resultados, Long carId) {
        return resultados.stream()
                .filter(r -> r.car().id().equals(carId))
                .findFirst().orElseThrow()
                .score();
    }

    @Test
    void reproduceElEjemploDeReferenciaCalculadoAMano() {
        Car a = baseCar();
        a.setPrice(10000);
        a.setReliability(4);
        Car b = baseCar();
        b.setPrice(20000);
        b.setReliability(6);
        Car c = baseCar();
        c.setPrice(30000);
        c.setReliability(10);

        List<RecommendationResult> resultados = topsis.rank(
                List.of(a, b, c), pesos(1, 0, 1, 0, 0, 0, 0), null, false);

        assertThat(scoreFor(resultados, a.getId())).isEqualTo(52.3);
        assertThat(scoreFor(resultados, b.getId())).isEqualTo(42.7);
        assertThat(scoreFor(resultados, c.getId())).isEqualTo(47.7);

        // Orden descendente por proximidad al ideal: A > C > B
        assertThat(resultados.stream().map(r -> r.car().id()).toList())
                .containsExactly(a.getId(), c.getId(), b.getId());

        // A y C son extremos de ambos criterios: comparten denominador y Ci(A)+Ci(C)=1
        assertThat(scoreFor(resultados, a.getId()) + scoreFor(resultados, c.getId())).isEqualTo(100.0);
    }

    @Test
    void unSoloCandidatoObtieneElScoreMaximo() {
        Car unico = baseCar();

        List<RecommendationResult> resultados = topsis.rank(
                List.of(unico), pesos(1, 1, 1, 1, 1, 1, 1), null, false);

        // D+ = D- = 0 -> Ci = 1.0 por convencion -> score 100
        assertThat(resultados).hasSize(1);
        assertThat(resultados.get(0).score()).isEqualTo(100.0);
    }

    @Test
    void columnasConstantesNoProvocanDivisionPorCero() {
        // Dos coches identicos en todos los criterios: A+ = A- en todas las columnas
        Car coche1 = baseCar();
        Car coche2 = baseCar();

        List<RecommendationResult> resultados = topsis.rank(
                List.of(coche1, coche2), pesos(1, 1, 1, 1, 1, 1, 1), null, false);

        assertThat(resultados).hasSize(2);
        assertThat(resultados).allSatisfy(r -> assertThat(r.score()).isEqualTo(100.0));
    }

    @Test
    void elCriterioDeCosteTieneSuIdealEnElMinimo() {
        Car barato = baseCar();
        barato.setPrice(10000);
        Car caro = baseCar();
        caro.setPrice(30000);

        // Solo pesa el precio: el barato coincide con A+ (Ci=1) y el caro con A- (Ci=0)
        List<RecommendationResult> resultados = topsis.rank(
                List.of(barato, caro), pesos(1, 0, 0, 0, 0, 0, 0), null, false);

        assertThat(scoreFor(resultados, barato.getId())).isEqualTo(100.0);
        assertThat(scoreFor(resultados, caro.getId())).isEqualTo(0.0);
    }

    @Test
    void unaColumnaConNormaCeroNoRompeElCalculo() {
        // reliability = 0 en todos: la norma de la columna es 0 y r_ij = 0 por convencion
        Car barato = baseCar();
        barato.setPrice(10000);
        barato.setReliability(0);
        Car caro = baseCar();
        caro.setPrice(30000);
        caro.setReliability(0);

        List<RecommendationResult> resultados = topsis.rank(
                List.of(barato, caro), pesos(1, 0, 1, 0, 0, 0, 0), null, false);

        // La columna de norma cero no aporta distancia: decide el precio
        assertThat(scoreFor(resultados, barato.getId())).isEqualTo(100.0);
        assertThat(scoreFor(resultados, caro.getId())).isEqualTo(0.0);
    }

    @Test
    void losCriteriaScoresSonMinMaxParaLasBarrasDelFrontend() {
        Car barato = baseCar();
        barato.setPrice(10000);
        Car medio = baseCar();
        medio.setPrice(20000);
        Car caro = baseCar();
        caro.setPrice(30000);

        List<RecommendationResult> resultados = topsis.rank(
                List.of(barato, medio, caro), pesos(1, 0, 0, 0, 0, 0, 0), null, false);

        // Aunque el ranking es TOPSIS, las barras de detalle usan min-max (coste invertido)
        assertThat(criteriaScoreFor(resultados, barato.getId())).isEqualTo(1.0);
        assertThat(criteriaScoreFor(resultados, medio.getId())).isEqualTo(0.5);
        assertThat(criteriaScoreFor(resultados, caro.getId())).isEqualTo(0.0);
    }

    private double criteriaScoreFor(List<RecommendationResult> resultados, Long carId) {
        return resultados.stream()
                .filter(r -> r.car().id().equals(carId))
                .findFirst().orElseThrow()
                .criteriaScores().get("price");
    }
}
