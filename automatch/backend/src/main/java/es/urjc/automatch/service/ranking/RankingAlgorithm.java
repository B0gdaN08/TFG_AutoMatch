package es.urjc.automatch.service.ranking;

import es.urjc.automatch.dto.RecommendationResult;
import es.urjc.automatch.model.Car;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Contrato comun para los algoritmos de ordenacion multicriterio del recomendador.
 *
 * Recibe los candidatos ya filtrados (garantizados no vacios) y los pesos
 * normalizados (suma > 0, valores >= 0) y devuelve la lista completa ordenada
 * de mayor a menor puntuacion, con score en [0..100] con 1 decimal.
 */
public interface RankingAlgorithm {

    /** Identificadores de los criterios, en el orden canonico del recomendador. */
    List<String> CRITERIA = List.of(
            "price", "efficiency", "reliability", "safety", "space", "performance", "equipment");

    /** Criterios cuyo ideal es el valor minimo (criterios de coste). */
    Set<String> COST_CRITERIA = Set.of("price", "efficiency");

    /**
     * Ordena los candidatos de mayor a menor puntuacion.
     *
     * @param candidates coches candidatos (ya filtrados, lista no vacia)
     * @param weights    pesos por criterio (suma > 0, valores >= 0)
     * @param email      email del usuario autenticado (puede ser null)
     * @param isAdmin    true si el usuario tiene rol ADMIN
     * @return lista completa ordenada, puntuacion 0-100 con 1 decimal
     */
    List<RecommendationResult> rank(List<Car> candidates, Map<String, Double> weights,
                                     String email, boolean isAdmin);
}
