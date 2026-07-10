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
 * Algoritmo TOPSIS (Technique for Order Preference by Similarity to Ideal Solution)
 * segun la formulacion clasica de Hwang y Yoon (1981).
 *
 * Los pasos son:
 *
 *   1. Matriz de decision X: x_ij = valor bruto del criterio j para el coche i.
 *
 *   2. Normalizacion vectorial (Hwang & Yoon, ec. 3):
 *        r_ij = x_ij / ||x_j||,  donde ||x_j|| = sqrt(sum_i x_ij^2).
 *      Si la norma de una columna es cero (todos los valores son 0), r_ij = 0.
 *
 *   3. Matriz ponderada (Hwang & Yoon, ec. 4):
 *        v_ij = w_j * r_ij,
 *      con pesos w_j normalizados a suma 1.
 *
 *   4. Soluciones ideales (Hwang & Yoon, ec. 5):
 *        A+ = { max_i(v_ij) si beneficio, min_i(v_ij) si coste }
 *        A- = { min_i(v_ij) si beneficio, max_i(v_ij) si coste }
 *      Los criterios de coste (price, efficiency) tienen su ideal en el minimo.
 *
 *   5. Distancias euclídeas (Hwang & Yoon, ec. 6-7):
 *        D+_i = sqrt(sum_j (v_ij - A+_j)^2)
 *        D-_i = sqrt(sum_j (v_ij - A-_j)^2)
 *
 *   6. Coeficiente de proximidad (Hwang & Yoon, ec. 8):
 *        Ci = D-_i / (D+_i + D-_i),  con Ci in [0, 1].
 *      Mayor Ci -> mejor alternativa.
 *      Caso borde D+ + D- = 0 (candidato unico o columnas constantes): Ci = 1.0.
 *
 *   7. Puntuacion: score = round(Ci * 100, 1 decimal).
 *
 * Los criteriaScores que se devuelven son los valores normalizados min-max (no los
 * vectoriales de TOPSIS), de modo que el frontend muestra las mini-barras de detalle
 * con la misma escala que en el modo Suma Ponderada.
 *
 * Casos borde manejados:
 *   - Un solo candidato: D+ = D- = 0 => Ci = 1.0 => score = 100.0.
 *   - Columna con norma cero: r_ij = 0 para todos.
 *   - D+ + D- = 0 por cualquier otro motivo: Ci = 1.0.
 */
public class TopsisAlgorithm implements RankingAlgorithm {

    private final CarService carService;

    public TopsisAlgorithm(CarService carService) {
        this.carService = carService;
    }

    @Override
    public List<RecommendationResult> rank(List<Car> candidates, Map<String, Double> weights,
                                            String email, boolean isAdmin) {
        int n = candidates.size();
        int m = CRITERIA.size();

        // Paso 1 — Matriz de decision con valores brutos
        double[][] x = new double[n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                x[i][j] = AlgoUtils.rawValue(candidates.get(i), CRITERIA.get(j));
            }
        }

        // Paso 2 — Normalizacion vectorial: r_ij = x_ij / ||x_j||
        double[][] r = new double[n][m];
        for (int j = 0; j < m; j++) {
            double sumSq = 0;
            for (int i = 0; i < n; i++) sumSq += x[i][j] * x[i][j];
            double norm = Math.sqrt(sumSq);
            for (int i = 0; i < n; i++) r[i][j] = (norm == 0) ? 0 : x[i][j] / norm;
        }

        // Paso 3 — Matriz ponderada: v_ij = w_j * r_ij  (pesos normalizados a suma 1)
        double totalWeight = weights.values().stream().mapToDouble(Double::doubleValue).sum();
        double[][] v = new double[n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                double wj = weights.get(CRITERIA.get(j)) / totalWeight;
                v[i][j] = wj * r[i][j];
            }
        }

        // Paso 4 — Soluciones ideales A+ y A-
        double[] aPlus  = new double[m];
        double[] aMinus = new double[m];
        for (int j = 0; j < m; j++) {
            boolean isCost = COST_CRITERIA.contains(CRITERIA.get(j));
            double min = Double.MAX_VALUE, max = -Double.MAX_VALUE;
            for (int i = 0; i < n; i++) {
                if (v[i][j] < min) min = v[i][j];
                if (v[i][j] > max) max = v[i][j];
            }
            // Criterio de coste: el ideal positivo es el MINIMO (menos gasto)
            aPlus[j]  = isCost ? min : max;
            aMinus[j] = isCost ? max : min;
        }

        // Paso 5 — Distancias euclídeas a A+ y A-
        double[] dPlus  = new double[n];
        double[] dMinus = new double[n];
        for (int i = 0; i < n; i++) {
            double sumP = 0, sumM = 0;
            for (int j = 0; j < m; j++) {
                sumP += Math.pow(v[i][j] - aPlus[j],  2);
                sumM += Math.pow(v[i][j] - aMinus[j], 2);
            }
            dPlus[i]  = Math.sqrt(sumP);
            dMinus[i] = Math.sqrt(sumM);
        }

        // Paso 6-7 — Coeficiente Ci y puntuacion final; criteriaScores en min-max para el frontend
        // Pre-calculo de normalizacion min-max para devolver las barras de detalle
        Map<String, Map<Long, Double>> minMaxNorm = new LinkedHashMap<>();
        for (String criterion : CRITERIA) {
            minMaxNorm.put(criterion, AlgoUtils.normalizeMinMax(candidates, criterion));
        }

        List<RecommendationResult> results = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            Car car = candidates.get(i);
            double denom = dPlus[i] + dMinus[i];
            double ci = (denom == 0) ? 1.0 : dMinus[i] / denom;
            double score = Math.round(ci * 1000.0) / 10.0;

            Map<String, Double> criteriaScores = new LinkedHashMap<>();
            for (String criterion : CRITERIA) {
                double mm = minMaxNorm.get(criterion).get(car.getId());
                criteriaScores.put(criterion, Math.round(mm * 100.0) / 100.0);
            }

            results.add(new RecommendationResult(
                    carService.toResponse(car, email, isAdmin), score, criteriaScores));
        }

        results.sort(Comparator.comparingDouble(RecommendationResult::score).reversed());
        return results;
    }
}
