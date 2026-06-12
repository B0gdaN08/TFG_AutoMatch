package es.urjc.automatch.controller;

import es.urjc.automatch.model.Car;
import es.urjc.automatch.service.FavoriteService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @GetMapping
    public List<Car> list(Authentication authentication) {
        return favoriteService.list(authentication.getName());
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
