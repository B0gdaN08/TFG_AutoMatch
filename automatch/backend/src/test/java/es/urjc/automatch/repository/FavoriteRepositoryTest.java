package es.urjc.automatch.repository;

import es.urjc.automatch.model.Car;
import es.urjc.automatch.model.CarType;
import es.urjc.automatch.model.Favorite;
import es.urjc.automatch.model.FuelType;
import es.urjc.automatch.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class FavoriteRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Test
    void noPermiteMarcarElMismoCocheComoFavoritoDosVecesParaElMismoUsuario() {
        User user = entityManager.persistAndFlush(new User("usuario@test.es", "hash", "Usuario", "ROLE_USER"));
        Car car = entityManager.persistAndFlush(new Car("Marca", "Modelo", 2025, 20000, CarType.COMPACTO,
                FuelType.GASOLINA, "C", 5.0, 120, 400, 5, 7.0, 7.0, 7.0, 300, 0.5, "Coche de prueba"));

        favoriteRepository.saveAndFlush(new Favorite(user, car));

        assertThat(favoriteRepository.existsByUserIdAndCarId(user.getId(), car.getId())).isTrue();

        assertThatThrownBy(() -> favoriteRepository.saveAndFlush(new Favorite(user, car)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
