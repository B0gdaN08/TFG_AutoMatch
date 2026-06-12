package es.urjc.automatch.service;

import es.urjc.automatch.dto.CarRequest;
import es.urjc.automatch.dto.CarResponse;
import es.urjc.automatch.model.Car;
import es.urjc.automatch.model.CarStatus;
import es.urjc.automatch.model.CarType;
import es.urjc.automatch.model.FuelType;
import es.urjc.automatch.model.User;
import es.urjc.automatch.repository.CarRepository;
import es.urjc.automatch.repository.FavoriteRepository;
import es.urjc.automatch.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class CarService {

    private final CarRepository carRepository;
    private final UserRepository userRepository;
    private final FavoriteRepository favoriteRepository;
    private final ImageStorageService imageStorageService;

    public CarService(CarRepository carRepository, UserRepository userRepository,
                       FavoriteRepository favoriteRepository, ImageStorageService imageStorageService) {
        this.carRepository = carRepository;
        this.userRepository = userRepository;
        this.favoriteRepository = favoriteRepository;
        this.imageStorageService = imageStorageService;
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
     * Listado con filtros opcionales. Con un catalogo pequeno filtramos en memoria;
     * como mejora futura puede pasarse a JPA Specifications o consultas derivadas.
     * Solo incluye coches publicados (catalogo oficial + anuncios aprobados).
     */
    public List<Car> findAll(String search, CarType type, FuelType fuel, Double maxPrice) {
        return carRepository.findAll().stream()
                .filter(car -> car.getStatus() == CarStatus.APPROVED)
                .filter(car -> search == null || search.isBlank()
                        || (car.getBrand() + " " + car.getModel()).toLowerCase().contains(search.toLowerCase()))
                .filter(car -> type == null || car.getType() == type)
                .filter(car -> fuel == null || car.getFuel() == fuel)
                .filter(car -> maxPrice == null || car.getPrice() <= maxPrice)
                .sorted((a, b) -> Double.compare(a.getPrice(), b.getPrice()))
                .toList();
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

    public List<Car> findVisibleByIds(List<Long> ids, String email, boolean isAdmin) {
        return ids.stream().map(id -> findVisibleById(id, email, isAdmin)).toList();
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

    /** Un usuario publica su propio coche en venta: queda pendiente de revision. */
    public Car createListing(CarRequest request, String email) {
        User user = userByEmail(email);
        Car car = toEntity(request);
        car.setOwnerId(user.getId());
        car.setStatus(CarStatus.PENDING);
        return carRepository.save(car);
    }

    /** El admin anade un coche directamente al catalogo oficial. */
    public Car createCatalogCar(CarRequest request) {
        Car car = toEntity(request);
        car.setOwnerId(null);
        car.setStatus(CarStatus.APPROVED);
        return carRepository.save(car);
    }

    public Car update(Long id, CarRequest request) {
        Car car = findById(id);
        applyRequest(car, request);
        return carRepository.save(car);
    }

    public Car setStatus(Long id, CarStatus status) {
        Car car = findById(id);
        car.setStatus(status);
        return carRepository.save(car);
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
        car.setReliability(request.reliability());
        car.setSafety(request.safety());
        car.setEquipment(request.equipment());
        car.setMaintenancePerYear(request.maintenancePerYear());
        car.setDepreciation5y(request.depreciation5y());
        car.setDescription(request.description());
        car.setImageUrls(request.imageUrls() == null ? List.of() : request.imageUrls());
    }
}
