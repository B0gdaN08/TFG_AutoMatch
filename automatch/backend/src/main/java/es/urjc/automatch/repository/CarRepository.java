package es.urjc.automatch.repository;

import es.urjc.automatch.model.Car;
import es.urjc.automatch.model.CarStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface CarRepository extends JpaRepository<Car, Long>, JpaSpecificationExecutor<Car> {
    List<Car> findByStatus(CarStatus status);
    List<Car> findByOwnerId(Long ownerId);
}
