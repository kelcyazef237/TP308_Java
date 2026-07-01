package com.port.douala.ui;

import com.port.douala.core.entities.FretEntity;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.List;

public class MainFrame extends JFrame {

    private final ApplicationController controller;
    private JTable mainTable;
    private FretTableModel tableModel;
    private JLabel statusLabel;

    public MainFrame(ApplicationController controller) {
        this.controller = controller;
        initUI();
    }

    private void initUI() {
        setTitle("Système de Gestion du Trafic de Fret - Port Autonome de Douala");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // 1. Barre d'outils (Toolbar) avec les boutons
        JToolBar toolBar = new JToolBar();
        JButton btnAdd = new JButton("➕ Ajouter");
        JButton btnEdit = new JButton("✏️ Modifier");
        JButton btnDelete = new JButton("🗑️ Supprimer");

        btnAdd.addActionListener(e -> controller.onAddFret());
        btnEdit.addActionListener(e -> {
            int row = mainTable.getSelectedRow();
            if (row != -1) {
                // Conversion pour gérer le tri (même s'il n'y en a pas pour l'instant)
                FretEntity entity = tableModel.getEntityAt(row);
                controller.onEditFret(entity);
            } else {
                JOptionPane.showMessageDialog(this, "Sélectionnez une ligne à modifier.", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        btnDelete.addActionListener(e -> {
            int row = mainTable.getSelectedRow();
            if (row != -1) {
                FretEntity entity = tableModel.getEntityAt(row);
                controller.onDeleteFret(entity);
            } else {
                JOptionPane.showMessageDialog(this, "Sélectionnez une ligne à supprimer.", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        toolBar.add(btnAdd);
        toolBar.add(btnEdit);
        toolBar.add(btnDelete);
        add(toolBar, BorderLayout.NORTH);

        // 2. Le tableau (JTable) au centre
        tableModel = new FretTableModel(controller.getDataList());
        mainTable = new JTable(tableModel);
        mainTable.setRowHeight(25);
        JScrollPane scrollPane = new JScrollPane(mainTable);
        add(scrollPane, BorderLayout.CENTER);

        // 3. Barre de statut (en bas)
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusLabel = new JLabel("🌡️ Température : 25.0 °C (En attente de la simulation)");
        statusPanel.add(statusLabel);
        add(statusPanel, BorderLayout.SOUTH);
    }

    // Méthode appelée par le contrôleur pour rafraîchir les données
    public void refreshTableData() {
        tableModel.setData(controller.getDataList());
    }

    // ==========================================================
    // MODÈLE DE TABLEAU INTERNE (AbstractTableModel)
    // ==========================================================
    private static class FretTableModel extends AbstractTableModel {
        private List<FretEntity> data;
        private final String[] COLUMNS = {"ID", "Navire", "Marchandise", "Destination", "Statut", "Poids (T)"};

        public FretTableModel(List<FretEntity> data) {
            this.data = data;
        }

        public void setData(List<FretEntity> newData) {
            this.data = newData;
            fireTableDataChanged(); // Préviens la JTable que tout a changé
        }

        public FretEntity getEntityAt(int row) {
            return data.get(row);
        }

        @Override
        public int getRowCount() { return data.size(); }

        @Override
        public int getColumnCount() { return COLUMNS.length; }

        @Override
        public String getColumnName(int col) { return COLUMNS[col]; }

        @Override
        public Object getValueAt(int row, int col) {
            FretEntity e = data.get(row);
            switch (col) {
                case 0: return e.getId();
                case 1: return e.getShipName();
                case 2: return e.getGoodsType();
                case 3: return e.getDestination();
                case 4: return e.getStatus();
                case 5: return e.getWeight();
                default: return null;
            }
        }

        @Override
        public boolean isCellEditable(int row, int col) { return false; }
    }
}