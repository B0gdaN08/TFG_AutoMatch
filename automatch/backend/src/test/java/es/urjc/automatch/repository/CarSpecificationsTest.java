package es.urjc.automatch.repository;

import es.urjc.automatch.model.Car;
import es.urjc.automatch.model.CarStatus;
import es.urjc.automatch.model.CarType;
import es.urjc.automatch.model.FuelType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CarSpecificationsTest {

    @Autowired
    private CarRepository carRepository;

    private Car aprobado(String brand, String model, CarType type, FuelType fuel,
                          double price, String dgtLabel) {
        Car car = new Car(brand, model, 2024, price, type, fuel, dgtLabel,
                6.0, 100, 400, 5, 7.0, 7.0, 7.0, 300, 0.5, "Test");
        car.setStatus(CarStatus.APPROVED);
        return carRepository.save(car);
    }

    @Test
    void estadoAprobadoExcluyeCochesNoAprobados() {
        Car aprobado = aprobado("Seat", "León", CarType.COMPACTO, FuelType.GASOLINA, 20000, "C");
        Car pendiente = new Car("Ford", "Focus", 2024, 22000, CarType.COMPACTO, FuelType.GASOLINA,
                "C", 6.0, 100, 400, 5, 7.0, 7.0, 7.0, 300, 0.5, "Test");
        pendiente.setStatus(CarStatus.PENDING);
        carRepository.save(pendiente);

        List<Car> result = carRepository.findAll(CarSpecifications.estadoAprobado());

        assertThat(result).extracting(Car::getId).containsExactly(aprobado.getId());
    }

    @Test
    void textoContieneBuscaCaseInsensibleEnMarcaYModelo() {
        Car seat = aprobado("Seat", "León", CarType.COMPACTO, FuelType.GASOLINA, 20000, "C");
        Car ford = aprobado("Ford", "León Sport", CarType.COMPACTO, FuelType.GASOLINA, 22000, "C");
        aprobado("Volkswagen", "Golf", CarType.COMPACTO, FuelType.GASOLINA, 25000, "C");

        Specification<Car> spec = Specification.where(CarSpecifications.estadoAprobado())
                .and(CarSpecifications.textoContiene("LEÓN"));

        List<Car> result = carRepository.findAll(spec);

        assertThat(result).extracting(Car::getId).containsExactlyInAnyOrder(seat.getId(), ford.getId());
    }

    @Test
    void filtroPrecioMinimoYMaximo() {
        aprobado("Dacia",    "Sandero", CarType.COMPACTO, FuelType.GASOLINA, 15000, "B");
        Car mediano = aprobado("Seat",     "León",    CarType.COMPACTO, FuelType.GASOLINA, 25000, "C");
        aprobado("Mercedes", "Clase C", CarType.BERLINA,  FuelType.DIESEL,   55000, "C");

        Specification<Car> spec = Specification.where(CarSpecifications.estadoAprobado())
                .and(CarSpecifications.precioMinimo(20000.0))
                .and(CarSpecifications.precioMaximo(30000.0));

        List<Car> result = carRepository.findAll(spec);

        assertThat(result).extracting(Car::getId).containsExactly(mediano.getId());
    }

    @Test
    void filtroCombustibleYEtiquetaDgt() {
        Car electrico = aprobado("Tesla", "Model 3", CarType.BERLINA,  FuelType.ELECTRICO, 40000, "CERO");
        aprobado("Seat", "León",    CarType.COMPACTO, FuelType.GASOLINA,  20000, "C");

        Specification<Car> spec = Specification.where(CarSpecifications.estadoAprobado())
                .and(CarSpecifications.combustible(FuelType.ELECTRICO))
                .and(CarSpecifications.etiquetaDgt("CERO"));

        List<Car> result = carRepository.findAll(spec);

        assertThat(result).extracting(Car::getId).containsExactly(electrico.getId());
    }

    @Test
    void combinaFiltrosTipoYPrecioYCombustible() {
        Car objetivo = aprobado("Kia",   "EV3",      CarType.SUV, FuelType.ELECTRICO, 32000, "CERO");
        aprobado("Kia",   "Sportage", CarType.SUV, FuelType.GASOLINA,  28000, "C");    // combustible incorrecto
        aprobado("Tesla", "Model 3",  CarType.BERLINA, FuelType.ELECTRICO, 41000, "CERO"); // tipo incorrecto

        Specification<Car> spec = Specification.where(CarSpecifications.estadoAprobado())
                .and(CarSpecifications.tipo(CarType.SUV))
                .and(CarSpecifications.combustible(FuelType.ELECTRICO))
                .and(CarSpecifications.precioMaximo(35000.0));

        List<Car> result = carRepository.findAll(spec);

        assertThat(result).extracting(Car::getId).containsExactly(objetivo.getId());
    }
}
