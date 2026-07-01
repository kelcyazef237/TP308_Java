package metier;

/**
 * Représente un conteneur réfrigéré avec contrôle de température.
 * Déclenche une alerte si la température sort de la plage autorisée.
 * 
 * @author Equipe Core & Metier
 */
public class ConteneurRefrigere extends Marchandise {
    private double temperatureActuelle;
    private double temperatureMin;
    private double temperatureMax;
    private boolean alerte;

    /**
     * Constructeur avec température initiale automatique (moyenne de la plage).
     * 
     * @param id          Identifiant
     * @param poids       Poids en kg (> 0)
     * @param description Description
     * @param tempMin     Température minimale autorisée
     * @param tempMax     Température maximale autorisée
     * @throws IllegalArgumentException si tempMin >= tempMax
     */
    public ConteneurRefrigere(int id, double poids, String description,
                              double tempMin, double tempMax) {
        this(id, poids, description, tempMin, tempMax, (tempMin + tempMax) / 2);
    }

    /**
     * Constructeur avec température initiale personnalisée.
     * 
     * @param id          Identifiant
     * @param poids       Poids en kg (> 0)
     * @param description Description
     * @param tempMin     Température minimale autorisée
     * @param tempMax     Température maximale autorisée
     * @param tempInit    Température initiale (doit être entre tempMin et tempMax)
     * @throws IllegalArgumentException si tempMin >= tempMax ou si tempInit hors plage
     */
    public ConteneurRefrigere(int id, double poids, String description,
                              double tempMin, double tempMax, double tempInit) {
        super(id, poids, description);
        if (tempMin >= tempMax) {
            throw new IllegalArgumentException("La température minimale doit être inférieure à la maximale.");
        }
        if (tempInit < tempMin || tempInit > tempMax) {
            throw new IllegalArgumentException("La température initiale doit être comprise entre " + tempMin + " et " + tempMax);
        }
        this.temperatureMin = tempMin;
        this.temperatureMax = tempMax;
        this.temperatureActuelle = tempInit;
        this.alerte = false;
    }

    /**
     * Calcul de la taxe : 5% du poids (base) + 2% supplément énergétique.
     * 
     * @return taxe = poids * 0.07
     */
    @Override
    public double calculerTaxe() {
        return getPoids() * 0.05 + getPoids() * 0.02;
    }

    /**
     * Met à jour la température du conteneur et vérifie si l'alerte change d'état.
     * 
     * @param nouvelleTemp Nouvelle température à enregistrer
     * @return <code>true</code> si l'état d'alerte a changé (normal ↔ alerte),
     *         <code>false</code> sinon.
     */
    public boolean mettreAJourTemperature(double nouvelleTemp) {
        this.temperatureActuelle = nouvelleTemp;
        boolean ancienneAlerte = this.alerte;
        this.alerte = (nouvelleTemp < temperatureMin || nouvelleTemp > temperatureMax);
        return this.alerte != ancienneAlerte;
    }

    // --- Getters ---
    public boolean estEnAlerte() { return alerte; }
    public double getTemperatureActuelle() { return temperatureActuelle; }
    public double getTemperatureMin() { return temperatureMin; }
    public double getTemperatureMax() { return temperatureMax; }

    @Override
    public String toString() {
        return "ConteneurRefrigere{" + super.toString() +
               ", temp=" + temperatureActuelle +
               ", alerte=" + alerte +
               '}';
    }
}