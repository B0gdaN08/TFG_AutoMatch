package es.urjc.automatch.repository;

import es.urjc.automatch.model.Car;
import es.urjc.automatch.model.CarStatus;
import es.urjc.automatch.model.CarType;
import es.urjc.automatch.model.FuelType;
import org.springframework.data.jpa.domain.Specification;

/** Especificaciones JPA componibles para el catálogo de coches. */
public class CarSpecifications {

    private CarSpecifications() {}

    public static Specification<Car> estadoAprobado() {
        return (root, query, cb) -> cb.equal(root.get("status"), CarStatus.APPROVED);
    }

    /** LIKE case-insensitive sobre brand y model. */
    public static Specification<Car> textoContiene(String search) {
        return (root, query, cb) -> {
            String pattern = "%" + search.toLowerCase() + "%";
            return cb.or(
                cb.like(cb.lower(root.get("brand")), pattern),
                cb.like(cb.lower(root.get("model")), pattern)
            );
        };
    }

    public static Specification<Car> tipo(CarType type) {
        return (root, query, cb) -> cb.equal(root.get("type"), type);
    }

    public static Specification<Car> combustible(FuelType fuel) {
        return (root, query, cb) -> cb.equal(root.get("fuel"), fuel);
    }

    public static Specification<Car> precioMinimo(Double minPrice) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price"), minPrice);
    }

    public static Specification<Car> precioMaximo(Double maxPrice) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), maxPrice);
    }

    public static Specification<Car> etiquetaDgt(String dgtLabel) {
        return (root, query, cb) -> cb.equal(root.get("dgtLabel"), dgtLabel);
    }
}
