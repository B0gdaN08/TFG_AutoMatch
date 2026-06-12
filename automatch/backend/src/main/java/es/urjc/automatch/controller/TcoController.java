package es.urjc.automatch.controller;

import es.urjc.automatch.dto.TcoResult;
import es.urjc.automatch.security.AuthUtils;
import es.urjc.automatch.service.TcoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tco")
@Tag(name = "TCO", description = "Cálculo del coste total de propiedad de un coche")
public class TcoController {

    private final TcoService tcoService;

    public TcoController(TcoService tcoService) {
        this.tcoService = tcoService;
    }

    @Operation(summary = "Calcular TCO", description = "Calcula el coste total de propiedad de un coche para un número de años y kilómetros anuales")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cálculo realizado correctamente"),
            @ApiResponse(responseCode = "404", description = "El coche no existe o no está visible para el usuario actual")
    })
    @GetMapping("/{carId}")
    public TcoResult calculate(@PathVariable Long carId,
                               @RequestParam(defaultValue = "5") int years,
                               @RequestParam(defaultValue = "15000") int km,
                               Authentication auth) {
        return tcoService.calculate(carId, years, km, AuthUtils.emailOrNull(auth), AuthUtils.isAdmin(auth));
    }
}
