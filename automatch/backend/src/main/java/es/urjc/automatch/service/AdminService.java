package es.urjc.automatch.service;

import es.urjc.automatch.dto.AdminDtos.StatsResponse;
import es.urjc.automatch.dto.AdminDtos.UserSummary;
import es.urjc.automatch.model.Car;
import es.urjc.automatch.model.CarStatus;
import es.urjc.automatch.model.User;
import es.urjc.automatch.repository.CarRepository;
import es.urjc.automatch.repository.FavoriteRepository;
import es.urjc.automatch.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

@Service
public class AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminService.class);

    private static final Set<String> VALID_ROLES = Set.of("ROLE_USER", "ROLE_ADMIN");

    private final UserRepository userRepository;
    private final CarRepository carRepository;
    private final FavoriteRepository favoriteRepository;

    public AdminService(UserRepository userRepository, CarRepository carRepository,
                         FavoriteRepository favoriteRepository) {
        this.userRepository = userRepository;
        this.carRepository = carRepository;
        this.favoriteRepository = favoriteRepository;
    }

    private User userByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));
    }

    private User userById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No existe el usuario con id " + id));
    }

    public List<UserSummary> listUsers() {
        return userRepository.findAll().stream()
                .map(u -> new UserSummary(u.getId(), u.getEmail(), u.getName(), u.getRole()))
                .toList();
    }

    public UserSummary updateRole(Long id, String role, String currentEmail) {
        if (!VALID_ROLES.contains(role)) {
            throw new IllegalArgumentException("Rol no válido: " + role);
        }
        User target = userById(id);
        User current = userByEmail(currentEmail);
        if (target.getId().equals(current.getId())) {
            throw new IllegalArgumentException("No puedes cambiar tu propio rol");
        }
        target.setRole(role);
        userRepository.save(target);
        log.info("Auditoria: {} cambia el rol de {} (id={}) a {}", currentEmail, target.getEmail(), id, role);
        return new UserSummary(target.getId(), target.getEmail(), target.getName(), target.getRole());
    }

    @Transactional
    public void deleteUser(Long id, String currentEmail) {
        User target = userById(id);
        User current = userByEmail(currentEmail);
        if (target.getId().equals(current.getId())) {
            throw new IllegalArgumentException("No puedes borrar tu propia cuenta");
        }
        favoriteRepository.deleteByUserId(target.getId());
        for (Car car : carRepository.findByOwnerId(target.getId())) {
            favoriteRepository.deleteByCarId(car.getId());
            carRepository.delete(car);
        }
        userRepository.delete(target);
        log.info("Auditoria: {} elimina al usuario {} (id={})", currentEmail, target.getEmail(), id);
    }

    public StatsResponse stats() {
        List<Car> cars = carRepository.findAll();
        List<User> users = userRepository.findAll();

        long catalogCars = cars.stream().filter(c -> c.getOwnerId() == null).count();
        long userListings = cars.stream().filter(c -> c.getOwnerId() != null).count();
        long pendingListings = cars.stream().filter(c -> c.getStatus() == CarStatus.PENDING).count();
        long approvedListings = cars.stream()
                .filter(c -> c.getOwnerId() != null && c.getStatus() == CarStatus.APPROVED).count();
        long rejectedListings = cars.stream().filter(c -> c.getStatus() == CarStatus.REJECTED).count();
        long totalAdmins = users.stream().filter(u -> "ROLE_ADMIN".equals(u.getRole())).count();

        return new StatsResponse(catalogCars, userListings, pendingListings, approvedListings,
                rejectedListings, users.size(), totalAdmins, favoriteRepository.count());
    }
}
