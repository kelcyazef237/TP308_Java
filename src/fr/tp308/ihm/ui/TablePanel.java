package fr.tp308.ihm.ui;

import fr.tp308.ihm.model.ConteneurDto;
import fr.tp308.ihm.util.AppTheme;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.util.List;
import java.util.function.Consumer;

public class TablePanel extends JPanel {

    private final MarchandiseTableModel tableModel;
    private final JTable table;
    private Consumer<ConteneurDto> selectionListener;

    public TablePanel() {
        this.tableModel = new MarchandiseTableModel();
        this.table = new JTable(tableModel);
        initializePanel();
        applyTableSettings();
    }

    private void initializePanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(AppTheme.BORDER_COLOR), "Liste des conteneurs"));
        setBackground(AppTheme.PANEL_BACKGROUND);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void applyTableSettings() {
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(28);
        table.setAutoCreateRowSorter(true);
        table.setFont(AppTheme.TABLE_FONT);
        table.setShowGrid(false);
        table.setIntercellSpacing(new java.awt.Dimension(0, 0));
        table.setFillsViewportHeight(true);
        table.getTableHeader().setFont(AppTheme.HEADER_FONT);
        table.getTableHeader().setBackground(AppTheme.TABLE_HEADER_BACKGROUND);
        table.getTableHeader().setForeground(AppTheme.PRIMARY_COLOR);
        table.getTableHeader().setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        table.getSelectionModel().addListSelectionListener(new RowSelectionHandler());
        table.setDefaultRenderer(Object.class, new AlternatingRowRenderer());
        table.setDefaultRenderer(Number.class, new AlternatingRowRenderer());
    }

    public void setSelectionListener(Consumer<ConteneurDto> selectionListener) {
        this.selectionListener = selectionListener;
    }

    public void updateTable(List<ConteneurDto> conteneurs) {
        tableModel.setData(conteneurs);
    }

    public void addRow(ConteneurDto conteneur) {
        tableModel.addRow(conteneur);
    }

    public void updateRow(ConteneurDto conteneur) {
        tableModel.updateRow(conteneur);
    }

    public void removeRow(java.util.UUID id) {
        tableModel.removeRow(id);
    }

    public int getRowCount() {
        return tableModel.getRowCount();
    }

    private class RowSelectionHandler implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent event) {
            if (event.getValueIsAdjusting()) {
                return;
            }
            int selectedRow = table.getSelectedRow();
            if (selectedRow < 0) {
                return;
            }
            int modelRow = table.convertRowIndexToModel(selectedRow);
            ConteneurDto conteneur = tableModel.getRow(modelRow);
            if (selectionListener != null && conteneur != null) {
                selectionListener.accept(conteneur);
            }
        }
    }
}
