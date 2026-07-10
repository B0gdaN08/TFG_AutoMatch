package es.urjc.automatch.service.ranking;

import es.urjc.automatch.dto.RecommendationResult;
import es.urjc.automatch.model.Car;
import es.urjc.automatch.service.CarService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Algoritmo de Suma Ponderada (Weighted Sum Model, WSM).
 *
 * Cada criterio se normaliza min-max en el rango [0..1] invirtiendo la escala
 * en los criterios de coste (price, efficiency). La puntuacion final es:
 *   score = sum(w_j * norm_j) / sum(w_j) * 100
 */
public class WeightedSumAlgorithm implements RankingAlgorithm {

    private final CarService carService;

    public WeightedSumAlgorithm(CarService carService) {
        this.carService = carService;
    }

    @Override
    public List<RecommendationResult> rank(List<Car> candidates, Map<String, Double> weights,
                                            String email, boolean isAdmin) {
        Map<String, Map<Long, Double>> normalized = new LinkedHashMap<>();
        for (String criterion : CRITERIA) {
            normalized.put(criterion, AlgoUtils.normalizeMinMax(candidates, criterion));
        }

        double totalWeight = weights.values().stream().mapToDouble(Double::doubleValue).sum();

        List<RecommendationResult> results = new ArrayList<>();
        for (Car car : candidates) {
            Map<String, Double> criteriaScores = new LinkedHashMap<>();
            double weighted = 0;
            for (String criterion : CRITERIA) {
                double value = normalized.get(criterion).get(car.getId());
                criteriaScores.put(criterion, Math.round(value * 100.0) / 100.0);
                weighted += weights.get(criterion) * value;
            }
            double score = Math.round(weighted / totalWeight * 1000.0) / 10.0;
            results.add(new RecommendationResult(
                    carService.toResponse(car, email, isAdmin), score, criteriaScores));
        }

        results.sort(Comparator.comparingDouble(RecommendationResult::score).reversed());
        return results;
    }
}
