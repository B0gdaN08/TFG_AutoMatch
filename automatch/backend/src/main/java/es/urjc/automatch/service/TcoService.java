package es.urjc.automatch.service;

import es.urjc.automatch.dto.TcoResult;
import es.urjc.automatch.model.Car;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Calculo del Coste Total de Propiedad (TCO):
 * depreciacion + energia + seguro + mantenimiento + impuestos.
 */
@Service
public class TcoService {

    private final CarService carService;

    public TcoService(CarService carService) {
        this.carService = carService;
    }

    public TcoResult calculate(Long carId, int years, int kmPerYear, String email, boolean isAdmin) {
        if (years < 1 || years > 15) {
            throw new IllegalArgumentException("El periodo debe estar entre 1 y 15 años");
        }
        if (kmPerYear < 1000 || kmPerYear > 100000) {
            throw new IllegalArgumentException("Los km anuales deben estar entre 1.000 y 100.000");
        }

        Car car = carService.findVisibleById(carId, email, isAdmin);

        // Depreciacion: simplificacion lineal a partir de la perdida a 5 anos
        // (mejora futura: curva no lineal, mas fuerte los primeros anos).
        double depreciation = car.getPrice() * car.getDepreciation5y() * (years / 5.0);
        depreciation = Math.min(depreciation, car.getPrice() * 0.85);

        double energy = CarCosts.energyCostPer100Km(car) / 100.0 * kmPerYear * years;
        double insurance = CarCosts.insurancePerYear(car) * years;
        // El mantenimiento escala con el uso (referencia: 15.000 km/anio).
        double maintenance = car.getMaintenancePerYear() * years * (kmPerYear / 15000.0);
        double taxes = CarCosts.taxPerYear(car) * years;

        Map<String, Double> breakdown = new LinkedHashMap<>();
        breakdown.put("depreciacion", round(depreciation));
        breakdown.put("energia", round(energy));
        breakdown.put("seguro", round(insurance));
        breakdown.put("mantenimiento", round(maintenance));
        breakdown.put("impuestos", round(taxes));

        double total = breakdown.values().stream().mapToDouble(Double::doubleValue).sum();
        double totalKm = (double) kmPerYear * years;

        return new TcoResult(
                car.getId(),
                car.getBrand() + " " + car.getModel(),
                years,
                kmPerYear,
                breakdown,
                round(total),
                round(total / years),
                Math.round(total / totalKm * 1000.0) / 1000.0);
    }

    private double round(double value) {
        return Math.round(value);
    }
}
