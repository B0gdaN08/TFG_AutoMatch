package es.urjc.automatch.dto;

import es.urjc.automatch.model.Car;
import es.urjc.automatch.model.CarStatus;
import es.urjc.automatch.model.CarType;
import es.urjc.automatch.model.FuelType;
import es.urjc.automatch.service.CarCosts;

import java.util.List;

/**
 * Vista publica de un coche: no incluye el ownerId (no debe revelarse).
 * "own" indica si el coche pertenece al usuario autenticado de la peticion.
 * "userListing" indica si es un anuncio de usuario (tiene vendedor contactable),
 * sin revelar su identidad: el contacto se pide aparte en GET /api/cars/{id}/contact.
 * "status" solo se rellena cuando quien consulta es el dueño o un admin;
 * para el resto llega como null (los anuncios visibles para cualquiera estan APPROVED).
 */
public record CarResponse(
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
        double energyCostPer100,
        String description,
        List<String> imageUrls,
        boolean own,
        boolean userListing,
        CarStatus status) {

    public static CarResponse from(Car car, Long currentUserId, boolean isAdmin) {
        boolean own = currentUserId != null && currentUserId.equals(car.getOwnerId());
        CarStatus status = (own || isAdmin) ? car.getStatus() : null;
        // Coste de energia €/100km calculado en el backend (CarCosts) para que el
        // frontend no tenga que duplicar las constantes de precios de carburante.
        double energyCost = Math.round(CarCosts.energyCostPer100Km(car) * 100.0) / 100.0;
        return new CarResponse(
                car.getId(), car.getBrand(), car.getModel(), car.getYear(), car.getPrice(),
                car.getType(), car.getFuel(), car.getDgtLabel(), car.getConsumption(), car.getPowerCv(),
                car.getTrunkLiters(), car.getSeats(), car.getReliability(), car.getSafety(), car.getEquipment(),
                car.getMaintenancePerYear(), car.getDepreciation5y(), energyCost, car.getDescription(),
                car.getImageUrls(), own, car.getOwnerId() != null, status);
    }
}
