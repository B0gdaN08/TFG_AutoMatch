package es.urjc.automatch.config;

import es.urjc.automatch.model.Car;
import es.urjc.automatch.model.CarType;
import es.urjc.automatch.model.User;
import es.urjc.automatch.repository.CarRepository;
import es.urjc.automatch.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Map;

import static es.urjc.automatch.model.CarType.*;
import static es.urjc.automatch.model.FuelType.*;

/**
 * Carga inicial de datos: 2 usuarios de prueba y un catálogo de coches
 * del mercado español con precios y datos orientativos (2025-2026).
 *
 * Campos del constructor de Car:
 * marca, modelo, año, precio, tipo, combustible, etiqueta DGT,
 * consumo (l/100km o kWh/100km en eléctricos), potencia CV, maletero (l), plazas,
 * fiabilidad (1-10), seguridad (1-10), equipamiento (1-10),
 * mantenimiento (€/año), depreciación a 5 años (0-1), descripción.
 */
@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner initData(CarRepository cars, UserRepository users, PasswordEncoder encoder,
            @Value("${app.seed.admin-password}") String adminPassword,
            @Value("${app.seed.demo-password}") String demoPassword) {
        return args -> {
            if (users.count() == 0) {
                users.save(new User("admin@automatch.es", encoder.encode(adminPassword), "Admin", "ROLE_ADMIN"));
                users.save(new User("demo@automatch.es", encoder.encode(demoPassword), "Usuario Demo", "ROLE_USER"));
            }

            if (cars.count() == 0) {
                seedCars(cars);
            }

            // Coches sin fotos: foto real del modelo si esta en el catalogo oficial,
            // o imagen generica segun su tipo en caso contrario.
            for (Car car : cars.findAll()) {
                if (car.getImageUrls().isEmpty()) {
                    String slug = REAL_PHOTOS.get(car.getBrand() + " " + car.getModel());
                    if (slug != null && car.getOwnerId() == null) {
                        car.setImageUrls(List.of("/images/cars/real/" + slug + ".jpg"));
                    } else {
                        car.setImageUrls(List.of(genericImage(car.getType())));
                    }
                    cars.save(car);
                }
            }
        };
    }

    private void seedCars(CarRepository cars) {
            // --- Urbanos ---
            cars.save(new Car("Dacia", "Sandero", 2025, 15500, URBANO, GASOLINA, "C", 5.3, 91, 328, 5,
                    7.5, 6.0, 5.5, 320, 0.45,
                    "El coche nuevo más vendido de España. Sencillo, barato de mantener y muy difícil de batir en precio."));
            cars.save(new Car("Renault", "Clio E-Tech", 2025, 23900, URBANO, HIBRIDO, "ECO", 4.3, 145, 301, 5,
                    7.0, 8.0, 7.0, 360, 0.48,
                    "Híbrido completo con consumos muy bajos en ciudad y etiqueta ECO."));
            cars.save(new Car("Peugeot", "208", 2025, 21000, URBANO, GASOLINA, "C", 5.0, 100, 311, 5,
                    6.5, 7.5, 7.0, 380, 0.52,
                    "Diseño llamativo e interior i-Cockpit. Uno de los utilitarios más vendidos de Europa."));
            cars.save(new Car("Toyota", "Yaris Hybrid", 2025, 24500, URBANO, HIBRIDO, "ECO", 3.8, 116, 286, 5,
                    9.5, 8.5, 7.0, 290, 0.42,
                    "Referencia en fiabilidad y consumo urbano. Mantiene muy bien su valor de reventa."));
            cars.save(new Car("Opel", "Corsa", 2025, 19800, URBANO, GASOLINA, "C", 5.1, 100, 309, 5,
                    6.8, 7.0, 6.5, 360, 0.53,
                    "Utilitario equilibrado, cómodo y con buena relación equipamiento/precio."));
            cars.save(new Car("Citroen", "C3", 2025, 16900, URBANO, GASOLINA, "C", 5.2, 100, 310, 5,
                    6.5, 6.5, 6.0, 350, 0.50,
                    "Apuesta por el confort: suspensiones blandas y asientos mullidos a precio contenido."));
            cars.save(new Car("Seat", "Ibiza", 2025, 19000, URBANO, GASOLINA, "C", 5.0, 110, 355, 5,
                    7.5, 7.5, 6.5, 340, 0.48,
                    "Clásico español: ágil, con buen maletero para su tamaño y mecánicas TSI conocidas."));
            cars.save(new Car("Renault", "5 E-Tech", 2025, 25000, URBANO, ELECTRICO, "CERO", 15.0, 122, 326, 5,
                    7.0, 8.0, 7.5, 250, 0.50,
                    "Eléctrico urbano de diseño retro. Ideal como segundo coche o para ciudad con carga en casa."));

            // --- Compactos ---
            cars.save(new Car("Seat", "León", 2025, 26500, COMPACTO, GASOLINA, "C", 5.6, 150, 380, 5,
                    7.5, 8.5, 7.5, 400, 0.50,
                    "Compacto de referencia en España: dinámico, espacioso y con buen precio frente al Golf."));
            cars.save(new Car("Volkswagen", "Golf", 2025, 33000, COMPACTO, GASOLINA, "C", 5.5, 150, 381, 5,
                    7.5, 9.0, 8.0, 450, 0.46,
                    "El compacto por excelencia. Calidad percibida alta y excelente comportamiento general."));
            cars.save(new Car("Toyota", "Corolla 140H", 2025, 30500, COMPACTO, HIBRIDO, "ECO", 4.5, 140, 361, 5,
                    9.5, 9.0, 7.5, 310, 0.40,
                    "Híbrido superventas: consumos reales por debajo de 5 l/100 y fiabilidad sobresaliente."));
            cars.save(new Car("Mazda", "3 e-Skyactiv G", 2025, 29500, COMPACTO, GASOLINA, "ECO", 5.9, 140, 351, 5,
                    8.5, 9.0, 8.0, 380, 0.47,
                    "Acabado casi premium, tacto de conducción cuidado y microhibridación con etiqueta ECO."));
            cars.save(new Car("MG", "MG4", 2025, 28000, COMPACTO, ELECTRICO, "CERO", 16.0, 170, 363, 5,
                    6.5, 8.0, 7.5, 260, 0.55,
                    "Uno de los eléctricos con mejor relación precio/autonomía del mercado."));
            cars.save(new Car("BMW", "Serie 1 118", 2025, 36500, COMPACTO, GASOLINA, "C", 6.0, 136, 380, 5,
                    7.5, 8.5, 8.5, 550, 0.48,
                    "Acceso a la marca premium alemana con un compacto refinado y bien equipado."));
            cars.save(new Car("Audi", "A3 Sportback", 2025, 37500, COMPACTO, GASOLINA, "C", 5.8, 150, 380, 5,
                    7.5, 8.5, 8.5, 560, 0.47,
                    "Interior de gran calidad y tecnología del grupo VW en formato compacto premium."));

            // --- Berlinas y familiares ---
            cars.save(new Car("Skoda", "Octavia", 2025, 30000, BERLINA, GASOLINA, "C", 5.4, 150, 600, 5,
                    8.0, 8.5, 7.5, 410, 0.47,
                    "Imbatible en espacio por euro: maletero de 600 litros y habitabilidad de berlina grande."));
            cars.save(new Car("Tesla", "Model 3", 2025, 40000, BERLINA, ELECTRICO, "CERO", 14.0, 283, 594, 5,
                    7.0, 9.5, 9.0, 250, 0.50,
                    "Eléctrico de referencia: gran autonomía, red de Supercargadores y prestaciones altas."));
            cars.save(new Car("Dacia", "Jogger", 2025, 19500, FAMILIAR, GASOLINA, "C", 6.0, 110, 708, 7,
                    7.0, 6.0, 5.5, 340, 0.45,
                    "La opción más barata con 7 plazas. Práctico, modular y de mantenimiento económico."));

            // --- SUV ---
            cars.save(new Car("Mazda", "CX-30 e-Skyactiv G", 2025, 31500, SUV, GASOLINA, "ECO", 6.2, 140, 430, 5,
                    8.5, 9.0, 8.0, 390, 0.46,
                    "SUV compacto con tacto premium, diseño elegante y muy buena nota en seguridad."));
            cars.save(new Car("Hyundai", "Tucson HEV", 2025, 35500, SUV, HIBRIDO, "ECO", 5.6, 215, 616, 5,
                    8.0, 9.0, 8.5, 420, 0.45,
                    "Uno de los SUV más vendidos: híbrido potente, enorme maletero y 5 años de garantía."));
            cars.save(new Car("Kia", "Sportage HEV", 2025, 34500, SUV, HIBRIDO, "ECO", 5.7, 215, 587, 5,
                    8.0, 9.0, 8.5, 420, 0.45,
                    "Primo del Tucson con 7 años de garantía y un equipamiento muy completo."));
            cars.save(new Car("Nissan", "Qashqai", 2025, 33000, SUV, HIBRIDO, "ECO", 5.3, 190, 479, 5,
                    7.5, 8.5, 7.5, 430, 0.49,
                    "El SUV que inventó el segmento, ahora con mecánica e-Power de bajo consumo urbano."));
            cars.save(new Car("Dacia", "Duster", 2025, 21000, SUV, GASOLINA, "C", 6.0, 130, 472, 5,
                    7.5, 6.5, 6.0, 350, 0.42,
                    "SUV más barato del mercado y uno de los que menos se deprecian. Apto para campo."));
            cars.save(new Car("Ford", "Puma", 2025, 26500, SUV, GASOLINA, "ECO", 5.4, 125, 456, 5,
                    7.0, 8.0, 7.0, 380, 0.50,
                    "SUV pequeño divertido de conducir, con microhibridación y maletero MegaBox."));
            cars.save(new Car("Peugeot", "3008 Hybrid", 2025, 38500, SUV, HIBRIDO, "ECO", 5.5, 136, 520, 5,
                    6.8, 8.5, 8.5, 450, 0.50,
                    "Diseño interior espectacular con doble pantalla panorámica y etiqueta ECO."));
            cars.save(new Car("Kia", "EV3", 2025, 36000, SUV, ELECTRICO, "CERO", 15.5, 204, 460, 5,
                    8.0, 8.5, 8.5, 270, 0.48,
                    "SUV eléctrico compacto con gran autonomía real y la garantía de 7 años de Kia."));
            cars.save(new Car("Cupra", "Formentor e-Hybrid", 2025, 42000, SUV, HIBRIDO_ENCHUFABLE, "CERO", 5.5, 204, 345, 5,
                    7.0, 8.5, 8.5, 480, 0.52,
                    "SUV deportivo enchufable: etiqueta CERO, buen empuje y unos 100 km eléctricos WLTP."));
            cars.save(new Car("Skoda", "Kodiaq TDI", 2025, 42000, SUV, DIESEL, "C", 5.6, 150, 910, 7,
                    7.5, 9.0, 8.0, 470, 0.46,
                    "SUV grande de 7 plazas con maletero gigante. El diésel ideal para muchos kilómetros."));
    }

    // Imagen ilustrativa generica segun la carroceria, usada mientras el anuncio no tenga fotos propias.
    private static String genericImage(CarType type) {
        return "/images/cars/" + type.name().toLowerCase() + ".svg";
    }

    // Foto real del modelo (Wikimedia Commons) para cada coche del catalogo oficial,
    // indexada por "marca modelo" (no por id: asi las fotos no se cruzan aunque
    // cambie el orden de insercion o se borre y re-siembre el catalogo).
    private static final Map<String, String> REAL_PHOTOS = Map.ofEntries(
            Map.entry("Dacia Sandero", "dacia-sandero"),
            Map.entry("Renault Clio E-Tech", "renault-clio"),
            Map.entry("Peugeot 208", "peugeot-208"),
            Map.entry("Toyota Yaris Hybrid", "toyota-yaris"),
            Map.entry("Opel Corsa", "opel-corsa"),
            Map.entry("Citroen C3", "citroen-c3"),
            Map.entry("Seat Ibiza", "seat-ibiza"),
            Map.entry("Renault 5 E-Tech", "renault-5"),
            Map.entry("Seat León", "seat-leon"),
            Map.entry("Volkswagen Golf", "volkswagen-golf"),
            Map.entry("Toyota Corolla 140H", "toyota-corolla"),
            Map.entry("Mazda 3 e-Skyactiv G", "mazda-3"),
            Map.entry("MG MG4", "mg-mg4"),
            Map.entry("BMW Serie 1 118", "bmw-serie-1"),
            Map.entry("Audi A3 Sportback", "audi-a3"),
            Map.entry("Skoda Octavia", "skoda-octavia"),
            Map.entry("Tesla Model 3", "tesla-model-3"),
            Map.entry("Dacia Jogger", "dacia-jogger"),
            Map.entry("Mazda CX-30 e-Skyactiv G", "mazda-cx-30"),
            Map.entry("Hyundai Tucson HEV", "hyundai-tucson"),
            Map.entry("Kia Sportage HEV", "kia-sportage"),
            Map.entry("Nissan Qashqai", "nissan-qashqai"),
            Map.entry("Dacia Duster", "dacia-duster"),
            Map.entry("Ford Puma", "ford-puma"),
            Map.entry("Peugeot 3008 Hybrid", "peugeot-3008"),
            Map.entry("Kia EV3", "kia-ev3"),
            Map.entry("Cupra Formentor e-Hybrid", "cupra-formentor"),
            Map.entry("Skoda Kodiaq TDI", "skoda-kodiaq")
    );
}
