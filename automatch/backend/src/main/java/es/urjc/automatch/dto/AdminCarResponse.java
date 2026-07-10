package es.urjc.automatch.dto;

import es.urjc.automatch.model.Car;
import es.urjc.automatch.model.CarStatus;
import es.urjc.automatch.model.CarType;
import es.urjc.automatch.model.FuelType;

import java.util.List;

/**
 * Vista de administracion de un coche: incluye ownerId y status sin condiciones
 * (el endpoint es solo para ADMIN). Evita exponer la entidad JPA directamente.
 */
public record AdminCarResponse(
        Long id,
        String brand,
        String model,
        int year,
        double price,
        CarType type,
        FuelType fuel,
        String dgtLabel,
        double consumption,
        int powerCv,
        int trunkLiters,
        int seats,
        double reliability,
        double safety,
        double equipment,
        double maintenancePerYear,
        double depreciation5y,
        String description,
        List<String> imageUrls,
        Long ownerId,
        CarStatus status) {

    public static AdminCarResponse from(Car car) {
        return new AdminCarResponse(
                car.getId(), car.getBrand(), car.getModel(), car.getYear(), car.getPrice(),
                car.getType(), car.getFuel(), car.getDgtLabel(), car.getConsumption(), car.getPowerCv(),
                car.getTrunkLiters(), car.getSeats(), car.getReliability(), car.getSafety(), car.getEquipment(),
                car.getMaintenancePerYear(), car.getDepreciation5y(), car.getDescription(), car.getImageUrls(),
                car.getOwnerId(), car.getStatus());
    }
}
