package metier.ui.models;

import metier.*;

import javax.swing.table.AbstractTableModel;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Modele de table Swing pour l'affichage des cargaisons.
 * Colonnes : N° Conteneur, Type, Cargaison, Poids, Origine, Destination,
 *            Arrivee, Compagnie, Temperature, Taxe, Statut
 *
 * @author Equipe Core & Metier
 * @version 1.0
 */
public class ModeleTableCargaison extends AbstractTableModel {

    private static final String[] COLONNES = {
        "N° Conteneur", "Type", "Cargaison", "Poids (kg)",
        "Origine", "Destination", "Arrivee", "Compagnie",
        "Temperature", "Taxe (CFA)", "Statut"
    };

    private List<Marchandise> cargaisons = new ArrayList<>();
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public int getRowCount() {
        return cargaisons.size();
    }

    @Override
    public int getColumnCount() {
        return COLONNES.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLONNES[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return switch (columnIndex) {
            case 3, 9 -> Double.class; // Poids, Taxe
            case 6 -> String.class;    // Date (formatted)
            default -> String.class;
        };
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex < 0 || rowIndex >= cargaisons.size()) return null;
        Marchandise m = cargaisons.get(rowIndex);

        return switch (columnIndex) {
            case 0  -> m.getNumeroConteneur();
            case 1  -> (m instanceof ConteneurRefrigere) ? "Refrigere" : "Standard";
            case 2  -> m.getDescription();
            case 3  -> m.getPoids();
            case 4  -> m.getOrigine();
            case 5  -> m.getDestination();
            case 6  -> m.getDateArrivee() != null ? m.getDateArrivee().format(DATE_FMT) : "--";
            case 7  -> m.getCompagnieMaritime();
            case 8  -> getTemperature(m);
            case 9  -> m.calculerTaxe();
            case 10 -> m.getStatut();
            default -> null;
        };
    }

    private String getTemperature(Marchandise m) {
        if (m instanceof ConteneurRefrigere cr) {
            return String.format("%.1f °C", cr.getTemperatureActuelle());
        }
        return "--";
    }

    public Marchandise getCargaisonAt(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < cargaisons.size()) {
            return cargaisons.get(rowIndex);
        }
        return null;
    }

    public void actualiser(List<Marchandise> nouvellesCargaisons) {
        this.cargaisons = nouvellesCargaisons != null ? nouvellesCargaisons : new ArrayList<>();
        fireTableDataChanged();
    }

    public List<Marchandise> getCargaisons() {
        return cargaisons;
    }

    /**
     * Retourne l'index de la colonne de statut pour le rendu special.
     */
    public static int getColonneStatut() {
        return 10;
    }

    /**
     * Retourne l'index de la colonne temperature pour le rendu special.
     */
    public static int getColonneTemperature() {
        return 8;
    }

    /**
     * Retourne l'index de la colonne type pour le rendu special.
     */
    public static int getColonneType() {
        return 1;
    }
}
