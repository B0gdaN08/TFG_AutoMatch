package es.urjc.automatch.dto;

import es.urjc.automatch.model.CarType;
import es.urjc.automatch.model.FuelType;

import java.util.List;
import java.util.Map;

/**
 * Peticion del recomendador multicriterio.
 * weights: importancia 0-5 para cada criterio:
 *   price, efficiency, reliability, safety, space, performance, equipment
 * Los filtros (maxPrice, types, fuels, minSeats) son restricciones duras.
 */
public record RecommendationRequest(
        Double maxPrice,
        List<CarType> types,
        List<FuelType> fuels,
        Integer minSeats,
        Map<String, Double> weights) {
}
