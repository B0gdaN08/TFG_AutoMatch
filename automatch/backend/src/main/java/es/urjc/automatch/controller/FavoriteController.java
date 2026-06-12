package es.urjc.automatch.controller;

import es.urjc.automatch.dto.CarResponse;
import es.urjc.automatch.security.AuthUtils;
import es.urjc.automatch.service.CarService;
import es.urjc.automatch.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
@Tag(name = "Favoritos", description = "Gestión de los coches favoritos del usuario autenticado")
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final CarService carService;

    public FavoriteController(FavoriteService favoriteService, CarService carService) {
        this.favoriteService = favoriteService;
        this.carService = carService;
    }

    @Operation(summary = "Listar favoritos", description = "Devuelve los coches marcados como favoritos por el usuario autenticado")
    @GetMapping
    public List<CarResponse> list(Authentication authentication) {
        var cars = favoriteService.list(authentication.getName());
        return carService.toResponseList(cars, authentication.getName(), AuthUtils.isAdmin(authentication));
    }

    @Operation(summary = "Añadir favorito", description = "Marca un coche como favorito del usuario autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Coche añadido a favoritos"),
            @ApiResponse(responseCode = "404", description = "El coche no existe")
    })
    @PostMapping("/{carId}")
    @ResponseStatus(HttpStatus.CREATED)
    public void add(Authentication authentication, @PathVariable Long carId) {
        favoriteService.add(authentication.getName(), carId);
    }

    @Operation(summary = "Quitar favorito", description = "Elimina un coche de los favoritos del usuario autenticado")
    @ApiResponse(responseCode = "204", description = "Coche eliminado de favoritos")
    @DeleteMapping("/{carId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(Authentication authentication, @PathVariable Long carId) {
        favoriteService.remove(authentication.getName(), carId);
    }
}
