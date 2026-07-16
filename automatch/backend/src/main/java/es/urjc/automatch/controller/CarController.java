package es.urjc.automatch.controller;

import es.urjc.automatch.dto.CarRequest;
import es.urjc.automatch.dto.CarResponse;
import es.urjc.automatch.dto.PageResponse;
import es.urjc.automatch.dto.SellerContact;
import es.urjc.automatch.model.Car;
import es.urjc.automatch.model.CarStatus;
import es.urjc.automatch.model.CarType;
import es.urjc.automatch.model.FuelType;
import es.urjc.automatch.security.AuthUtils;
import es.urjc.automatch.service.CarService;
import es.urjc.automatch.service.ModelScoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cars")
@Tag(name = "Coches", description = "Catálogo de coches y anuncios de usuarios")
public class CarController {

    private static final Map<String, Sort> SORT_MAP = Map.of(
            "price,asc",  Sort.by("price").ascending(),
            "price,desc", Sort.by("price").descending(),
            "year,desc",  Sort.by("year").descending(),
            "brand,asc",  Sort.by("brand").ascending()
    );

    private final CarService carService;
    private final ModelScoreService modelScoreService;

    public CarController(CarService carService, ModelScoreService modelScoreService) {
        this.carService = carService;
        this.modelScoreService = modelScoreService;
    }

    @Operation(summary = "Listar coches paginado",
               description = "Devuelve una página de coches visibles con filtros, ordenación y paginación. " +
                             "sort admite: price,asc | price,desc | year,desc | brand,asc")
    @GetMapping
    public PageResponse<CarResponse> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) CarType type,
            @RequestParam(required = false) FuelType fuel,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String dgtLabel,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false)    String sort,
            Authentication auth) {

        int clampedSize = Math.max(1, Math.min(size, 50));
        int clampedPage = Math.max(0, page);
        Sort springSort = sort != null ? SORT_MAP.getOrDefault(sort, Sort.by("price").ascending())
                                       : Sort.by("price").ascending();
        Pageable pageable = PageRequest.of(clampedPage, clampedSize, springSort);

        String email = AuthUtils.emailOrNull(auth);
        boolean isAdmin = AuthUtils.isAdmin(auth);
        Page<Car> carPage = carService.findAll(search, type, fuel, minPrice, maxPrice, dgtLabel, pageable);
        return PageResponse.of(carService.toResponsePage(carPage, email, isAdmin));
    }

    @Operation(summary = "Marcas y modelos conocidos",
               description = "Base de conocimiento de modelos para los desplegables del formulario de venta")
    @GetMapping("/models")
    public List<ModelScoreService.BrandModels> models() {
        return modelScoreService.brandCatalog();
    }

    @Operation(summary = "Todos los coches aprobados",
               description = "Lista completa sin paginación. Usada por selects de Comparador, TCO y Home.")
    @GetMapping("/all")
    public List<CarResponse> all(Authentication auth) {
        String email = AuthUtils.emailOrNull(auth);
        boolean isAdmin = AuthUtils.isAdmin(auth);
        return carService.toResponseList(carService.findAllApproved(), email, isAdmin);
    }

    @Operation(summary = "Comparar coches", description = "Devuelve los datos de 2 o 3 coches para compararlos lado a lado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Coches encontrados"),
            @ApiResponse(responseCode = "400", description = "Número de coches a comparar fuera de rango (2-3)")
    })
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

    @Operation(summary = "Mis anuncios", description = "Devuelve los anuncios publicados por el usuario autenticado")
    @GetMapping("/mine")
    public List<CarResponse> mine(Authentication auth) {
        var cars = carService.findMine(auth.getName());
        return carService.toResponseList(cars, auth.getName(), AuthUtils.isAdmin(auth));
    }

    @Operation(summary = "Anuncios pendientes", description = "Devuelve los anuncios de usuarios pendientes de moderación (solo administradores)")
    @GetMapping("/pending")
    public List<CarResponse> pending(Authentication auth) {
        var cars = carService.findPending();
        return carService.toResponseList(cars, AuthUtils.emailOrNull(auth), AuthUtils.isAdmin(auth));
    }

    @Operation(summary = "Detalle de un coche", description = "Devuelve los datos de un coche por su id")
    @ApiResponse(responseCode = "404", description = "El coche no existe o no está visible para el usuario actual")
    @GetMapping("/{id}")
    public CarResponse get(@PathVariable Long id, Authentication auth) {
        String email = AuthUtils.emailOrNull(auth);
        boolean isAdmin = AuthUtils.isAdmin(auth);
        var car = carService.findVisibleById(id, email, isAdmin);
        return carService.toResponse(car, email, isAdmin);
    }

    /** Cualquier usuario autenticado publica su coche en venta (queda pendiente de revision). */
    @Operation(summary = "Publicar un anuncio", description = "Crea un anuncio de coche del usuario autenticado, que queda pendiente de revisión")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Anuncio creado"),
            @ApiResponse(responseCode = "400", description = "Datos del coche inválidos")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CarResponse createListing(@Valid @RequestBody CarRequest request, Authentication auth) {
        var car = carService.createListing(request, auth.getName());
        return carService.toResponse(car, auth.getName(), AuthUtils.isAdmin(auth));
    }

    /** El admin anade un coche directamente al catalogo oficial. */
    @Operation(summary = "Añadir coche al catálogo", description = "Crea un coche del catálogo oficial directamente aprobado (solo administradores)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Coche del catálogo creado"),
            @ApiResponse(responseCode = "400", description = "Datos del coche inválidos")
    })
    @PostMapping("/catalog")
    @ResponseStatus(HttpStatus.CREATED)
    public CarResponse createCatalogCar(@Valid @RequestBody CarRequest request, Authentication auth) {
        var car = carService.createCatalogCar(request);
        return carService.toResponse(car, AuthUtils.emailOrNull(auth), AuthUtils.isAdmin(auth));
    }

    /** El admin edita cualquier coche; el dueño solo sus anuncios (y vuelven a revision). */
    @Operation(summary = "Actualizar un coche",
               description = "El administrador edita cualquier coche sin cambiar su estado. " +
                             "El propietario edita sus anuncios: si estaban aprobados o rechazados, vuelven a PENDING")
    @ApiResponses({
            @ApiResponse(responseCode = "403", description = "No eres el propietario ni administrador"),
            @ApiResponse(responseCode = "404", description = "El coche no existe")
    })
    @PutMapping("/{id}")
    public CarResponse update(@PathVariable Long id, @Valid @RequestBody CarRequest request, Authentication auth) {
        var car = carService.update(id, request, auth.getName(), AuthUtils.isAdmin(auth));
        return carService.toResponse(car, auth.getName(), AuthUtils.isAdmin(auth));
    }

    @Operation(summary = "Contacto del vendedor",
               description = "Nombre y email del vendedor de un anuncio aprobado (solo usuarios autenticados)")
    @ApiResponse(responseCode = "404", description = "El coche no existe, no es un anuncio de usuario o no está aprobado")
    @GetMapping("/{id}/contact")
    public SellerContact contact(@PathVariable Long id) {
        return carService.contactInfo(id);
    }

    @Operation(summary = "Aprobar anuncio", description = "Aprueba un anuncio pendiente de un usuario (solo administradores)")
    @ApiResponse(responseCode = "404", description = "El coche no existe")
    @PutMapping("/{id}/approve")
    public CarResponse approve(@PathVariable Long id, Authentication auth) {
        var car = carService.setStatus(id, CarStatus.APPROVED, auth.getName());
        return carService.toResponse(car, AuthUtils.emailOrNull(auth), AuthUtils.isAdmin(auth));
    }

    @Operation(summary = "Rechazar anuncio", description = "Rechaza un anuncio pendiente de un usuario (solo administradores)")
    @ApiResponse(responseCode = "404", description = "El coche no existe")
    @PutMapping("/{id}/reject")
    public CarResponse reject(@PathVariable Long id, Authentication auth) {
        var car = carService.setStatus(id, CarStatus.REJECTED, auth.getName());
        return carService.toResponse(car, AuthUtils.emailOrNull(auth), AuthUtils.isAdmin(auth));
    }

    /** El dueño puede borrar su anuncio; el admin puede borrar cualquier coche. */
    @Operation(summary = "Eliminar un coche", description = "Elimina un anuncio propio o, si es administrador, cualquier coche")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Coche eliminado"),
            @ApiResponse(responseCode = "403", description = "No tienes permiso para eliminar este coche"),
            @ApiResponse(responseCode = "404", description = "El coche no existe")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication auth) {
        carService.delete(id, auth.getName(), AuthUtils.isAdmin(auth));
    }
}
