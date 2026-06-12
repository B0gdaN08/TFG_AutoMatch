package es.urjc.automatch.repository;

import es.urjc.automatch.model.Car;
import es.urjc.automatch.model.CarStatus;
import es.urjc.automatch.model.CarType;
import es.urjc.automatch.model.FuelType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CarRepositoryTest {

    @Autowired
    private CarRepository carRepository;

    private Car nuevoCoche(CarStatus status, Long ownerId) {
        Car car = new Car("Marca", "Modelo", 2025, 20000, CarType.COMPACTO, FuelType.GASOLINA,
                "C", 5.0, 120, 400, 5, 7.0, 7.0, 7.0, 300, 0.5, "Coche de prueba");
        car.setStatus(status);
        car.setOwnerId(ownerId);
        return car;
    }

    @Test
    void deberiaEncontrarCochesPorEstado() {
        carRepository.save(nuevoCoche(CarStatus.PENDING, 1L));
        carRepository.save(nuevoCoche(CarStatus.APPROVED, null));
        carRepository.save(nuevoCoche(CarStatus.REJECTED, 2L));

        List<Car> pendientes = carRepository.findByStatus(CarStatus.PENDING);
        List<Car> aprobados = carRepository.findByStatus(CarStatus.APPROVED);

        assertThat(pendientes).hasSize(1);
        assertThat(pendientes.get(0).getStatus()).isEqualTo(CarStatus.PENDING);
        assertThat(aprobados).hasSize(1);
        assertThat(aprobados.get(0).getStatus()).isEqualTo(CarStatus.APPROVED);
    }

    @Test
    void deberiaEncontrarCochesPorPropietario() {
        carRepository.save(nuevoCoche(CarStatus.PENDING, 10L));
        carRepository.save(nuevoCoche(CarStatus.APPROVED, 10L));
        carRepository.save(nuevoCoche(CarStatus.PENDING, 20L));

        List<Car> cochesDelUsuario10 = carRepository.findByOwnerId(10L);

        assertThat(cochesDelUsuario10).hasSize(2);
        assertThat(cochesDelUsuario10).allMatch(car -> car.getOwnerId().equals(10L));
    }
}
