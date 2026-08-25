package es.urjc.automatch.service;

import es.urjc.automatch.model.Car;
import es.urjc.automatch.model.CarStatus;
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

    /**
     * Solo se listan favoritos APPROVED: si un anuncio vuelve a moderacion
     * (o se rechaza), desaparece de los favoritos ajenos en lugar de mostrar
     * una tarjeta cuya ficha daria 404. El favorito se conserva y reaparece
     * si el anuncio se re-aprueba.
     */
    public List<Car> list(String email) {
        return favoriteRepository.findByUser(userByEmail(email)).stream()
                .map(Favorite::getCar)
                .filter(car -> car.getStatus() == CarStatus.APPROVED)
                .toList();
    }

    public void add(String email, Long carId) {
        User user = userByEmail(email);
        // Visibilidad: no se puede marcar por id un anuncio PENDING/REJECTED ajeno
        Car car = carService.findVisibleById(carId, email, false);
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
