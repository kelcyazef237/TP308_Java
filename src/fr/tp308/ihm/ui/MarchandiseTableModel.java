package fr.tp308.ihm.ui;

import fr.tp308.ihm.model.ConteneurDto;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MarchandiseTableModel extends AbstractTableModel {

    private static final String[] COLUMN_NAMES = {
            "Type",
            "Code",
            "Poids (kg)",
            "Température (°C)",
            "Alerte",
            "Taxe"
    };

    private final List<ConteneurDto> data;

    public MarchandiseTableModel() {
        this.data = new ArrayList<>();
    }

    public void setData(List<ConteneurDto> conteneurs) {
        data.clear();
        if (conteneurs != null) {
            data.addAll(conteneurs);
        }
        fireTableDataChanged();
    }

    public void addRow(ConteneurDto conteneur) {
        data.add(conteneur);
        fireTableRowsInserted(data.size() - 1, data.size() - 1);
    }

    public void updateRow(ConteneurDto conteneur) {
        int index = findRowIndex(conteneur.getId());
        if (index >= 0) {
            data.set(index, conteneur);
            fireTableRowsUpdated(index, index);
        }
    }

    public void removeRow(UUID id) {
        int index = findRowIndex(id);
        if (index >= 0) {
            data.remove(index);
            fireTableRowsDeleted(index, index);
        }
    }

    public ConteneurDto getRow(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < data.size()) {
            return data.get(rowIndex);
        }
        return null;
    }

    public int getRowCount() {
        return data.size();
    }

    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }

    public String getColumnName(int column) {
        return COLUMN_NAMES[column];
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        ConteneurDto conteneur = getRow(rowIndex);
        if (conteneur == null) {
            return "";
        }
        return switch (columnIndex) {
            case 0 -> conteneur.getType();
            case 1 -> conteneur.getCode();
            case 2 -> conteneur.getPoids();
            case 3 -> conteneur.getTemperature() != null ? conteneur.getTemperature() : "N/A";
            case 4 -> conteneur.isAlerte() ? "Oui" : "Non";
            case 5 -> conteneur.getTaxe();
            default -> "";
        };
    }

    private int findRowIndex(UUID id) {
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).getId().equals(id)) {
                return i;
            }
        }
        return -1;
    }
}
