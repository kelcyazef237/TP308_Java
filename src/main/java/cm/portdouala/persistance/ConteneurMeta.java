package cm.portdouala.persistance;

import java.io.Serializable;
import java.util.UUID;

/**
 * Métadonnées associées à un conteneur métier ({@code metier.Marchandise}),
 * mais que la classe métier ne porte pas elle-même :
 * - l'UUID utilisé côté IHM (ConteneurDto),
 * - le code conteneur (ex: "C001", "R101"),
 * - la plage de température autorisée (pour les conteneurs réfrigérés uniquement).
 *
 * Cette classe permet de faire le pont entre le monde métier (id int) et
 * le monde IHM (id UUID) sans modifier la classe Marchandise de l'équipe Core.
 *
 * @author Equipe Persistance
 */
public class ConteneurMeta implements Serializable {
    private static final long serialVersionUID = 1L;

    private final UUID uuid;
    private final int idMetier;
    private String code;
    private Double temperatureMin; // null si conteneur standard
    private Double temperatureMax; // null si conteneur standard

    public ConteneurMeta(UUID uuid, int idMetier, String code,
                          Double temperatureMin, Double temperatureMax) {
        this.uuid = uuid;
        this.idMetier = idMetier;
        this.code = code;
        this.temperatureMin = temperatureMin;
        this.temperatureMax = temperatureMax;
    }

    public UUID getUuid() { return uuid; }
    public int getIdMetier() { return idMetier; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public Double getTemperatureMin() { return temperatureMin; }
    public Double getTemperatureMax() { return temperatureMax; }
    public void setTemperatureMin(Double v) { this.temperatureMin = v; }
    public void setTemperatureMax(Double v) { this.temperatureMax = v; }
}
