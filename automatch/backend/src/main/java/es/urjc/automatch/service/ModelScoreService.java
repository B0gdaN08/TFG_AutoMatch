package es.urjc.automatch.service;

import es.urjc.automatch.model.ModelScore;
import es.urjc.automatch.repository.ModelScoreRepository;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Determina automaticamente las puntuaciones "expertas" de un coche
 * (fiabilidad, seguridad, equipamiento, mantenimiento y depreciacion)
 * a partir de su marca, modelo y año, usando la base de conocimiento
 * de la tabla model_scores (migracion V3).
 *
 * Estrategia de resolucion en cascada:
 *   1. MODELO — coincidencia de marca + modelo (exacta o por contencion,
 *      p. ej. "Corolla" encaja con la ficha "Corolla 140H"); si hay varias,
 *      gana la ficha mas especifica y despues la de año mas cercano.
 *   2. MARCA — media de todas las fichas de la marca.
 *   3. GLOBAL — media de toda la tabla (siempre hay 28+ fichas de V3).
 *
 * Punto de extension: para incorporar fuentes externas (Euro NCAP, informes
 * de fiabilidad...) basta con añadir filas a model_scores via una nueva
 * migracion o un proceso de importacion; la resolucion no cambia.
 */
@Service
public class ModelScoreService {

    /** Puntuaciones resueltas y la fuente usada (para el log de auditoria). */
    public record ResolvedScores(double reliability, double safety, double equipment,
                                  double maintenancePerYear, double depreciation5y, String source) {
    }

    /** Una marca con sus modelos conocidos, para los desplegables del formulario de venta. */
    public record BrandModels(String brand, List<String> models) {
    }

    private final ModelScoreRepository repository;

    public ModelScoreService(ModelScoreRepository repository) {
        this.repository = repository;
    }

    /** Marcas y modelos de la base de conocimiento, ordenados alfabeticamente. */
    public List<BrandModels> brandCatalog() {
        Map<String, List<String>> byBrand = repository.findAll().stream()
                .collect(Collectors.groupingBy(ModelScore::getBrand, TreeMap::new,
                        Collectors.mapping(ModelScore::getModel, Collectors.toList())));
        return byBrand.entrySet().stream()
                .map(e -> new BrandModels(e.getKey(), e.getValue().stream().distinct().sorted().toList()))
                .toList();
    }

    public ResolvedScores resolve(String brand, String model, int year) {
        List<ModelScore> all = repository.findAll();
        String normBrand = normalize(brand);
        String normModel = normalize(model);

        List<ModelScore> sameBrand = all.stream()
                .filter(m -> normalize(m.getBrand()).equals(normBrand))
                .toList();

        // 1a) Coincidencia EXACTA de modelo dentro de la marca (gana siempre:
        //     "Seal" debe resolver a la ficha "Seal" aunque exista "Seal U")
        List<ModelScore> exactMatches = sameBrand.stream()
                .filter(m -> normalize(m.getModel()).equals(normModel))
                .toList();
        if (!exactMatches.isEmpty()) {
            ModelScore best = exactMatches.stream()
                    .min(Comparator.comparingInt(m -> Math.abs(m.getYear() - year)))
                    .orElseThrow();
            return new ResolvedScores(best.getReliability(), best.getSafety(), best.getEquipment(),
                    best.getMaintenancePerYear(), best.getDepreciation5y(), "MODELO");
        }

        // 1b) Coincidencia por contencion ("Corolla" encaja con "Corolla 140H");
        //     entre varias gana la ficha mas especifica y despues el año mas cercano
        List<ModelScore> modelMatches = sameBrand.stream()
                .filter(m -> {
                    String ref = normalize(m.getModel());
                    return ref.contains(normModel) || normModel.contains(ref);
                })
                .toList();
        if (!modelMatches.isEmpty()) {
            ModelScore best = modelMatches.stream()
                    .max(Comparator
                            .comparingInt((ModelScore m) -> normalize(m.getModel()).length())
                            .thenComparing(m -> -Math.abs(m.getYear() - year)))
                    .orElseThrow();
            return new ResolvedScores(best.getReliability(), best.getSafety(), best.getEquipment(),
                    best.getMaintenancePerYear(), best.getDepreciation5y(), "MODELO");
        }

        // 2) Media de la marca
        if (!sameBrand.isEmpty()) {
            return average(sameBrand, "MARCA");
        }

        // 3) Media global de la base de conocimiento
        if (!all.isEmpty()) {
            return average(all, "GLOBAL");
        }

        // Tabla vacia (no deberia ocurrir: V3 la siembra): valores neutros
        return new ResolvedScores(6.0, 7.0, 6.5, 400, 0.50, "NEUTRO");
    }

    private ResolvedScores average(List<ModelScore> scores, String source) {
        return new ResolvedScores(
                round1(scores.stream().mapToDouble(ModelScore::getReliability).average().orElse(0)),
                round1(scores.stream().mapToDouble(ModelScore::getSafety).average().orElse(0)),
                round1(scores.stream().mapToDouble(ModelScore::getEquipment).average().orElse(0)),
                Math.round(scores.stream().mapToDouble(ModelScore::getMaintenancePerYear).average().orElse(0)),
                round2(scores.stream().mapToDouble(ModelScore::getDepreciation5y).average().orElse(0)),
                source);
    }

    private static double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    /**
     * Normalizacion para el matching: minusculas, sin acentos y sin signos ni
     * espacios. Asi "León" = "leon", "T Roc" = "T-Roc" = "troc" e "ID3" = "ID.3".
     */
    private static String normalize(String text) {
        if (text == null) return "";
        String lower = Normalizer.normalize(text.toLowerCase().trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return lower.replaceAll("[^a-z0-9]", "");
    }
}
