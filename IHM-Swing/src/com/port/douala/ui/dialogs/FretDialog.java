package com.port.douala.ui.dialogs;

import com.port.douala.core.entities.FretEntity;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class FretDialog extends JDialog {

    // ===========================================================
    // 1. ZONE DES ATTRIBUTS (LES CHAMPS DE SAISIE)
    // ===========================================================
    private JTextField txtShipName;
    private JTextField txtGoods;
    private JTextField txtDestination;
    private JTextField txtWeight;
    private JComboBox<String> comboStatus;

    // Variables d'état pour savoir si on valide et l'entité retournée
    private boolean confirmed = false;
    private FretEntity resultEntity;

    // ===========================================================
    // 2. CONSTRUCTEUR
    // ===========================================================
    public FretDialog(JFrame parent, String title, FretEntity entityToEdit) {
        super(parent, title, true); // 'true' = MODAL
        this.resultEntity = entityToEdit;

        // Appel des méthodes qui construisent l'interface
        initUI();                // Étape 1 : Dessiner les champs et boutons
        prefillFieldsIfEdit();   // Étape 2 : Remplir si c'est une modification

        // Ajustements finaux
        pack();
        setLocationRelativeTo(parent);
        setResizable(false);

        // ===========================================================
        // 3. ASTUCES D'ERGONOMIE (à placer dans le constructeur)
        // ===========================================================

        // Astuce 1 : Le curseur cligne directement dans le champ "Navire"
        SwingUtilities.invokeLater(() -> txtShipName.requestFocusInWindow());

        // Astuce 2 : Appuyer sur la touche ECHAP ferme la fenêtre
        getRootPane().registerKeyboardAction(
                e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    // ===========================================================
    // 4. MÉTHODE : CONSTRUIRE L'INTERFACE (initUI)
    // ===========================================================
    private void initUI() {
        // Panel principal avec une marge
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // --- Création des composants ---
        txtShipName = new JTextField(15);
        txtGoods = new JTextField(15);
        txtDestination = new JTextField(15);
        txtWeight = new JTextField(10);

        String[] statuses = {"EN_ATTENTE", "EN_COURS", "LIVRE", "BLOQUE"};
        comboStatus = new JComboBox<>(statuses);
        comboStatus.setEditable(false);

        // --- Placement des lignes (Ligne 0 à 4) ---
        // Ligne 0 : Navire
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Nom du Navire :"), gbc);
        gbc.gridx = 1;
        panel.add(txtShipName, gbc);

        // Ligne 1 : Marchandise
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Type Marchandise :"), gbc);
        gbc.gridx = 1;
        panel.add(txtGoods, gbc);

        // Ligne 2 : Destination
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Destination :"), gbc);
        gbc.gridx = 1;
        panel.add(txtDestination, gbc);

        // Ligne 3 : Poids
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Poids (Tonnes) :"), gbc);
        gbc.gridx = 1;
        panel.add(txtWeight, gbc);

        // Ligne 4 : Statut
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Statut :"), gbc);
        gbc.gridx = 1;
        panel.add(comboStatus, gbc);

        // --- Ligne 5 : Boutons (avec leurs écouteurs) ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnOk = new JButton("Enregistrer");
        JButton btnCancel = new JButton("Annuler");

        // ECOUTEUR du bouton OK (câblage directement ici)
        btnOk.addActionListener(e -> {
            // On appelle la validation
            if (validateInput()) {
                // Construction de l'entité (ici on utilise le Builder)
                FretEntity.Builder builder = new FretEntity.Builder()
                        .setShipName(txtShipName.getText().trim())
                        .setGoodsType(txtGoods.getText().trim())
                        .setDestination(txtDestination.getText().trim())
                        .setWeight(Double.parseDouble(txtWeight.getText().trim()))
                        .setStatus((String) comboStatus.getSelectedItem());

                // Si c'est une modification, on récupère l'ID de l'ancien
                if (resultEntity != null) {
                    builder.setId(resultEntity.getId());
                }

                resultEntity = builder.build();
                confirmed = true;
                dispose(); // Ferme la boîte
            }
        });

        // ECOUTEUR du bouton Annuler
        btnCancel.addActionListener(e -> {
            confirmed = false;
            dispose();
        });

        buttonPanel.add(btnOk);
        buttonPanel.add(btnCancel);

        // Ajout du panel des boutons sur la ligne 5
        gbc.gridx = 0; gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(buttonPanel, gbc);

        // Application du panel à la fenêtre
        setContentPane(panel);
    }

    // ===========================================================
    // 5. MÉTHODE : PRÉ-REMPLIR LES CHAMPS (préfillFieldsIfEdit)
    // ===========================================================
    private void prefillFieldsIfEdit() {
        if (this.resultEntity != null) {
            // On remplit les champs avec les valeurs de l'entité
            txtShipName.setText(this.resultEntity.getShipName());
            txtGoods.setText(this.resultEntity.getGoodsType());
            txtDestination.setText(this.resultEntity.getDestination());
            txtWeight.setText(String.valueOf(this.resultEntity.getWeight()));
            comboStatus.setSelectedItem(this.resultEntity.getStatus());

            // On change le titre pour indiquer qu'on modifie
            setTitle("Modifier le Fret - ID: " + this.resultEntity.getId());
        } else {
            setTitle("Ajouter un nouveau Fret");
        }
    }

    // ===========================================================
    // 6. MÉTHODE : VALIDATION DES DONNÉES (validateInput)
    // ===========================================================
    private boolean validateInput() {
        String ship = txtShipName.getText().trim();
        String goods = txtGoods.getText().trim();
        String dest = txtDestination.getText().trim();
        String weightStr = txtWeight.getText().trim();

        // Vérification des champs vides
        if (ship.isEmpty() || goods.isEmpty() || dest.isEmpty() || weightStr.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Tous les champs doivent être remplis !",
                    "Erreur de saisie",
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }

        // Vérification que le poids est un nombre positif
        try {
            double weight = Double.parseDouble(weightStr);
            if (weight <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Le poids doit être un nombre décimal positif (ex: 12.5) !",
                    "Erreur de saisie",
                    JOptionPane.ERROR_MESSAGE);
            txtWeight.requestFocus();
            txtWeight.selectAll();
            return false;
        }
        return true;
    }

    // ===========================================================
    // 7. GETTEURS PUBLICS (pour récupérer les résultats)
    // ===========================================================
    public boolean isConfirmed() {
        return confirmed;
    }

    public FretEntity getResultEntity() {
        return resultEntity;
    }
}