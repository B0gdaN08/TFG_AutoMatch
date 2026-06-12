package es.urjc.automatch.service;

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

    public List<Car> findByIds(List<Long> ids) {
        return ids.stream().map(this::findById).toList();
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
    public Car createListing(Car car, String email) {
        User user = userByEmail(email);
        car.setId(null);
        car.setOwnerId(user.getId());
        car.setStatus(CarStatus.PENDING);
        return carRepository.save(car);
    }

    /** El admin anade un coche directamente al catalogo oficial. */
    public Car createCatalogCar(Car car) {
        car.setId(null);
        car.setOwnerId(null);
        car.setStatus(CarStatus.APPROVED);
        return carRepository.save(car);
    }

    public Car update(Long id, Car data) {
        Car car = findById(id);
        data.setId(car.getId());
        data.setOwnerId(car.getOwnerId());
        data.setStatus(car.getStatus());
        return carRepository.save(data);
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
}
