package es.urjc.automatch.controller;

import es.urjc.automatch.dto.RecommendationRequest;
import es.urjc.automatch.dto.RecommendationResult;
import es.urjc.automatch.security.AuthUtils;
import es.urjc.automatch.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@Tag(name = "Recomendaciones", description = "Recomendador multicriterio de coches según preferencias y filtros")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @Operation(summary = "Recomendar coches", description = "Devuelve los coches del catálogo puntuados y ordenados según los pesos y filtros indicados")
    @ApiResponse(responseCode = "200", description = "Lista de coches recomendados con su puntuación")
    @PostMapping
    public List<RecommendationResult> recommend(@RequestBody RecommendationRequest request, Authentication auth) {
        return recommendationService.recommend(request, AuthUtils.emailOrNull(auth), AuthUtils.isAdmin(auth));
    }
}
