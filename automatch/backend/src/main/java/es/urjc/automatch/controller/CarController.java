package es.urjc.automatch.controller;

import es.urjc.automatch.model.Car;
import es.urjc.automatch.model.CarStatus;
import es.urjc.automatch.model.CarType;
import es.urjc.automatch.model.FuelType;
import es.urjc.automatch.service.CarService;
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
    public List<Car> list(@RequestParam(required = false) String search,
                          @RequestParam(required = false) CarType type,
                          @RequestParam(required = false) FuelType fuel,
                          @RequestParam(required = false) Double maxPrice) {
        return carService.findAll(search, type, fuel, maxPrice);
    }

    @GetMapping("/compare")
    public List<Car> compare(@RequestParam List<Long> ids) {
        if (ids.size() < 2 || ids.size() > 3) {
            throw new IllegalArgumentException("Selecciona entre 2 y 3 coches para comparar");
        }
        return carService.findByIds(ids);
    }

    // --- Anuncios de usuarios ---

    @GetMapping("/mine")
    public List<Car> mine(Authentication auth) {
        return carService.findMine(auth.getName());
    }

    @GetMapping("/pending")
    public List<Car> pending() {
        return carService.findPending();
    }

    @GetMapping("/{id}")
    public Car get(@PathVariable Long id) {
        return carService.findById(id);
    }

    /** Cualquier usuario autenticado publica su coche en venta (queda pendiente de revision). */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Car createListing(@RequestBody Car car, Authentication auth) {
        return carService.createListing(car, auth.getName());
    }

    /** El admin anade un coche directamente al catalogo oficial. */
    @PostMapping("/catalog")
    @ResponseStatus(HttpStatus.CREATED)
    public Car createCatalogCar(@RequestBody Car car) {
        return carService.createCatalogCar(car);
    }

    @PutMapping("/{id}")
    public Car update(@PathVariable Long id, @RequestBody Car car) {
        return carService.update(id, car);
    }

    @PutMapping("/{id}/approve")
    public Car approve(@PathVariable Long id) {
        return carService.setStatus(id, CarStatus.APPROVED);
    }

    @PutMapping("/{id}/reject")
    public Car reject(@PathVariable Long id) {
        return carService.setStatus(id, CarStatus.REJECTED);
    }

    /** El dueño puede borrar su anuncio; el admin puede borrar cualquier coche. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication auth) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        carService.delete(id, auth.getName(), isAdmin);
    }
}
