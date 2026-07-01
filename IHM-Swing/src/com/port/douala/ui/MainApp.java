package com.port.douala.ui;

import javax.swing.SwingUtilities;

public class MainApp {
    public static void main(String[] args) {
        // Règle d'or : On lance TOUJOURS l'interface dans l'EDT (le thread de Swing)
        SwingUtilities.invokeLater(() -> {
            try {
                // On crée le contrôleur (le chef d'orchestre)
                ApplicationController controller = new ApplicationController();
                // On lance l'application
                controller.start();
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Erreur au démarrage : " + e.getMessage());
            }
        });
    }
}