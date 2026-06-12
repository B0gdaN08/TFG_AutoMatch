package es.urjc.automatch.service;

import es.urjc.automatch.dto.RecommendationRequest;
import es.urjc.automatch.dto.RecommendationResult;
import es.urjc.automatch.model.Car;
import es.urjc.automatch.model.CarStatus;
import es.urjc.automatch.repository.CarRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToDoubleFunction;

/**
 * Recomendador multicriterio por suma ponderada (weighted sum model).
 *
 * 1. Se aplican los filtros duros (presupuesto, tipo, combustible, plazas).
 * 2. Cada criterio se normaliza min-max entre los coches candidatos (0..1).
 *    En los criterios de coste (precio, coste energetico) se invierte la escala.
 * 3. Puntuacion final = suma(peso_i * valor_normalizado_i) / suma(pesos) * 100.
 *
 * Mejora futura para la memoria: implementar tambien TOPSIS y comparar resultados.
 */
@Service
public class RecommendationService {

    public static final List<String> CRITERIA = List.of(
            "price", "efficiency", "reliability", "safety", "space", "performance", "equipment");

    private final CarRepository carRepository;
    private final CarService carService;

    public RecommendationService(CarRepository carRepository, CarService carService) {
        this.carRepository = carRepository;
        this.carService = carService;
    }

    public List<RecommendationResult> recommend(RecommendationRequest request, String email, boolean isAdmin) {
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

        // Valores normalizados (0..1) por criterio y coche
        Map<String, Map<Long, Double>> normalized = new LinkedHashMap<>();
        normalized.put("price", normalize(candidates, Car::getPrice, true));
        normalized.put("efficiency", normalize(candidates, CarCosts::energyCostPer100Km, true));
        normalized.put("reliability", normalize(candidates, Car::getReliability, false));
        normalized.put("safety", normalize(candidates, Car::getSafety, false));
        normalized.put("space", normalize(candidates, Car::getTrunkLiters, false));
        normalized.put("performance", normalize(candidates, Car::getPowerCv, false));
        normalized.put("equipment", normalize(candidates, Car::getEquipment, false));

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
            double score = Math.round(weighted / totalWeight * 1000.0) / 10.0; // 0..100, 1 decimal
            results.add(new RecommendationResult(carService.toResponse(car, email, isAdmin), score, criteriaScores));
        }

        results.sort(Comparator.comparingDouble(RecommendationResult::score).reversed());
        return results.size() > 10 ? results.subList(0, 10) : results;
    }

    /** Si no llegan pesos (o son todo ceros) se usa 1 para cada criterio. */
    private Map<String, Double> normalizeWeights(Map<String, Double> input) {
        Map<String, Double> weights = new LinkedHashMap<>();
        double sum = 0;
        for (String criterion : CRITERIA) {
            double value = input == null ? 0 : Math.max(0, input.getOrDefault(criterion, 0.0));
            weights.put(criterion, value);
            sum += value;
        }
        if (sum == 0) {
            CRITERIA.forEach(criterion -> weights.put(criterion, 1.0));
        }
        return weights;
    }

    /** Normalizacion min-max. Si el criterio es de coste, mejor cuanto mas bajo. */
    private Map<Long, Double> normalize(List<Car> cars, ToDoubleFunction<Car> extractor, boolean isCost) {
        double min = cars.stream().mapToDouble(extractor).min().orElse(0);
        double max = cars.stream().mapToDouble(extractor).max().orElse(0);
        Map<Long, Double> values = new LinkedHashMap<>();
        for (Car car : cars) {
            double normalizedValue;
            if (max == min) {
                normalizedValue = 1.0;
            } else if (isCost) {
                normalizedValue = (max - extractor.applyAsDouble(car)) / (max - min);
            } else {
                normalizedValue = (extractor.applyAsDouble(car) - min) / (max - min);
            }
            values.put(car.getId(), normalizedValue);
        }
        return values;
    }
}
