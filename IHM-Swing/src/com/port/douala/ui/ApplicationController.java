package com.port.douala.ui;

import com.port.douala.core.entities.FretEntity;
import com.port.douala.ui.dialogs.FretDialog;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class ApplicationController {

    private MainFrame mainFrame;
    // Liste factice qui fait office de "base de données" pour vos tests
    private List<FretEntity> dataList;

    public ApplicationController() {
        // On initialise la liste avec 2 exemples pour que le tableau ne soit pas vide
        dataList = new ArrayList<>();
        dataList.add(createFakeEntity(1L, "Titanic", "Conteneurs", "Europe", 150.5, "EN_ATTENTE"));
        dataList.add(createFakeEntity(2L, "Queen Mary", "Céréales", "Asie", 200.0, "LIVRE"));
    }

    // Méthode qui crée des objets factices (car l'équipe Core n'a pas encore fini)
    private FretEntity createFakeEntity(Long id, String ship, String goods, String dest, double weight, String status) {
        // J'utilise le Builder que j'ai mis dans la classe factice FretEntity
        return new FretEntity.Builder()
                .setId(id)
                .setShipName(ship)
                .setGoodsType(goods)
                .setDestination(dest)
                .setWeight(weight)
                .setStatus(status)
                .build();
    }

    // Lancement de l'application
    public void start() {
        mainFrame = new MainFrame(this);
        mainFrame.setVisible(true);
        // On rafraîchit le tableau avec les données factices
        refreshTable();
    }

    // Récupère la liste des données (utilisé par le modèle de la JTable)
    public List<FretEntity> getDataList() {
        return dataList;
    }

    // Action : Ajouter un fret
    public void onAddFret() {
        // On ouvre la boîte de dialogue en mode AJOUT (on passe null)
        FretDialog dialog = new FretDialog(mainFrame, "Ajouter un fret", null);
        dialog.setVisible(true);

        // Si l'utilisateur a cliqué sur "Enregistrer"
        if (dialog.isConfirmed()) {
            FretEntity newFret = dialog.getResultEntity();
            // On lui donne un ID fictif (pour l'exemple)
            long newId = dataList.stream().mapToLong(FretEntity::getId).max().orElse(0) + 1;
            newFret.setId(newId);

            // On l'ajoute à la liste
            dataList.add(newFret);
            // On rafraîchit le tableau
            refreshTable();

            JOptionPane.showMessageDialog(mainFrame, "Fret ajouté avec succès !");
        }
    }

    // Action : Modifier un fret
    public void onEditFret(FretEntity selectedEntity) {
        if (selectedEntity == null) {
            JOptionPane.showMessageDialog(mainFrame, "Veuillez sélectionner une ligne.", "Info", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // On ouvre la boîte de dialogue en mode MODIFICATION (on passe l'entité sélectionnée)
        FretDialog dialog = new FretDialog(mainFrame, "Modifier le fret", selectedEntity);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            FretEntity updatedFret = dialog.getResultEntity();
            // On cherche l'ancien dans la liste et on le remplace
            int index = dataList.indexOf(selectedEntity);
            if (index != -1) {
                dataList.set(index, updatedFret);
                refreshTable();
                JOptionPane.showMessageDialog(mainFrame, "Fret modifié avec succès !");
            }
        }
    }

    // Action : Supprimer un fret
    public void onDeleteFret(FretEntity selectedEntity) {
        if (selectedEntity == null) {
            JOptionPane.showMessageDialog(mainFrame, "Veuillez sélectionner une ligne.", "Info", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(mainFrame,
                "Voulez-vous vraiment supprimer ce fret ?",
                "Confirmation",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            dataList.remove(selectedEntity);
            refreshTable();
            JOptionPane.showMessageDialog(mainFrame, "Fret supprimé !");
        }
    }

    // Rafraîchit l'affichage du tableau
    private void refreshTable() {
        if (mainFrame != null) {
            mainFrame.refreshTableData();
        }
    }
}