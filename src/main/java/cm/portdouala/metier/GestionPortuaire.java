package cm.portdouala.metier;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Gestionnaire central des cargaisons du port.
 * Encapsule la liste des marchandises et fournit des méthodes de filtrage,
 * de calcul et de persistance (sérialisation).
 * 
 * @author Equipe Core & Metier
 */
public class GestionPortuaire implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private List<Marchandise> cargaisons;

    /**
     * Constructeur initialisant une liste vide.
     */
    public GestionPortuaire() {
        this.cargaisons = new ArrayList<>();
    }

    /**
     * Ajoute une nouvelle cargaison à la liste.
     * 
     * @param m La marchandise à ajouter (ne peut pas être null)
     * @throws IllegalArgumentException si la marchandise est null
     */
    public void ajouterCargaison(Marchandise m) {
        if (m == null) {
            throw new IllegalArgumentException("La marchandise ne peut pas être nulle.");
        }
        cargaisons.add(m);
    }

    /**
     * Supprime une cargaison par son identifiant.
     * 
     * @param id L'identifiant de la marchandise à supprimer
     * @return <code>true</code> si une suppression a eu lieu, <code>false</code> sinon
     */
    public boolean supprimerCargaison(int id) {
        return cargaisons.removeIf(m -> m.getId() == id);
    }

    /**
     * Retourne une copie de la liste complète des cargaisons.
     * 
     * @return Une nouvelle ArrayList contenant toutes les marchandises
     */
    public List<Marchandise> getToutesLesCargaisons() {
        return new ArrayList<>(cargaisons);
    }

    /**
     * Filtre et retourne la liste des conteneurs réfrigérés actuellement en alerte.
     * 
     * @return Liste des conteneurs réfrigérés dont la température est hors norme
     */
    public List<ConteneurRefrigere> getCargaisonsEnAlerte() {
        return cargaisons.stream()
                .filter(m -> m instanceof ConteneurRefrigere)
                .map(m -> (ConteneurRefrigere) m)
                .filter(ConteneurRefrigere::estEnAlerte)
                .collect(Collectors.toList());
    }

    /**
     * Retourne la liste de tous les conteneurs réfrigérés (utile pour le thread).
     * 
     * @return Liste de tous les objets ConteneurRefrigere
     */
    public List<ConteneurRefrigere> getConteneursRefrigeres() {
        return cargaisons.stream()
                .filter(m -> m instanceof ConteneurRefrigere)
                .map(m -> (ConteneurRefrigere) m)
                .collect(Collectors.toList());
    }

    /**
     * Calcule le montant total des taxes de toutes les cargaisons.
     * 
     * @return Somme des taxes de chaque marchandise
     */
    public double calculerTaxeTotale() {
        return cargaisons.stream()
                .mapToDouble(Marchandise::calculerTaxe)
                .sum();
    }

    /**
     * Sauvegarde l'état complet du gestionnaire (toutes les cargaisons)
     * dans un fichier via la sérialisation Java.
     * 
     * @param chemin Nom du fichier de sauvegarde (ex: "fret.ser")
     * @throws IOException Si une erreur d'écriture se produit
     */
    public void sauvegarder(String chemin) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(chemin))) {
            oos.writeObject(this);
        }
    }

    /**
     * Charge un gestionnaire précédemment sauvegardé depuis un fichier sérialisé.
     * 
     * @param chemin Nom du fichier à charger
     * @return L'objet GestionPortuaire restauré
     * @throws IOException Si une erreur de lecture se produit
     * @throws ClassNotFoundException Si le fichier ne correspond pas au bon type
     */
    public static GestionPortuaire charger(String chemin) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(chemin))) {
            return (GestionPortuaire) ois.readObject();
        }
    }
}