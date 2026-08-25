package es.urjc.automatch.service;

import es.urjc.automatch.dto.RecommendationRequest;
import es.urjc.automatch.dto.RecommendationResult;
import es.urjc.automatch.model.Car;
import es.urjc.automatch.model.CarStatus;
import es.urjc.automatch.repository.CarRepository;
import es.urjc.automatch.service.ranking.RankingAlgorithm;
import es.urjc.automatch.service.ranking.TopsisAlgorithm;
import es.urjc.automatch.service.ranking.WeightedSumAlgorithm;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Orquestador del recomendador multicriterio.
 *
 * Aplica los filtros duros (presupuesto, tipo, combustible, plazas), normaliza los pesos
 * y delega el ranking al algoritmo seleccionado por el cliente (campo "method"):
 *   - "WEIGHTED_SUM" (por defecto): Suma Ponderada con normalizacion min-max.
 *   - "TOPSIS": TOPSIS clasico de Hwang y Yoon (1981) con normalizacion vectorial.
 *
 * Ambos algoritmos implementan {@link RankingAlgorithm} y estan disponibles como instancias
 * internas del servicio sin necesidad de ser Spring beans independientes.
 */
@Service
public class RecommendationService {

    private final CarRepository carRepository;
    private final WeightedSumAlgorithm weightedSumAlgorithm;
    private final TopsisAlgorithm topsisAlgorithm;

    public RecommendationService(CarRepository carRepository, CarService carService) {
        this.carRepository = carRepository;
        this.weightedSumAlgorithm = new WeightedSumAlgorithm(carService);
        this.topsisAlgorithm = new TopsisAlgorithm(carService);
    }

    /** Devuelve el top-10 del ranking (endpoint publico de la API). */
    public List<RecommendationResult> recommend(RecommendationRequest request, String email, boolean isAdmin) {
        List<RecommendationResult> all = recommendAll(request, email, isAdmin);
        return all.size() > 10 ? all.subList(0, 10) : all;
    }

    /**
     * Devuelve el ranking completo sin limitar a 10 resultados.
     * Usado internamente por {@code recommend()} y por el experimento comparativo.
     */
    public List<RecommendationResult> recommendAll(RecommendationRequest request, String email, boolean isAdmin) {
        List<Car> candidates = carRepository.findByStatus(CarStatus.APPROVED).stream()
                .filter(car -> request.maxPrice() == null || car.getPrice() <= request.maxPrice())
                .filter(car -> request.types() == null || request.types().isEmpty()
                        || request.types().contains(car.getType()))
                .filter(car -> request.fuels() == null || request.fuels().isEmpty()
                        || request.fuels().contains(car.getFuel()))
                .filter(car -> request.minSeats() == null || car.getSeats() >= request.minSeats())
                .toList();

        if (candidates.isEmpty()) {
            return List.of();
        }

        Map<String, Double> weights = normalizeWeights(request.weights());
        return selectAlgorithm(request.method()).rank(candidates, weights, email, isAdmin);
    }

    private RankingAlgorithm selectAlgorithm(String method) {
        return "TOPSIS".equalsIgnoreCase(method) ? topsisAlgorithm : weightedSumAlgorithm;
    }

    /** Si no llegan pesos (o son todo ceros) se usa 1 para cada criterio. */
    private Map<String, Double> normalizeWeights(Map<String, Double> input) {
        Map<String, Double> weights = new LinkedHashMap<>();
        double sum = 0;
        for (String criterion : RankingAlgorithm.CRITERIA) {
            double value = input == null ? 0 : Math.max(0, input.getOrDefault(criterion, 0.0));
            weights.put(criterion, value);
            sum += value;
        }
        if (sum == 0) {
            RankingAlgorithm.CRITERIA.forEach(criterion -> weights.put(criterion, 1.0));
        }
        return weights;
    }
}
