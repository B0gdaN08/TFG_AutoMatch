package es.urjc.automatch.service;

import es.urjc.automatch.model.Car;
import es.urjc.automatch.model.CarType;
import es.urjc.automatch.model.FuelType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CarCostsTest {

    private Car cocheConCombustible(FuelType fuel, double consumption) {
        return new Car("Marca", "Modelo", 2025, 20000, CarType.COMPACTO, fuel,
                "C", consumption, 110, 380, 5, 7.5, 8.5, 7.5, 400, 0.5, "Coche de prueba");
    }

    @Test
    void deberiaCalcularElCosteEnergeticoPara100KmEnGasolina() {
        Car car = cocheConCombustible(FuelType.GASOLINA, 6.0);

        assertThat(CarCosts.energyCostPer100Km(car)).isEqualTo(6.0 * CarCosts.GASOLINE_EUR_L);
    }

    @Test
    void deberiaCalcularElCosteEnergeticoPara100KmEnDiesel() {
        Car car = cocheConCombustible(FuelType.DIESEL, 5.0);

        assertThat(CarCosts.energyCostPer100Km(car)).isEqualTo(5.0 * CarCosts.DIESEL_EUR_L);
    }

    @Test
    void deberiaCalcularElCosteEnergeticoPara100KmEnHibrido() {
        Car car = cocheConCombustible(FuelType.HIBRIDO, 4.5);

        assertThat(CarCosts.energyCostPer100Km(car)).isEqualTo(4.5 * CarCosts.GASOLINE_EUR_L);
    }

    @Test
    void deberiaAplicarElFactor070AlCosteEnergeticoDeUnHibridoEnchufable() {
        Car car = cocheConCombustible(FuelType.HIBRIDO_ENCHUFABLE, 5.0);

        assertThat(CarCosts.PHEV_FACTOR).isEqualTo(0.70);
        assertThat(CarCosts.energyCostPer100Km(car))
                .isEqualTo(5.0 * CarCosts.GASOLINE_EUR_L * CarCosts.PHEV_FACTOR);
    }

    @Test
    void deberiaCalcularElCosteEnergeticoPara100KmEnElectricoConElPrecioDelKwh() {
        Car car = cocheConCombustible(FuelType.ELECTRICO, 16.0);

        assertThat(CarCosts.energyCostPer100Km(car)).isEqualTo(16.0 * CarCosts.ELECTRICITY_EUR_KWH);
    }

    @Test
    void deberiaAplicarLaBonificacionDel75PorCientoEnElIvtmParaEtiquetaCero() {
        Car cocheConEtiquetaC = cocheConCombustible(FuelType.GASOLINA, 6.0);
        cocheConEtiquetaC.setPowerCv(120);
        cocheConEtiquetaC.setDgtLabel("C");

        Car cocheConEtiquetaCero = cocheConCombustible(FuelType.ELECTRICO, 16.0);
        cocheConEtiquetaCero.setPowerCv(120);
        cocheConEtiquetaCero.setDgtLabel("CERO");

        double impuestoNormal = CarCosts.taxPerYear(cocheConEtiquetaC);
        double impuestoCero = CarCosts.taxPerYear(cocheConEtiquetaCero);

        assertThat(impuestoNormal).isEqualTo(135.0);
        assertThat(impuestoCero).isEqualTo(Math.round(impuestoNormal * 0.25));
    }

    @Test
    void elSeguroIncluyeUnRecargoParaLosSuv() {
        Car suv = cocheConCombustible(FuelType.GASOLINA, 6.0);
        suv.setType(CarType.SUV);
        suv.setPowerCv(150);

        Car compacto = cocheConCombustible(FuelType.GASOLINA, 6.0);
        compacto.setType(CarType.COMPACTO);
        compacto.setPowerCv(150);

        assertThat(CarCosts.insurancePerYear(suv)).isEqualTo(CarCosts.insurancePerYear(compacto) + 60);
    }

    @Test
    void elSeguroIncluyeUnRecargoParaLosCochesElectricos() {
        Car electrico = cocheConCombustible(FuelType.ELECTRICO, 16.0);
        electrico.setPowerCv(150);

        Car gasolina = cocheConCombustible(FuelType.GASOLINA, 6.0);
        gasolina.setPowerCv(150);

        assertThat(CarCosts.insurancePerYear(electrico)).isEqualTo(CarCosts.insurancePerYear(gasolina) + 90);
    }

    @Test
    void elIvtmDependeDeLaPotenciaDelMotor() {
        Car pocaPotencia = cocheConCombustible(FuelType.GASOLINA, 6.0);
        pocaPotencia.setPowerCv(90);

        Car potenciaMedia = cocheConCombustible(FuelType.GASOLINA, 6.0);
        potenciaMedia.setPowerCv(120);

        Car muchaPotencia = cocheConCombustible(FuelType.GASOLINA, 6.0);
        muchaPotencia.setPowerCv(180);

        assertThat(CarCosts.taxPerYear(pocaPotencia)).isEqualTo(65.0);
        assertThat(CarCosts.taxPerYear(potenciaMedia)).isEqualTo(135.0);
        assertThat(CarCosts.taxPerYear(muchaPotencia)).isEqualTo(190.0);
    }
}
