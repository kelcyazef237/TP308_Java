package cm.portdouala.metier;

/**
 * Représente un conteneur standard sans contrôle de température.
 * 
 * @author Equipe Core & Metier
 */
public class ConteneurStandard extends Marchandise {
    private String contenu;

    /**
     * Constructeur du conteneur standard.
     * 
     * @param id          Identifiant unique
     * @param poids       Poids en kg (> 0)
     * @param description Description
     * @param contenu     Nature de la marchandise transportée (non vide)
     * @throws IllegalArgumentException si le contenu est vide
     */
    public ConteneurStandard(int id, double poids, String description, String contenu) {
        super(id, poids, description);
        if (contenu == null || contenu.trim().isEmpty()) {
            throw new IllegalArgumentException("Le contenu ne peut pas être vide.");
        }
        this.contenu = contenu;
    }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) {
        if (contenu == null || contenu.trim().isEmpty()) {
            throw new IllegalArgumentException("Le contenu ne peut pas être vide.");
        }
        this.contenu = contenu;
    }

    /**
     * Calcul de la taxe : 5% du poids.
     * 
     * @return taxe = poids * 0.05
     */
    @Override
    public double calculerTaxe() {
        return getPoids() * 0.05;
    }

    @Override
    public String toString() {
        return "ConteneurStandard{" + super.toString() + ", contenu='" + contenu + '\'' + '}';
    }
}