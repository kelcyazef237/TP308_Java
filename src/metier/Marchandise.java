package metier;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Classe abstraite representant une marchandise generique dans le port.
 * Elle sert de base pour les conteneurs standards et refrigeres.
 *
 * @author Equipe Core & Metier
 * @version 2.0
 */
public abstract class Marchandise implements Serializable {
    private static final long serialVersionUID = 2L;

    private int id;
    private double poids;
    private String description;

    // Nouveaux champs operationnels
    private String numeroConteneur;
    private String origine;
    private String destination;
    private LocalDate dateArrivee;
    private String compagnieMaritime;
    private String statut;

    /**
     * Constructeur de base (compatibilite ascendante) avec valeurs par defaut
     * pour les nouveaux champs operationnels.
     */
    public Marchandise(int id, double poids, String description) {
        if (id < 0) {
            throw new IllegalArgumentException("L'ID doit etre positif ou nul.");
        }
        if (poids <= 0) {
            throw new IllegalArgumentException("Le poids doit etre strictement positif (>= 0.1).");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("La description ne peut pas etre vide.");
        }
        this.id = id;
        this.poids = poids;
        this.description = description;
        this.numeroConteneur = "CONT-" + String.format("%04d", id);
        this.origine = "Non specifie";
        this.destination = "Non specifie";
        this.dateArrivee = LocalDate.now();
        this.compagnieMaritime = "Non specifie";
        this.statut = "En attente";
    }

    /**
     * Constructeur complet avec tous les champs operationnels.
     */
    public Marchandise(int id, double poids, String description,
                       String numeroConteneur, String origine, String destination,
                       LocalDate dateArrivee, String compagnieMaritime, String statut) {
        this(id, poids, description);
        if (numeroConteneur != null && !numeroConteneur.trim().isEmpty())
            this.numeroConteneur = numeroConteneur;
        if (origine != null && !origine.trim().isEmpty())
            this.origine = origine;
        if (destination != null && !destination.trim().isEmpty())
            this.destination = destination;
        if (dateArrivee != null)
            this.dateArrivee = dateArrivee;
        if (compagnieMaritime != null && !compagnieMaritime.trim().isEmpty())
            this.compagnieMaritime = compagnieMaritime;
        if (statut != null && !statut.trim().isEmpty())
            this.statut = statut;
    }

    // --- Getters originaux ---
    public int getId() { return id; }
    public double getPoids() { return poids; }
    public String getDescription() { return description; }

    // --- Getters des nouveaux champs ---
    public String getNumeroConteneur() { return numeroConteneur; }
    public String getOrigine() { return origine; }
    public String getDestination() { return destination; }
    public LocalDate getDateArrivee() { return dateArrivee; }
    public String getCompagnieMaritime() { return compagnieMaritime; }
    public String getStatut() { return statut; }

    // --- Setters des nouveaux champs ---
    public void setNumeroConteneur(String numeroConteneur) {
        if (numeroConteneur != null && !numeroConteneur.trim().isEmpty())
            this.numeroConteneur = numeroConteneur;
    }
    public void setOrigine(String origine) {
        if (origine != null && !origine.trim().isEmpty())
            this.origine = origine;
    }
    public void setDestination(String destination) {
        if (destination != null && !destination.trim().isEmpty())
            this.destination = destination;
    }
    public void setDateArrivee(LocalDate dateArrivee) {
        if (dateArrivee != null)
            this.dateArrivee = dateArrivee;
    }
    public void setCompagnieMaritime(String compagnieMaritime) {
        if (compagnieMaritime != null && !compagnieMaritime.trim().isEmpty())
            this.compagnieMaritime = compagnieMaritime;
    }
    public void setStatut(String statut) {
        if (statut != null && !statut.trim().isEmpty())
            this.statut = statut;
    }

    /**
     * Calcule le montant de la taxe portuaire pour cette marchandise.
     * Polymorphe : chaque sous-classe definit sa propre formule.
     *
     * @return le montant de la taxe en francs CFA
     */
    public abstract double calculerTaxe();

    @Override
    public String toString() {
        return "Marchandise{" +
               "id=" + id +
               ", conteneur='" + numeroConteneur + '\'' +
               ", poids=" + poids +
               ", description='" + description + '\'' +
               ", origine='" + origine + '\'' +
               ", destination='" + destination + '\'' +
               ", date=" + dateArrivee +
               ", compagnie='" + compagnieMaritime + '\'' +
               ", statut='" + statut + '\'' +
               '}';
    }
}
