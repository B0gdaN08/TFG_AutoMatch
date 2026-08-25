package es.urjc.automatch.service;

import es.urjc.automatch.dto.CarRequest;
import es.urjc.automatch.dto.CarResponse;
import es.urjc.automatch.dto.SellerContact;
import es.urjc.automatch.model.Car;
import es.urjc.automatch.model.CarStatus;
import es.urjc.automatch.model.CarType;
import es.urjc.automatch.model.FuelType;
import es.urjc.automatch.model.User;
import es.urjc.automatch.repository.CarRepository;
import es.urjc.automatch.repository.CarSpecifications;
import es.urjc.automatch.repository.FavoriteRepository;
import es.urjc.automatch.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class CarService {

    private static final Logger log = LoggerFactory.getLogger(CarService.class);

    private final CarRepository carRepository;
    private final UserRepository userRepository;
    private final FavoriteRepository favoriteRepository;
    private final ImageStorageService imageStorageService;
    private final ModelScoreService modelScoreService;

    public CarService(CarRepository carRepository, UserRepository userRepository,
                       FavoriteRepository favoriteRepository, ImageStorageService imageStorageService,
                       ModelScoreService modelScoreService) {
        this.carRepository = carRepository;
        this.userRepository = userRepository;
        this.favoriteRepository = favoriteRepository;
        this.imageStorageService = imageStorageService;
        this.modelScoreService = modelScoreService;
    }

    private User userByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));
    }

    private Long userIdOrNull(String email) {
        if (email == null) {
            return null;
        }
        return userRepository.findByEmail(email).map(User::getId).orElse(null);
    }

    /**
     * Listado paginado con filtros opcionales usando JPA Specifications.
     * Solo incluye coches publicados (catálogo oficial + anuncios aprobados).
     */
    public Page<Car> findAll(String search, CarType type, FuelType fuel,
                              Double minPrice, Double maxPrice, String dgtLabel,
                              Pageable pageable) {
        Specification<Car> spec = Specification.where(CarSpecifications.estadoAprobado());
        if (search != null && !search.isBlank())
            spec = spec.and(CarSpecifications.textoContiene(search));
        if (type != null)
            spec = spec.and(CarSpecifications.tipo(type));
        if (fuel != null)
            spec = spec.and(CarSpecifications.combustible(fuel));
        if (minPrice != null)
            spec = spec.and(CarSpecifications.precioMinimo(minPrice));
        if (maxPrice != null)
            spec = spec.and(CarSpecifications.precioMaximo(maxPrice));
        if (dgtLabel != null && !dgtLabel.isBlank())
            spec = spec.and(CarSpecifications.etiquetaDgt(dgtLabel));
        return carRepository.findAll(spec, pageable);
    }

    /** Lista completa de aprobados, sin paginación, para selects y destacados. */
    public List<Car> findAllApproved() {
        return carRepository.findAll(Specification.where(CarSpecifications.estadoAprobado()));
    }

    public Car findById(Long id) {
        return carRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No existe el coche con id " + id));
    }

    /**
     * Como findById, pero comprobando visibilidad: un coche que no esta APPROVED
     * (PENDING o REJECTED) solo lo puede ver su propietario o un admin. Para
     * cualquier otro caso se lanza la misma excepcion que si no existiera (404),
     * de forma que no se pueda distinguir "no existe" de "no tienes acceso".
     */
    public Car findVisibleById(Long id, String email, boolean isAdmin) {
        Car car = findById(id);
        if (car.getStatus() != CarStatus.APPROVED && !isAdmin) {
            Long userId = userIdOrNull(email);
            boolean owner = userId != null && userId.equals(car.getOwnerId());
            if (!owner) {
                throw new NoSuchElementException("No existe el coche con id " + id);
            }
        }
        return car;
    }

    /** Anuncios publicados por el usuario, en cualquier estado. */
    public List<Car> findMine(String email) {
        User user = userByEmail(email);
        return carRepository.findByOwnerId(user.getId());
    }

    /** Anuncios de usuarios pendientes de moderacion. */
    public List<Car> findPending() {
        return carRepository.findByStatus(CarStatus.PENDING);
    }

    /**
     * Un usuario publica su propio coche en venta: queda pendiente de revision.
     * Las puntuaciones expertas NO las elige el vendedor: se asignan automaticamente
     * desde la base de conocimiento de modelos.
     */
    public Car createListing(CarRequest request, String email) {
        User user = userByEmail(email);
        Car car = toEntity(request);
        applyScores(car, request, false);
        car.setOwnerId(user.getId());
        car.setStatus(CarStatus.PENDING);
        return carRepository.save(car);
    }

    /** El admin anade un coche directamente al catalogo oficial. */
    public Car createCatalogCar(CarRequest request) {
        Car car = toEntity(request);
        applyScores(car, request, true);
        car.setOwnerId(null);
        car.setStatus(CarStatus.APPROVED);
        return carRepository.save(car);
    }

    /**
     * Edita un coche. El admin puede editar cualquiera sin efectos secundarios;
     * el dueño solo puede editar sus propios anuncios y, si el anuncio ya estaba
     * moderado (APPROVED o REJECTED), vuelve a PENDING para re-moderacion.
     */
    public Car update(Long id, CarRequest request, String email, boolean isAdmin) {
        Car car = findById(id);
        if (!isAdmin) {
            User user = userByEmail(email);
            if (car.getOwnerId() == null || !car.getOwnerId().equals(user.getId())) {
                throw new AccessDeniedException("No puedes editar este coche");
            }
            if (car.getStatus() != CarStatus.PENDING) {
                car.setStatus(CarStatus.PENDING);
                log.info("Auditoria: {} edita su anuncio {} ({} {}) y vuelve a moderacion (PENDING)",
                        email, id, car.getBrand(), car.getModel());
            }
        }
        applyRequest(car, request);
        applyScores(car, request, isAdmin);
        return carRepository.save(car);
    }

    public Car setStatus(Long id, CarStatus status, String actorEmail) {
        Car car = findById(id);
        car.setStatus(status);
        log.info("Auditoria: {} cambia el estado del coche {} ({} {}) a {}",
                actorEmail, id, car.getBrand(), car.getModel(), status);
        return carRepository.save(car);
    }

    /**
     * Contacto del vendedor: solo para anuncios de usuarios (ownerId != null)
     * que esten APPROVED. Para el resto, 404 indistinguible de "no existe".
     */
    public SellerContact contactInfo(Long id) {
        Car car = findById(id);
        if (car.getOwnerId() == null || car.getStatus() != CarStatus.APPROVED) {
            throw new NoSuchElementException("Este coche no tiene información de contacto disponible");
        }
        User owner = userRepository.findById(car.getOwnerId())
                .orElseThrow(() -> new NoSuchElementException("Este coche no tiene información de contacto disponible"));
        return new SellerContact(owner.getName(), owner.getEmail());
    }

    @Transactional
    public void delete(Long id, String email, boolean isAdmin) {
        Car car = findById(id);
        if (!isAdmin) {
            User user = userByEmail(email);
            if (car.getOwnerId() == null || !car.getOwnerId().equals(user.getId())) {
                throw new AccessDeniedException("No puedes borrar este coche");
            }
        }
        favoriteRepository.deleteByCarId(id);
        carRepository.delete(car);
        log.info("Auditoria: {} elimina el coche {} ({} {})", email, id, car.getBrand(), car.getModel());
        for (String url : car.getImageUrls()) {
            imageStorageService.delete(url);
        }
    }

    // --- Conversion a DTOs ---

    public CarResponse toResponse(Car car, String email, boolean isAdmin) {
        return CarResponse.from(car, userIdOrNull(email), isAdmin);
    }

    public List<CarResponse> toResponseList(List<Car> cars, String email, boolean isAdmin) {
        Long userId = userIdOrNull(email);
        return cars.stream().map(car -> CarResponse.from(car, userId, isAdmin)).toList();
    }

    public Page<CarResponse> toResponsePage(Page<Car> cars, String email, boolean isAdmin) {
        Long userId = userIdOrNull(email);
        return cars.map(car -> CarResponse.from(car, userId, isAdmin));
    }

    private Car toEntity(CarRequest request) {
        Car car = new Car();
        applyRequest(car, request);
        return car;
    }

    private void applyRequest(Car car, CarRequest request) {
        car.setBrand(request.brand());
        car.setModel(request.model());
        car.setYear(request.year());
        car.setPrice(request.price());
        car.setType(request.type());
        car.setFuel(request.fuel());
        car.setDgtLabel(request.dgtLabel());
        car.setConsumption(request.consumption());
        car.setPowerCv(request.powerCv());
        car.setTrunkLiters(request.trunkLiters());
        car.setSeats(request.seats());
        car.setDescription(request.description());
        car.setImageUrls(request.imageUrls() == null ? new ArrayList<>() : new ArrayList<>(request.imageUrls()));
    }

    /**
     * Asigna las puntuaciones expertas (fiabilidad, seguridad, equipamiento,
     * mantenimiento y depreciacion). Solo se respetan las del request cuando
     * quien edita es admin Y las envia todas; en cualquier otro caso se
     * resuelven automaticamente desde la base de conocimiento de modelos.
     */
    private void applyScores(Car car, CarRequest request, boolean trustRequest) {
        boolean allProvided = request.reliability() != null && request.safety() != null
                && request.equipment() != null && request.maintenancePerYear() != null
                && request.depreciation5y() != null;
        if (trustRequest && allProvided) {
            car.setReliability(request.reliability());
            car.setSafety(request.safety());
            car.setEquipment(request.equipment());
            car.setMaintenancePerYear(request.maintenancePerYear());
            car.setDepreciation5y(request.depreciation5y());
            return;
        }

        ModelScoreService.ResolvedScores scores =
                modelScoreService.resolve(request.brand(), request.model(), request.year());
        car.setReliability(scores.reliability());
        car.setSafety(scores.safety());
        car.setEquipment(scores.equipment());
        car.setMaintenancePerYear(scores.maintenancePerYear());
        car.setDepreciation5y(scores.depreciation5y());
        log.info("Puntuaciones auto-asignadas a {} {} ({}): fiab={} seg={} equip={} [fuente={}]",
                request.brand(), request.model(), request.year(),
                scores.reliability(), scores.safety(), scores.equipment(), scores.source());
    }
}
