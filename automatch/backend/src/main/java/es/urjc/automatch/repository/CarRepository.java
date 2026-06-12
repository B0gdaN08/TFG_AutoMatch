package es.urjc.automatch.repository;

import es.urjc.automatch.model.Car;
import es.urjc.automatch.model.CarStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CarRepository extends JpaRepository<Car, Long> {
    List<Car> findByStatus(CarStatus status);
    List<Car> findByOwnerId(Long ownerId);
}
