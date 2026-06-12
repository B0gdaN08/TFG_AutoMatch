package es.urjc.automatch.service;

import es.urjc.automatch.dto.TcoResult;
import es.urjc.automatch.model.Car;
import es.urjc.automatch.model.CarType;
import es.urjc.automatch.model.FuelType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TcoServiceTest {

    @Mock
    private CarService carService;

    private TcoService tcoService;

    @BeforeEach
    void setUp() {
        tcoService = new TcoService(carService);

        Car car = cocheDePrueba();
        lenient().when(carService.findVisibleById(eq(1L), eq((String) null), anyBoolean())).thenReturn(car);
    }

    private Car cocheDePrueba() {
        Car car = new Car("Seat", "León", 2025, 20000, CarType.COMPACTO, FuelType.GASOLINA,
                "C", 6.0, 110, 380, 5, 7.5, 8.5, 7.5, 400, 0.5, "Coche de prueba");
        car.setId(1L);
        return car;
    }

    @Test
    void deberiaCalcularElDesgloseCorrectoParaUnCocheConocido() {
        TcoResult result = tcoService.calculate(1L, 5, 15000, null, false);

        assertThat(result.breakdown().get("depreciacion")).isEqualTo(10000.0);
        assertThat(result.breakdown().get("energia")).isEqualTo(7425.0);
        assertThat(result.breakdown().get("seguro")).isEqualTo(2960.0);
        assertThat(result.breakdown().get("mantenimiento")).isEqualTo(2000.0);
        assertThat(result.breakdown().get("impuestos")).isEqualTo(675.0);
        assertThat(result.totalCost()).isEqualTo(23060.0);
        assertThat(result.costPerYear()).isEqualTo(4612.0);
        assertThat(result.costPerKm()).isEqualTo(0.307);
    }

    @Test
    void deberiaCaparLaDepreciacionAl85PorCientoDelPrecio() {
        Car car = cocheDePrueba();
        car.setDepreciation5y(0.9);
        when(carService.findVisibleById(eq(2L), eq((String) null), anyBoolean())).thenReturn(car);

        TcoResult result = tcoService.calculate(2L, 10, 15000, null, false);

        assertThat(result.breakdown().get("depreciacion")).isEqualTo(20000 * 0.85);
    }

    @Test
    void elMantenimientoEscalaConLosKmAnuales() {
        TcoResult resultado15000 = tcoService.calculate(1L, 5, 15000, null, false);
        TcoResult resultado30000 = tcoService.calculate(1L, 5, 30000, null, false);

        assertThat(resultado30000.breakdown().get("mantenimiento"))
                .isEqualTo(resultado15000.breakdown().get("mantenimiento") * 2);
    }

    @Test
    void deberiaLanzarExcepcionSiLosAnosEstanFueraDeRango() {
        assertThatThrownBy(() -> tcoService.calculate(1L, 0, 15000, null, false))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> tcoService.calculate(1L, 16, 15000, null, false))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deberiaLanzarExcepcionSiLosKmAnualesEstanFueraDeRango() {
        assertThatThrownBy(() -> tcoService.calculate(1L, 5, 999, null, false))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> tcoService.calculate(1L, 5, 100001, null, false))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
