package fr.tp308.ihm.model;

import java.util.Objects;
import java.util.UUID;

public class ConteneurDto {

    private final UUID id;
    private String type;
    private String code;
    private double poids;
    private Double temperature;
    private boolean alerte;
    private double taxe;

    public ConteneurDto(UUID id,
                        String type,
                        String code,
                        double poids,
                        Double temperature,
                        boolean alerte,
                        double taxe) {
        this.id = Objects.requireNonNull(id, "id ne peut pas être null");
        this.type = Objects.requireNonNull(type, "type ne peut pas être null");
        this.code = Objects.requireNonNull(code, "code ne peut pas être null");
        this.poids = poids;
        this.temperature = temperature;
        this.alerte = alerte;
        this.taxe = taxe;
    }

    public UUID getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public double getPoids() {
        return poids;
    }

    public void setPoids(double poids) {
        this.poids = poids;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public boolean isAlerte() {
        return alerte;
    }

    public void setAlerte(boolean alerte) {
        this.alerte = alerte;
    }

    public double getTaxe() {
        return taxe;
    }

    public void setTaxe(double taxe) {
        this.taxe = taxe;
    }
}
