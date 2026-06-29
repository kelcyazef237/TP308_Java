package metier;
import java.io.Serializable;

/**
 * Classe abstraite représentant une marchandise générique dans le port.
 * Elle sert de base pour les conteneurs standards et réfrigérés.
 * 
 * @author Equipe Core & Metier
 * @version 1.0
 */
public abstract class Marchandise implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int id;
    private double poids;
    private String description;

    /**
     * Constructeur de la marchandise avec validation des champs.
     * 
     * @param id          Identifiant unique (doit être >= 0)
     * @param poids       Poids en kilogrammes (doit être > 0)
     * @param description Description textuelle (ne doit pas être vide)
     * @throws IllegalArgumentException si un des champs est invalide
     */
    public Marchandise(int id, double poids, String description) {
        if (id < 0) {
            throw new IllegalArgumentException("L'ID doit être positif ou nul.");
        }
        if (poids <= 0) {
            throw new IllegalArgumentException("Le poids doit être strictement positif (>= 0.1).");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("La description ne peut pas être vide.");
        }
        this.id = id;
        this.poids = poids;
        this.description = description;
    }

    // --- Getters ---
    public int getId() { return id; }
    public double getPoids() { return poids; }
    public String getDescription() { return description; }

    /**
     * Calcule le montant de la taxe portuaire pour cette marchandise.
     * Cette méthode est polymorphe : chaque sous-classe définit sa propre formule.
     * 
     * @return le montant de la taxe en francs CFA
     */
    public abstract double calculerTaxe();

    @Override
    public String toString() {
        return "Marchandise{" +
               "id=" + id +
               ", poids=" + poids +
               ", description='" + description + '\'' +
               '}';
    }
}