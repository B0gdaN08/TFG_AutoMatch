package es.urjc.automatch.service.ranking;

import es.urjc.automatch.model.Car;
import es.urjc.automatch.service.CarCosts;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Utilidades estaticas compartidas por los algoritmos de ranking. */
final class AlgoUtils {

    private AlgoUtils() {
    }

    /** Devuelve el valor bruto del criterio indicado para el coche dado. */
    static double rawValue(Car car, String criterion) {
        return switch (criterion) {
            case "price"       -> car.getPrice();
            case "efficiency"  -> CarCosts.energyCostPer100Km(car);
            case "reliability" -> car.getReliability();
            case "safety"      -> car.getSafety();
            case "space"       -> (double) car.getTrunkLiters();
            case "performance" -> (double) car.getPowerCv();
            case "equipment"   -> car.getEquipment();
            default -> throw new IllegalArgumentException("Criterio desconocido: " + criterion);
        };
    }

    /**
     * Normalizacion min-max para un criterio sobre la lista de candidatos.
     * En criterios de coste invierte la escala (menor valor -> 1.0).
     * Si todos los coches tienen el mismo valor, todos reciben 1.0.
     */
    static Map<Long, Double> normalizeMinMax(List<Car> cars, String criterion) {
        boolean isCost = RankingAlgorithm.COST_CRITERIA.contains(criterion);
        double min = cars.stream().mapToDouble(c -> rawValue(c, criterion)).min().orElse(0);
        double max = cars.stream().mapToDouble(c -> rawValue(c, criterion)).max().orElse(0);
        Map<Long, Double> result = new LinkedHashMap<>();
        for (Car car : cars) {
            double raw = rawValue(car, criterion);
            double norm;
            if (max == min) {
                norm = 1.0;
            } else if (isCost) {
                norm = (max - raw) / (max - min);
            } else {
                norm = (raw - min) / (max - min);
            }
            result.put(car.getId(), norm);
        }
        return result;
    }
}
