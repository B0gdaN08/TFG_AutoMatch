package es.urjc.automatch.service;

import es.urjc.automatch.dto.RecommendationRequest;
import es.urjc.automatch.dto.RecommendationResult;
import es.urjc.automatch.model.FuelType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Experimento comparativo reproducible: Suma Ponderada vs TOPSIS.
 *
 * Ejecuta ambos algoritmos sobre el catálogo seed (28 coches) para 5 perfiles de usuario
 * y registra correlación de Spearman, top-3 de cada método y cambio medio de posición.
 * También realiza un análisis de sensibilidad variando el peso de "price" para el perfil
 * equilibrado.
 *
 * Los resultados se guardan en docs/experimento-recomendador.md (directorio backend).
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ExperimentoComparativaTest {

    @Autowired
    private RecommendationService svc;

    // -------------------------------------------------------------------------
    // Definición de perfiles
    // -------------------------------------------------------------------------

    record Perfil(String nombre, Map<String, Double> weights, List<FuelType> fuels) {}

    private static Map<String, Double> pesos(double price, double efficiency, double reliability,
                                              double safety, double space, double performance,
                                              double equipment) {
        Map<String, Double> m = new LinkedHashMap<>();
        m.put("price",       price);
        m.put("efficiency",  efficiency);
        m.put("reliability", reliability);
        m.put("safety",      safety);
        m.put("space",       space);
        m.put("performance", performance);
        m.put("equipment",   equipment);
        return m;
    }

    private static final List<Perfil> PERFILES = List.of(
            new Perfil("Ahorrador",   pesos(5, 5, 2, 1, 1, 1, 1), List.of()),
            new Perfil("Familiar",    pesos(2, 2, 4, 5, 5, 1, 3), List.of()),
            new Perfil("Deportivo",   pesos(1, 1, 3, 3, 1, 5, 3), List.of()),
            new Perfil("Equilibrado", pesos(3, 3, 3, 3, 3, 3, 3), List.of()),
            new Perfil("Ecológico",   pesos(3, 5, 3, 3, 2, 2, 3), List.of(FuelType.ELECTRICO))
    );

    // -------------------------------------------------------------------------
    // Test principal
    // -------------------------------------------------------------------------

    @Test
    void ejecutaExperimentoComparativoYGeneraInforme() throws IOException {
        StringBuilder md = new StringBuilder();
        md.append("# Experimento comparativo: Suma Ponderada vs TOPSIS\n\n");
        md.append("Catálogo: 28 coches del mercado español (semilla DataLoader, perfil test H2).\n");
        md.append("Todos los cálculos son deterministas y reproducibles con `mvn verify`.\n\n");

        // ---- Tabla de resumen por perfil ----
        md.append("## 1. Rankings por perfil\n\n");

        for (Perfil perfil : PERFILES) {
            List<FuelType> fuels = perfil.fuels().isEmpty() ? null : perfil.fuels();
            List<RecommendationResult> ws = rankAll("WEIGHTED_SUM", perfil.weights(), fuels);
            List<RecommendationResult> tp = rankAll("TOPSIS",       perfil.weights(), fuels);

            md.append("### Perfil: ").append(perfil.nombre()).append("\n\n");
            md.append("Pesos: ").append(pesosStr(perfil.weights()));
            if (!perfil.fuels().isEmpty()) md.append(" · Filtro combustible: ").append(perfil.fuels());
            md.append("\n\n");

            // Top-3 de cada algoritmo
            md.append("| # | WSM — Suma Ponderada | Score | TOPSIS | Score |\n");
            md.append("|---|---------------------|-------|--------|-------|\n");
            for (int i = 0; i < 3; i++) {
                String wsName  = i < ws.size() ? ws.get(i).car().brand() + " " + ws.get(i).car().model() : "—";
                String wsScore = i < ws.size() ? String.valueOf(ws.get(i).score()) : "—";
                String tpName  = i < tp.size() ? tp.get(i).car().brand() + " " + tp.get(i).car().model() : "—";
                String tpScore = i < tp.size() ? String.valueOf(tp.get(i).score()) : "—";
                md.append("| ").append(i + 1).append(" | ").append(wsName).append(" | ").append(wsScore)
                  .append(" | ").append(tpName).append(" | ").append(tpScore).append(" |\n");
            }
            md.append("\n");

            // Estadísticas
            double rho       = spearman(ws, tp);
            double meanShift = meanPositionChange(ws, tp);
            md.append(String.format("**Correlación de Spearman:** ρ = %.3f  \n", rho));
            md.append(String.format("**Cambio medio de posición por coche:** %.1f puestos\n\n", meanShift));
        }

        // ---- Análisis de sensibilidad ----
        md.append("## 2. Análisis de sensibilidad — perfil Equilibrado, peso `price` de 0 a 5\n\n");
        md.append("El resto de pesos se mantienen a 3. Se registra el primer clasificado de cada método.\n\n");
        md.append("| Peso `price` | #1 WSM | #1 TOPSIS |\n");
        md.append("|:------------:|--------|----------|\n");

        for (int pw = 0; pw <= 5; pw++) {
            Map<String, Double> w = pesos(pw, 3, 3, 3, 3, 3, 3);
            List<RecommendationResult> ws = rankAll("WEIGHTED_SUM", w, null);
            List<RecommendationResult> tp = rankAll("TOPSIS",       w, null);
            String wsTop = ws.isEmpty() ? "—" : ws.get(0).car().brand() + " " + ws.get(0).car().model();
            String tpTop = tp.isEmpty() ? "—" : tp.get(0).car().brand() + " " + tp.get(0).car().model();
            md.append("| ").append(pw).append(" | ").append(wsTop).append(" | ").append(tpTop).append(" |\n");
        }
        md.append("\n");

        // ---- Guardar archivo y volcar a consola ----
        String contenido = md.toString();
        Path out = Path.of("docs/experimento-recomendador.md");
        Files.createDirectories(out.getParent());
        Files.writeString(out, contenido);

        System.out.println("\n=== EXPERIMENTO COMPARATIVO WSM vs TOPSIS ===\n");
        System.out.println(contenido);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private List<RecommendationResult> rankAll(String method, Map<String, Double> weights,
                                                List<FuelType> fuels) {
        RecommendationRequest req = new RecommendationRequest(null, null, fuels, null, weights, method);
        return svc.recommendAll(req, null, false);
    }

    /** Correlación de Spearman entre dos rankings completos con los mismos n candidatos. */
    private double spearman(List<RecommendationResult> a, List<RecommendationResult> b) {
        int n = a.size();
        if (n <= 1) return 1.0;

        Map<Long, Integer> rankA = new LinkedHashMap<>(), rankB = new LinkedHashMap<>();
        for (int i = 0; i < a.size(); i++) rankA.put(a.get(i).car().id(), i + 1);
        for (int i = 0; i < b.size(); i++) rankB.put(b.get(i).car().id(), i + 1);

        double sumDiSq = 0;
        for (Long id : rankA.keySet()) {
            int rb = rankB.getOrDefault(id, n + 1);
            int d  = rankA.get(id) - rb;
            sumDiSq += (double) d * d;
        }
        return 1.0 - (6.0 * sumDiSq) / ((double) n * (n * n - 1));
    }

    /** Cambio medio absoluto de posición por coche entre dos rankings. */
    private double meanPositionChange(List<RecommendationResult> a, List<RecommendationResult> b) {
        int n = a.size();
        if (n == 0) return 0;

        Map<Long, Integer> rankB = new LinkedHashMap<>();
        for (int i = 0; i < b.size(); i++) rankB.put(b.get(i).car().id(), i + 1);

        double sum = 0;
        for (int i = 0; i < a.size(); i++) {
            int rb = rankB.getOrDefault(a.get(i).car().id(), n + 1);
            sum += Math.abs((i + 1) - rb);
        }
        return sum / n;
    }

    private String pesosStr(Map<String, Double> w) {
        List<String> parts = new ArrayList<>();
        w.forEach((k, v) -> { if (v > 0) parts.add(k + "=" + v.intValue()); });
        return String.join(", ", parts);
    }
}
