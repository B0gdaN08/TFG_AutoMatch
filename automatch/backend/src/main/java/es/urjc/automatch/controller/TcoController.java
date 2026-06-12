package es.urjc.automatch.controller;

import es.urjc.automatch.dto.TcoResult;
import es.urjc.automatch.security.AuthUtils;
import es.urjc.automatch.service.TcoService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tco")
public class TcoController {

    private final TcoService tcoService;

    public TcoController(TcoService tcoService) {
        this.tcoService = tcoService;
    }

    @GetMapping("/{carId}")
    public TcoResult calculate(@PathVariable Long carId,
                               @RequestParam(defaultValue = "5") int years,
                               @RequestParam(defaultValue = "15000") int km,
                               Authentication auth) {
        return tcoService.calculate(carId, years, km, AuthUtils.emailOrNull(auth), AuthUtils.isAdmin(auth));
    }
}
