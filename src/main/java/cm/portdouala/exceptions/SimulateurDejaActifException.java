package cm.portdouala.exceptions;

/**
 * Levée si on tente de démarrer un simulateur déjà en cours.
 */
public class SimulateurDejaActifException extends RuntimeException {
    private final String nomSimulateur;

    public SimulateurDejaActifException(String nomSimulateur) {
        super("Le simulateur '" + nomSimulateur + "' est déjà actif.");
        this.nomSimulateur = nomSimulateur;
    }

    public String getNomSimulateur() { return nomSimulateur; }
}