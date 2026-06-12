package es.urjc.automatch.controller;

import es.urjc.automatch.dto.CarResponse;
import es.urjc.automatch.security.AuthUtils;
import es.urjc.automatch.service.CarService;
import es.urjc.automatch.service.FavoriteService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final CarService carService;

    public FavoriteController(FavoriteService favoriteService, CarService carService) {
        this.favoriteService = favoriteService;
        this.carService = carService;
    }

    @GetMapping
    public List<CarResponse> list(Authentication authentication) {
        var cars = favoriteService.list(authentication.getName());
        return carService.toResponseList(cars, authentication.getName(), AuthUtils.isAdmin(authentication));
    }

    @PostMapping("/{carId}")
    @ResponseStatus(HttpStatus.CREATED)
    public void add(Authentication authentication, @PathVariable Long carId) {
        favoriteService.add(authentication.getName(), carId);
    }

    @DeleteMapping("/{carId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(Authentication authentication, @PathVariable Long carId) {
        favoriteService.remove(authentication.getName(), carId);
    }
}
