package metier.persistance;

import java.io.Serializable;

/**
 * Une mesure ponctuelle de temperature pour un conteneur refrigere.
 * Stockee dans un ring buffer de {@link GestionDonnees}.
 */
public class MesureTemperature implements Serializable {
    private static final long serialVersionUID = 1L;
    public final long horodatageMs;
    public final double temperature;
    public final boolean enAlerte;

    public MesureTemperature(long horodatageMs, double temperature, boolean enAlerte) {
        this.horodatageMs = horodatageMs;
        this.temperature = temperature;
        this.enAlerte = enAlerte;
    }
}
