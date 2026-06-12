package es.urjc.automatch.controller;

import es.urjc.automatch.dto.CarRequest;
import es.urjc.automatch.dto.CarResponse;
import es.urjc.automatch.model.Car;
import es.urjc.automatch.model.CarStatus;
import es.urjc.automatch.model.CarType;
import es.urjc.automatch.model.FuelType;
import es.urjc.automatch.security.AuthUtils;
import es.urjc.automatch.service.CarService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cars")
public class CarController {

    private final CarService carService;

    public CarController(CarService carService) {
        this.carService = carService;
    }

    @GetMapping
    public List<CarResponse> list(@RequestParam(required = false) String search,
                          @RequestParam(required = false) CarType type,
                          @RequestParam(required = false) FuelType fuel,
                          @RequestParam(required = false) Double maxPrice,
                          Authentication auth) {
        List<Car> cars = carService.findAll(search, type, fuel, maxPrice);
        return carService.toResponseList(cars, AuthUtils.emailOrNull(auth), AuthUtils.isAdmin(auth));
    }

    @GetMapping("/compare")
    public List<CarResponse> compare(@RequestParam List<Long> ids, Authentication auth) {
        if (ids.size() < 2 || ids.size() > 3) {
            throw new IllegalArgumentException("Selecciona entre 2 y 3 coches para comparar");
        }
        String email = AuthUtils.emailOrNull(auth);
        boolean isAdmin = AuthUtils.isAdmin(auth);
        var cars = carService.findVisibleByIds(ids, email, isAdmin);
        return carService.toResponseList(cars, email, isAdmin);
    }

    // --- Anuncios de usuarios ---

    @GetMapping("/mine")
    public List<CarResponse> mine(Authentication auth) {
        var cars = carService.findMine(auth.getName());
        return carService.toResponseList(cars, auth.getName(), AuthUtils.isAdmin(auth));
    }

    @GetMapping("/pending")
    public List<CarResponse> pending(Authentication auth) {
        var cars = carService.findPending();
        return carService.toResponseList(cars, AuthUtils.emailOrNull(auth), AuthUtils.isAdmin(auth));
    }

    @GetMapping("/{id}")
    public CarResponse get(@PathVariable Long id, Authentication auth) {
        String email = AuthUtils.emailOrNull(auth);
        boolean isAdmin = AuthUtils.isAdmin(auth);
        var car = carService.findVisibleById(id, email, isAdmin);
        return carService.toResponse(car, email, isAdmin);
    }

    /** Cualquier usuario autenticado publica su coche en venta (queda pendiente de revision). */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CarResponse createListing(@Valid @RequestBody CarRequest request, Authentication auth) {
        var car = carService.createListing(request, auth.getName());
        return carService.toResponse(car, auth.getName(), AuthUtils.isAdmin(auth));
    }

    /** El admin anade un coche directamente al catalogo oficial. */
    @PostMapping("/catalog")
    @ResponseStatus(HttpStatus.CREATED)
    public CarResponse createCatalogCar(@Valid @RequestBody CarRequest request, Authentication auth) {
        var car = carService.createCatalogCar(request);
        return carService.toResponse(car, AuthUtils.emailOrNull(auth), AuthUtils.isAdmin(auth));
    }

    @PutMapping("/{id}")
    public CarResponse update(@PathVariable Long id, @Valid @RequestBody CarRequest request, Authentication auth) {
        var car = carService.update(id, request);
        return carService.toResponse(car, AuthUtils.emailOrNull(auth), AuthUtils.isAdmin(auth));
    }

    @PutMapping("/{id}/approve")
    public CarResponse approve(@PathVariable Long id, Authentication auth) {
        var car = carService.setStatus(id, CarStatus.APPROVED);
        return carService.toResponse(car, AuthUtils.emailOrNull(auth), AuthUtils.isAdmin(auth));
    }

    @PutMapping("/{id}/reject")
    public CarResponse reject(@PathVariable Long id, Authentication auth) {
        var car = carService.setStatus(id, CarStatus.REJECTED);
        return carService.toResponse(car, AuthUtils.emailOrNull(auth), AuthUtils.isAdmin(auth));
    }

    /** El dueño puede borrar su anuncio; el admin puede borrar cualquier coche. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication auth) {
        carService.delete(id, auth.getName(), AuthUtils.isAdmin(auth));
    }
}
