package es.urjc.automatch.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Ficha de un coche del mercado espanol.
 * consumption: l/100km (gasolina/diesel/hibridos) o kWh/100km (electricos).
 * depreciation5y: fraccion del precio que se pierde a los 5 anos (0.55 = pierde el 55%).
 * reliability / safety / equipment: puntuaciones 1-10.
 */
@Entity
@Table(name = "cars")
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String brand;
    private String model;

    @Column(name = "model_year") // YEAR es palabra reservada en H2
    private int year;

    private double price;

    @Enumerated(EnumType.STRING)
    private CarType type;

    @Enumerated(EnumType.STRING)
    private FuelType fuel;

    private String dgtLabel;        // C, ECO, CERO
    private double consumption;
    private int powerCv;
    private int trunkLiters;
    private int seats;
    private double reliability;
    private double safety;
    private double equipment;
    private double maintenancePerYear;  // euros/anio estimados
    private double depreciation5y;      // 0..1

    @Column(length = 600)
    private String description;

    private Long ownerId; // null = coche del catalogo oficial

    @Enumerated(EnumType.STRING)
    private CarStatus status = CarStatus.APPROVED;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "car_images", joinColumns = @JoinColumn(name = "car_id"))
    @Column(name = "url")
    @OrderColumn(name = "position")
    private List<String> imageUrls = new ArrayList<>();

    public Car() {
    }

    public Car(String brand, String model, int year, double price, CarType type, FuelType fuel,
               String dgtLabel, double consumption, int powerCv, int trunkLiters, int seats,
               double reliability, double safety, double equipment,
               double maintenancePerYear, double depreciation5y, String description) {
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.price = price;
        this.type = type;
        this.fuel = fuel;
        this.dgtLabel = dgtLabel;
        this.consumption = consumption;
        this.powerCv = powerCv;
        this.trunkLiters = trunkLiters;
        this.seats = seats;
        this.reliability = reliability;
        this.safety = safety;
        this.equipment = equipment;
        this.maintenancePerYear = maintenancePerYear;
        this.depreciation5y = depreciation5y;
        this.description = description;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public CarType getType() { return type; }
    public void setType(CarType type) { this.type = type; }
    public FuelType getFuel() { return fuel; }
    public void setFuel(FuelType fuel) { this.fuel = fuel; }
    public String getDgtLabel() { return dgtLabel; }
    public void setDgtLabel(String dgtLabel) { this.dgtLabel = dgtLabel; }
    public double getConsumption() { return consumption; }
    public void setConsumption(double consumption) { this.consumption = consumption; }
    public int getPowerCv() { return powerCv; }
    public void setPowerCv(int powerCv) { this.powerCv = powerCv; }
    public int getTrunkLiters() { return trunkLiters; }
    public void setTrunkLiters(int trunkLiters) { this.trunkLiters = trunkLiters; }
    public int getSeats() { return seats; }
    public void setSeats(int seats) { this.seats = seats; }
    public double getReliability() { return reliability; }
    public void setReliability(double reliability) { this.reliability = reliability; }
    public double getSafety() { return safety; }
    public void setSafety(double safety) { this.safety = safety; }
    public double getEquipment() { return equipment; }
    public void setEquipment(double equipment) { this.equipment = equipment; }
    public double getMaintenancePerYear() { return maintenancePerYear; }
    public void setMaintenancePerYear(double maintenancePerYear) { this.maintenancePerYear = maintenancePerYear; }
    public double getDepreciation5y() { return depreciation5y; }
    public void setDepreciation5y(double depreciation5y) { this.depreciation5y = depreciation5y; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
    public CarStatus getStatus() { return status; }
    public void setStatus(CarStatus status) { this.status = status; }
    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }
}
