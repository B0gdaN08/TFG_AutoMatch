package es.urjc.automatch.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Ficha de referencia de un modelo: puntuaciones que el sistema asigna
 * automaticamente a los anuncios (el vendedor no las elige).
 * La tabla se siembra en la migracion V3 y es la "base de conocimiento"
 * de ModelScoreService.
 */
@Entity
@Table(name = "model_scores")
public class ModelScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String brand;
    private String model;

    @Column(name = "model_year")
    private int year;

    private double reliability;
    private double safety;
    private double equipment;
    private double maintenancePerYear;
    private double depreciation5y;

    public ModelScore() {
    }

    public ModelScore(String brand, String model, int year, double reliability, double safety,
                       double equipment, double maintenancePerYear, double depreciation5y) {
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.reliability = reliability;
        this.safety = safety;
        this.equipment = equipment;
        this.maintenancePerYear = maintenancePerYear;
        this.depreciation5y = depreciation5y;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
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
}
