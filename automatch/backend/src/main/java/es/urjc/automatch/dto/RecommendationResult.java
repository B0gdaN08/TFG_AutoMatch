package es.urjc.automatch.dto;

import es.urjc.automatch.model.Car;

import java.util.Map;

/**
 * Un coche recomendado con su puntuacion global (0-100) y el detalle
 * normalizado por criterio (0-1) para poder explicar el resultado.
 */
public record RecommendationResult(
        Car car,
        double score,
        Map<String, Double> criteriaScores) {
}
