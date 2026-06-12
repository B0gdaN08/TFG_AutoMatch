package es.urjc.automatch.dto;

import java.util.Map;

/**
 * Resultado del calculo de Coste Total de Propiedad (TCO).
 * breakdown: coste total por categoria durante todo el periodo.
 */
public record TcoResult(
        Long carId,
        String carName,
        int years,
        int kmPerYear,
        Map<String, Double> breakdown,
        double totalCost,
        double costPerYear,
        double costPerKm) {
}
