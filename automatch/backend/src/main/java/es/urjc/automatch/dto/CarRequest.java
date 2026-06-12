package es.urjc.automatch.dto;

import es.urjc.automatch.model.CarType;
import es.urjc.automatch.model.FuelType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.List;

/** Datos de entrada para crear o editar un coche (anuncio de usuario o catalogo oficial). */
public record CarRequest(

        @NotBlank(message = "La marca es obligatoria")
        String brand,

        @NotBlank(message = "El modelo es obligatorio")
        String model,

        @Min(value = 1990, message = "El año debe estar entre 1990 y 2100")
        @Max(value = 2100, message = "El año debe estar entre 1990 y 2100")
        int year,

        @Positive(message = "El precio debe ser mayor que 0")
        @DecimalMax(value = "1000000", message = "El precio no puede superar 1.000.000 €")
        double price,

        @NotNull(message = "El tipo de vehículo es obligatorio")
        CarType type,

        @NotNull(message = "El combustible es obligatorio")
        FuelType fuel,

        @Pattern(regexp = "C|ECO|CERO", message = "La etiqueta DGT debe ser C, ECO o CERO")
        String dgtLabel,

        @Positive(message = "El consumo debe ser mayor que 0")
        double consumption,

        @Min(value = 1, message = "La potencia debe estar entre 1 y 2000 CV")
        @Max(value = 2000, message = "La potencia debe estar entre 1 y 2000 CV")
        int powerCv,

        @Positive(message = "El maletero debe ser mayor que 0")
        int trunkLiters,

        @Min(value = 2, message = "Las plazas deben estar entre 2 y 9")
        @Max(value = 9, message = "Las plazas deben estar entre 2 y 9")
        int seats,

        @DecimalMin(value = "1", message = "La fiabilidad debe estar entre 1 y 10")
        @DecimalMax(value = "10", message = "La fiabilidad debe estar entre 1 y 10")
        double reliability,

        @DecimalMin(value = "1", message = "La seguridad debe estar entre 1 y 10")
        @DecimalMax(value = "10", message = "La seguridad debe estar entre 1 y 10")
        double safety,

        @DecimalMin(value = "1", message = "El equipamiento debe estar entre 1 y 10")
        @DecimalMax(value = "10", message = "El equipamiento debe estar entre 1 y 10")
        double equipment,

        @PositiveOrZero(message = "El mantenimiento no puede ser negativo")
        double maintenancePerYear,

        @DecimalMin(value = "0", message = "La depreciación a 5 años debe estar entre 0 y 1")
        @DecimalMax(value = "1", message = "La depreciación a 5 años debe estar entre 0 y 1")
        double depreciation5y,

        @Size(max = 600, message = "La descripción no puede superar los 600 caracteres")
        String description,

        @Size(max = 6, message = "Puedes subir un máximo de 6 fotos")
        List<@Pattern(regexp = "^/(uploads|images/cars)/.+", message = "Imagen no válida") String> imageUrls
) {
}
