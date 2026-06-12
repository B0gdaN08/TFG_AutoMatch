package es.urjc.automatch.service;

import es.urjc.automatch.model.Car;
import es.urjc.automatch.model.Favorite;
import es.urjc.automatch.model.User;
import es.urjc.automatch.repository.FavoriteRepository;
import es.urjc.automatch.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final CarService carService;

    public FavoriteService(FavoriteRepository favoriteRepository,
                           UserRepository userRepository,
                           CarService carService) {
        this.favoriteRepository = favoriteRepository;
        this.userRepository = userRepository;
        this.carService = carService;
    }

    private User userByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));
    }

    public List<Car> list(String email) {
        return favoriteRepository.findByUser(userByEmail(email)).stream()
                .map(Favorite::getCar)
                .toList();
    }

    public void add(String email, Long carId) {
        User user = userByEmail(email);
        Car car = carService.findById(carId);
        if (!favoriteRepository.existsByUserIdAndCarId(user.getId(), car.getId())) {
            favoriteRepository.save(new Favorite(user, car));
        }
    }

    public void remove(String email, Long carId) {
        User user = userByEmail(email);
        favoriteRepository.findByUserIdAndCarId(user.getId(), carId)
                .ifPresent(favoriteRepository::delete);
    }
}
