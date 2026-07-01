package com.port.douala.core.entities;
/**
 * CLASSE FACTICE - À SUPPRIMER QUAND L'ÉQUIPE CORE DONNERA LA VRAIE
 * Cette classe permet à votre IHM de compiler et de tourner.
 */
public class FretEntity {

    // Attributs (identiques à ceux que vous utilisez dans votre IHM)
    private Long id;
    private String shipName;
    private String goodsType;
    private String destination;
    private double weight;
    private String status;

    // ==========================================
    // 1. CONSTRUCTEURS
    // ==========================================
    public FretEntity() {
        // Constructeur vide pour le Builder
    }

    public FretEntity(Long id, String shipName, String goodsType, String destination, double weight, String status) {
        this.id = id;
        this.shipName = shipName;
        this.goodsType = goodsType;
        this.destination = destination;
        this.weight = weight;
        this.status = status;
    }

    // ==========================================
    // 2. GETTERS ET SETTERS (pour que votre IHM fonctionne)
    // ==========================================
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getShipName() { return shipName; }
    public void setShipName(String shipName) { this.shipName = shipName; }

    public String getGoodsType() { return goodsType; }
    public void setGoodsType(String goodsType) { this.goodsType = goodsType; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // ==========================================
    // 3. BUILDER (pour que votre FretDialog fonctionne)
    // ==========================================
    public static class Builder {
        private Long id;
        private String shipName;
        private String goodsType;
        private String destination;
        private double weight;
        private String status;

        public Builder setId(Long id) { this.id = id; return this; }
        public Builder setShipName(String shipName) { this.shipName = shipName; return this; }
        public Builder setGoodsType(String goodsType) { this.goodsType = goodsType; return this; }
        public Builder setDestination(String destination) { this.destination = destination; return this; }
        public Builder setWeight(double weight) { this.weight = weight; return this; }
        public Builder setStatus(String status) { this.status = status; return this; }

        public FretEntity build() {
            return new FretEntity(id, shipName, goodsType, destination, weight, status);
        }
    }

    // ==========================================
    // 4. toString() pour déboguer
    // ==========================================
    @Override
    public String toString() {
        return "FretEntity{" +
                "id=" + id +
                ", shipName='" + shipName + '\'' +
                ", goodsType='" + goodsType + '\'' +
                ", destination='" + destination + '\'' +
                ", weight=" + weight +
                ", status='" + status + '\'' +
                '}';
    }
}