package es.urjc.automatch.service;

import es.urjc.automatch.model.Car;
import es.urjc.automatch.model.CarStatus;
import es.urjc.automatch.model.User;
import es.urjc.automatch.repository.CarRepository;
import es.urjc.automatch.repository.FavoriteRepository;
import es.urjc.automatch.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CarServiceTest {

    @Mock
    private CarRepository carRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private FavoriteRepository favoriteRepository;
    @Mock
    private ImageStorageService imageStorageService;

    private CarService carService;

    @BeforeEach
    void setUp() {
        carService = new CarService(carRepository, userRepository, favoriteRepository, imageStorageService);
    }

    private Car carWithStatus(CarStatus status, Long ownerId) {
        Car car = new Car();
        car.setId(1L);
        car.setStatus(status);
        car.setOwnerId(ownerId);
        return car;
    }

    @Test
    void approvedCarIsVisibleToAnyone() {
        Car car = carWithStatus(CarStatus.APPROVED, 5L);
        when(carRepository.findById(1L)).thenReturn(Optional.of(car));

        Car result = carService.findVisibleById(1L, null, false);

        assertThat(result).isSameAs(car);
    }

    @Test
    void pendingCarIsHiddenFromAnonymousUsers() {
        Car car = carWithStatus(CarStatus.PENDING, 5L);
        when(carRepository.findById(1L)).thenReturn(Optional.of(car));

        assertThatThrownBy(() -> carService.findVisibleById(1L, null, false))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("No existe el coche con id 1");
    }

    @Test
    void pendingCarIsHiddenFromOtherUsers() {
        Car car = carWithStatus(CarStatus.PENDING, 5L);
        when(carRepository.findById(1L)).thenReturn(Optional.of(car));
        User other = new User("otro@automatch.es", "x", "Otro", "ROLE_USER");
        other.setId(99L);
        when(userRepository.findByEmail("otro@automatch.es")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> carService.findVisibleById(1L, "otro@automatch.es", false))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("No existe el coche con id 1");
    }

    @Test
    void pendingCarIsVisibleToItsOwner() {
        Car car = carWithStatus(CarStatus.PENDING, 5L);
        when(carRepository.findById(1L)).thenReturn(Optional.of(car));
        User owner = new User("owner@automatch.es", "x", "Owner", "ROLE_USER");
        owner.setId(5L);
        when(userRepository.findByEmail("owner@automatch.es")).thenReturn(Optional.of(owner));

        Car result = carService.findVisibleById(1L, "owner@automatch.es", false);

        assertThat(result).isSameAs(car);
    }

    @Test
    void rejectedCarIsVisibleToAdmin() {
        Car car = carWithStatus(CarStatus.REJECTED, 5L);
        when(carRepository.findById(1L)).thenReturn(Optional.of(car));

        Car result = carService.findVisibleById(1L, null, true);

        assertThat(result).isSameAs(car);
    }
}
