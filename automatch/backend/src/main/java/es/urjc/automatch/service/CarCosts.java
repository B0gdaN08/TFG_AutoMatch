package es.urjc.automatch.service;

import es.urjc.automatch.model.Car;
import es.urjc.automatch.model.CarType;

/**
 * Constantes y formulas de coste compartidas por el TCO y el recomendador.
 * Precios medios de referencia en Espana (actualizables).
 */
public final class CarCosts {

    public static final double GASOLINE_EUR_L = 1.65;
    public static final double DIESEL_EUR_L = 1.55;
    public static final double ELECTRICITY_EUR_KWH = 0.18;
    /** Factor para hibridos enchufables: parte de los km se hacen en electrico. */
    public static final double PHEV_FACTOR = 0.70;

    private CarCosts() {
    }

    /** Coste en euros de recorrer 100 km con este coche. */
    public static double energyCostPer100Km(Car car) {
        return switch (car.getFuel()) {
            case GASOLINA, HIBRIDO -> car.getConsumption() * GASOLINE_EUR_L;
            case DIESEL -> car.getConsumption() * DIESEL_EUR_L;
            case HIBRIDO_ENCHUFABLE -> car.getConsumption() * GASOLINE_EUR_L * PHEV_FACTOR;
            case ELECTRICO -> car.getConsumption() * ELECTRICITY_EUR_KWH;
        };
    }

    /** Seguro anual estimado segun potencia y tipo de coche. */
    public static double insurancePerYear(Car car) {
        double base = 350 + car.getPowerCv() * 2.2;
        if (car.getType() == CarType.SUV) {
            base += 60;
        }
        if (car.getFuel() == es.urjc.automatch.model.FuelType.ELECTRICO) {
            base += 90; // reparaciones mas caras
        }
        return Math.round(base);
    }

    /** Impuesto de circulacion (IVTM) anual aproximado. */
    public static double taxPerYear(Car car) {
        double tax;
        if (car.getPowerCv() < 100) {
            tax = 65;
        } else if (car.getPowerCv() <= 140) {
            tax = 135;
        } else {
            tax = 190;
        }
        if ("CERO".equalsIgnoreCase(car.getDgtLabel())) {
            tax *= 0.25; // bonificacion habitual del 75% para etiqueta CERO
        }
        return Math.round(tax);
    }
}
